package org.jenkinsci.plugins.fodupload.polling;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.controllers.LookupItemsController;
import org.jenkinsci.plugins.fodupload.controllers.ReleaseController;
import org.jenkinsci.plugins.fodupload.controllers.StaticScanSummaryController;
import org.jenkinsci.plugins.fodupload.models.AnalysisStatusTypeEnum;
import org.jenkinsci.plugins.fodupload.models.response.LookupItemsModel;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;
import org.jenkinsci.plugins.fodupload.models.response.ScanPauseDetail;
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
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public ScanStatusPoller(FodApiConnection apiConnection, int pollingInterval, PrintStream logger) {
        this.apiConnection = apiConnection;
        this.pollingInterval = pollingInterval;
        this.logger = logger;
    }

    /**
     * Polls the release status
     *
     * @param releaseId release to poll
     * @return true if status is completed | cancelled.
     * @throws java.io.IOException in certain cases
     * @throws InterruptedException in certain cases
     */
    public PollReleaseStatusResult pollReleaseStatus(final int releaseId) throws IOException, InterruptedException {

        LookupItemsController lookupItemsController = new LookupItemsController(this.apiConnection);
        ReleaseController releaseController = new ReleaseController(this.apiConnection);
        StaticScanSummaryController scanSummaryController = new StaticScanSummaryController(this.apiConnection, logger);
        PollReleaseStatusResult result = new PollReleaseStatusResult();
        List<LookupItemsModel> analysisStatusTypes = null;

        logger.println("Begin polling Fortify on Demand for results.");

        boolean finished = false;
        int counter = 0;

        while (!finished) {

            // don't sleep the first round
            if (counter != 0)
                Thread.sleep(1000L * 60 * this.pollingInterval);
            counter++;

            // Get the status of the release
            // The string passed in is a field filter. 
            // This will pull back a subset of the fields available at /api/v3/releases/{releaseId}/scans/{scanId}
            ReleaseDTO release = releaseController.getRelease(releaseId,
                    "currentAnalysisStatusTypeId,isPassed,passFailReasonTypeId,passFailReasonType,critical,high,medium,low,releaseId,rating,currentStaticScanId,releaseName");

            if (release == null) {
                failCount++;
                continue;
            }

            int status = release.getCurrentAnalysisStatusTypeId();

            // Get the possible statuses only once
            if (analysisStatusTypes == null)
                analysisStatusTypes = lookupItemsController.getLookupItems(APILookupItemTypes.AnalysisStatusTypes);

            if (failCount < MAX_FAILS) {
                String statusString = "";

                // Create a list of values that will be used to break the loop if found
                // This way if any of this changes we don't need to redo the keys or something
                List<String> complete = new ArrayList<>();

                for (LookupItemsModel item : analysisStatusTypes) {
                    if (item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Completed.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Canceled.name()))
                        complete.add(item.getValue());
                }

                // Look for and print the status OR break the loop.
                for (LookupItemsModel o : analysisStatusTypes) {
                    if (o != null) {
                        int analysisStatus = Integer.parseInt(o.getValue());
                        if (analysisStatus == status) {
                            statusString = o.getText().replace("_", " ");
                        }
                        if (complete.contains(Integer.toString(status))) {
                            finished = true;
                        }
                    }
                }

                logger.println(counter + ") Poll Status: " + statusString);


                if (statusString.equals(AnalysisStatusTypeEnum.Waiting.name())) {
                    ScanSummaryDTO scanSummaryDTO = scanSummaryController.getReleaseScanSummary(release.getReleaseId(), release.getCurrentStaticScanId());
                    if (scanSummaryDTO.getPauseDetails() != null)
                        printPauseMessages(scanSummaryDTO);
                }
                if (finished) {
                    result.setPassing(release.isPassed());
                    result.setPollingSuccessful(true);

                    if (!Utils.isNullOrEmpty(release.getPassFailReasonType()))
                        result.setFailReason(release.getPassFailReasonType());

                    if (statusString.equals(AnalysisStatusTypeEnum.Canceled.name())) {
                        ScanSummaryDTO scanSummaryDTO = scanSummaryController.getReleaseScanSummary(release.getReleaseId(), release.getCurrentStaticScanId());
                        printCancelMessages(scanSummaryDTO);
                    } else {
                        printPassFail(release);
                    }
                }
            } else {
                logger.println(String.format("Polling Failed %d times.  Terminating", MAX_FAILS));
                finished = true;
            }
        }

        return result;
    }

    /**
     * Prints some info about the release including an issue breakdown and pass/fail reason
     *
     * @param release release to print info for
     */
    private void printPassFail(ReleaseDTO release) {

        boolean isPassed = release.isPassed();
        logger.println(String.format("Critical: %d", release.getCritical()));
        logger.println(String.format("High:     %d", release.getHigh()));
        logger.println(String.format("Medium:   %d", release.getMedium()));
        logger.println(String.format("Low:      %d", release.getLow()));
        logger.println("For application status details see the customer portal: ");
        logger.println(String.format("%s/Redirect/Releases/%d", apiConnection.getBaseUrl(), release.getReleaseId()));
        logger.println(String.format("Scan %s established policy check", isPassed ? "passed" : "failed"));
        if (!isPassed) {
            String passFailReason = release.getPassFailReasonType() == null ?
                    "Pass/Fail Policy requirements not met " :
                    release.getPassFailReasonType();
            logger.println("Failure Reason:         " + passFailReason);
        }
    }

    private void printCancelMessages(ScanSummaryDTO scanSummary) {
        logger.println("-------Scan Cancelled------- ");
        logger.println();
        logger.println(String.format("Cancel reason:        %s", scanSummary.getCancelReason()));
        logger.println(String.format("Cancel reason notes:  %s", scanSummary.getAnalysisStatusReasonNotes()));
        logger.println();
        logger.println("For application status details see the customer portal: ");
        logger.println(String.format("%s/Redirect/Releases/%d", apiConnection.getBaseUrl(), scanSummary.getReleaseId()));
        logger.println();
    }

    private void printPauseMessages(ScanSummaryDTO scanSummary) {
        logger.println("-------Scan Paused------- ");
        logger.println();
        logger.println("Review the last pause entry below");
        logger.println();
        for (ScanPauseDetail spd : scanSummary.getPauseDetails()) {
            logger.println(String.format("Pause reason:         %s", spd.getReason()));
            logger.println(String.format("Pause reason notes:   %s", spd.getNotes()));
            logger.println();
        }
    }
}
