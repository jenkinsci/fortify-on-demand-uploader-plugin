package org.jenkinsci.plugins.fodupload.polling;

import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.LookupItemsController;
import org.jenkinsci.plugins.fodupload.controllers.ReleaseController;
import org.jenkinsci.plugins.fodupload.models.AnalysisStatusTypeEnum;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.response.LookupItemsModel;
import org.jenkinsci.plugins.fodupload.models.response.PollingSummaryDTO;
import org.jenkinsci.plugins.fodupload.models.response.PollingSummaryPauseDetail;
import org.jenkinsci.plugins.fodupload.models.response.ScanSummaryDTO;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.APILookupItemTypes;

public class ScanStatusPoller {

    private final static int MAX_FAILS = 3;

    private FodApiConnection apiConnection;
    private int failCount = 0;
    private int pollingInterval;
    private PrintStream logger;

    /**
     * Constructor
     *
     * @param apiConnection   apiConnection connection to use
     * @param pollingInterval the polling interval in minutes
     * @param logger          the PrintStream that will be logged to
     */
    public ScanStatusPoller(FodApiConnection apiConnection, int pollingInterval, PrintStream logger) {
        this.apiConnection = apiConnection;
        this.pollingInterval = pollingInterval;
        this.logger = logger;
    }

    /**
     * Polls the release status
     *
     * @param releaseId     release id
     * @param scanId        scan id of the release
     * @param correlationId correlation id related to scan id
     *                      at_return true if status is completed | cancelled.
     * @throws java.io.IOException  in certain cases
     * @throws InterruptedException in certain cases
     */
    public PollReleaseStatusResult pollReleaseStatus(final int releaseId, final int scanId, final String correlationId) throws IOException, InterruptedException {


        logger.println("Begin polling Fortify on Demand for results.");

        boolean finished = false;
        int counter = 1;
        LookupItemsController lookupItemsController = new LookupItemsController(this.apiConnection, logger, correlationId);
        List<LookupItemsModel> analysisStatusTypes = lookupItemsController.getLookupItems(APILookupItemTypes.AnalysisStatusTypes);
        StatusPollerThread pollerThread = null;

        // Create a list of values that will be used to break the loop if found
        // This way if any of this changes we don't need to redo the keys or something
        List<String> complete = new ArrayList<>();

        if (analysisStatusTypes != null) {
            for (LookupItemsModel item : analysisStatusTypes) {
                if (item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Completed.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Canceled.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Waiting.name()))
                    complete.add(item.getValue());
            }
        }

        try {
            while (!finished) {
                if (analysisStatusTypes == null) {
                    analysisStatusTypes = lookupItemsController.getLookupItems(APILookupItemTypes.AnalysisStatusTypes);
                    complete = new ArrayList<>();
                    for (LookupItemsModel item : analysisStatusTypes) {
                        if (item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Completed.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Canceled.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Waiting.name()))
                            complete.add(item.getValue());
                    }
                }
                if (counter == 1) {
                    //No Thread.sleep() on first round
                    pollerThread = new StatusPollerThread(String.valueOf(counter), releaseId, analysisStatusTypes, apiConnection, complete, logger, 0, scanId, correlationId);
                } else {
                    pollerThread = new StatusPollerThread(String.valueOf(counter), releaseId, analysisStatusTypes, apiConnection, complete, logger, pollingInterval, scanId, correlationId);
                }

                pollerThread.start();
                pollerThread.join();
                if (pollerThread.fail) {
                    failCount++;
                    continue;
                }

                if (failCount < MAX_FAILS) {
                    if (!pollerThread.fail && pollerThread.statusString != null) {
                        failCount = 0;
                        logger.println(pollerThread.getName() + ") Poll Status: " + pollerThread.statusString);

                        if (pollerThread.statusString.equals(AnalysisStatusTypeEnum.Waiting.name()) && pollerThread.pollingSummaryDTO.getPauseDetails() != null)
                            printPauseMessages(pollerThread.pollingSummaryDTO);
                        if (pollerThread.finished) {
                            finished = pollerThread.finished;

                            if (pollerThread.statusString.equals(AnalysisStatusTypeEnum.Canceled.name())) {
                                printCancelMessages(pollerThread.pollingSummaryDTO, releaseId);
                            } else if (pollerThread.statusString.equals(AnalysisStatusTypeEnum.Completed.name())) {
                                printPassFail(pollerThread.pollingSummaryDTO, releaseId);
                            }
                        }
                        counter++;
                    }
                } else {
                    logger.println(String.format("Polling Failed %d times.  Terminating", MAX_FAILS));
                    finished = true;
                }
            }
        } catch (InterruptedException e) {
            logger.println("Polling was interrupted. Please contact your administrator if the interruption was not intentional.");
            if (pollerThread.isAlive()) {
                pollerThread.interrupt();
            }
        }
        return pollerThread.result;
    }

    /**
     * Prints some info about the release including an issue breakdown and pass/fail reason
     *
     * @param release release to print info for
     */
    private void printPassFail(PollingSummaryDTO release, int releaseId) {

        boolean isPassed = release.getPassFailStatus();

        if(release.getScanType() == 2)
            logger.println(String.format("DAST Scan with id : %d Completed", release.getScanId()));
        else if(release.getScanType() ==1)
            logger.println(String.format("Static Scan with id : %d Completed", release.getScanId()));
        else //fallback
            logger.println(String.format("Scan with id : %d Completed", release.getScanId()));

        if (release.getOpenSourceScanId() > 0) {
            if (release.getOpenSourceStatusId() == 2)
                logger.println(String.format("Open Source Scan with id : %d Completed", release.getOpenSourceScanId()));
            else
                logger.println(String.format("Open Source Scan with id : %d Cancelled/Failed", release.getOpenSourceScanId()));
        }
        logger.println(String.format("Critical: %d", release.getIssueCountCritical()));
        logger.println(String.format("High:     %d", release.getIssueCountHigh()));
        logger.println(String.format("Medium:   %d", release.getIssueCountMedium()));
        logger.println(String.format("Low:      %d", release.getIssueCountLow()));
        logger.println("For application status details see the customer portal: ");
        logger.println(String.format("%s/Redirect/Releases/%d", apiConnection.getBaseUrl(), releaseId));
        logger.println(String.format("Scan %s established policy check", isPassed ? "passed" : "failed"));
        if (!isPassed) {
            String passFailReason = release.getPassFailReasonType() == null ?
                    "Pass/Fail Policy requirements not met " :
                    release.getPassFailReasonType();
            logger.println("Failure Reason:         " + passFailReason);
        }
    }

    private void printCancelMessages(PollingSummaryDTO scanSummary, int releaseId) {
        if (scanSummary == null) {
            logger.println("Unable to retrieve scan summary data cancel reasons");
        } else {
            logger.println("-------Scan Cancelled------- ");
            logger.println();
            logger.println(String.format("Cancel reason:        %s", scanSummary.getAnalysisStatusReason()));
            logger.println(String.format("Cancel reason notes:  %s", scanSummary.getAnalysisStatusReasonNotes()));
            logger.println();
            logger.println("For application status details see the customer portal: ");
            logger.println(String.format("%s/Redirect/Releases/%d", apiConnection.getBaseUrl(), releaseId));
            logger.println();
        }
    }

    private void printPauseMessages(PollingSummaryDTO scanSummary) {
        if (scanSummary == null) {
            logger.println("Unable to retrieve scan summary data pause reasons");
        } else {
            logger.println("-------Scan Paused------- ");
            logger.println();
            // Leaving the for loop because of the data structure.
            // Should only be one object because a pause cancels the polling.
            for (PollingSummaryPauseDetail spd : scanSummary.getPauseDetails()) {
                logger.println(String.format("Pause reason:         %s", spd.getReason()));
                logger.println(String.format("Pause reason notes:   %s", spd.getNotes()));
                logger.println();
            }
        }
    }

    public void printScanSummary(ScanSummaryDTO scanSummaryDTO) {
        if (scanSummaryDTO == null) {
            logger.println("Unable to retrieve scan summary");
        } else {
            logger.println();
            logger.println("Scan Summary");
            logger.println(String.format("Application Name: %s", scanSummaryDTO.getApplicationName()));
            logger.println(String.format("Release Name: %s", scanSummaryDTO.getReleaseName()));
            logger.println(String.format("Release Id: %s", scanSummaryDTO.getReleaseId()));
            logger.println(String.format("Scan Id: %s", scanSummaryDTO.getScanId()));
            logger.println(String.format("Scan Type: %s", scanSummaryDTO.getScanType()));
            logger.println(String.format("Assessment Type Id: %s", scanSummaryDTO.getAssessmentTypeName()));
            logger.println(String.format("Analysis Status Type Id: %s", scanSummaryDTO.getAnalysisStatusType()));
            logger.println(String.format("Scan Started Date & Time: %s", scanSummaryDTO.getStartedDatetime()));
            logger.println(String.format("Scan Completed Date & Time: %s", scanSummaryDTO.getCompletedDateTime()));
            logger.println(String.format("Is False Positive Challenge Enabled: %s", scanSummaryDTO.getIsFalsePositiveChallenge()));
            logger.println(String.format("Is Remediation Scan: %s", scanSummaryDTO.getIsRemediationScan()));
            logger.println(String.format("Is Remediation Scan: %s", scanSummaryDTO.getIsRemediationScan()));
            logger.println(String.format("Entitlement Units Consumed: %s", scanSummaryDTO.getEntitlementUnitsConsumed()));
            logger.println(String.format("Star Rating: %s", scanSummaryDTO.getStarRating()));
            logger.println(String.format("Notes: %s", scanSummaryDTO.getNotes()));
            logger.println(String.format("Scan Cancel Reason: %s", scanSummaryDTO.getCancelReason()));
            logger.println(String.format("Scan Method Type Reason: %s", scanSummaryDTO.getScanMethodTypeName()));
            logger.println(String.format("Scan Tool Used: %s", scanSummaryDTO.getScanTool()));
            logger.println(String.format("Scan Tool Version: %s", scanSummaryDTO.getScanToolVersion()));
            logger.println();
            logger.println(String.format("Total Issues: %s", scanSummaryDTO.getTotalIssues()));
            logger.println(String.format("Total Critical Issues Count: %s", scanSummaryDTO.getIssueCountCritical()));
            logger.println(String.format("Total High Issues Count: %s", scanSummaryDTO.getIssueCountHigh()));
            logger.println(String.format("Total Medium Issues Count: %s", scanSummaryDTO.getIssueCountMedium()));
            logger.println(String.format("Total Low Issues Count: %s", scanSummaryDTO.getIssueCountLow()));
       }
    }

    public ScanSummaryDTO GetScanSummary(int releaseId, int scanId) throws IOException {
        ReleaseController releaseController = new ReleaseController(this.apiConnection, this.logger, "");

        return releaseController.getRelease(releaseId, scanId);
    }

}


