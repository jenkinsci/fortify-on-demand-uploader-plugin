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
     * @throws java.io.IOException  in certain cases
     * @throws InterruptedException in certain cases
     */
    public PollReleaseStatusResult pollReleaseStatus(final int releaseId) throws IOException, InterruptedException {


        logger.println("Begin polling Fortify on Demand for results.");

        boolean finished = false;
        int counter = 1;
        LookupItemsController lookupItemsController = new LookupItemsController(this.apiConnection);
        List<LookupItemsModel> analysisStatusTypes =  lookupItemsController.getLookupItems(APILookupItemTypes.AnalysisStatusTypes);
        //List<StatusPollerThread> pollerThreads = new ArrayList<StatusPollerThread>();
        StatusPollerThread pollerThread = null;

        // Create a list of values that will be used to break the loop if found
        // This way if any of this changes we don't need to redo the keys or something
        List<String> complete = new ArrayList<>();
        
        if (analysisStatusTypes != null) {
            for (LookupItemsModel item : analysisStatusTypes) {
                if (item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Completed.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Canceled.name()))
                    complete.add(item.getValue());
            }
        }
        
        try{
            while (!finished) {
                if (analysisStatusTypes == null) {
                    analysisStatusTypes = lookupItemsController.getLookupItems(APILookupItemTypes.AnalysisStatusTypes);
                    complete = new ArrayList<>();
                    for (LookupItemsModel item : analysisStatusTypes) {
                        if (item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Completed.name()) || item.getText().equalsIgnoreCase(AnalysisStatusTypeEnum.Canceled.name()))
                            complete.add(item.getValue());
                    }
                }
                if (counter == 1) {
                    //No Thread.sleep() on first round
                    pollerThread = new StatusPollerThread(String.valueOf(counter), releaseId, analysisStatusTypes, apiConnection, complete, logger, 0);
                } else {
                    pollerThread = new StatusPollerThread(String.valueOf(counter), releaseId, analysisStatusTypes, apiConnection, complete, logger, pollingInterval);
                }

                pollerThread.start();
                pollerThread.join();
                if (pollerThread.fail) {
                    failCount++;
                    continue;
                }

                if (failCount < MAX_FAILS) {
                    if(!pollerThread.fail)
                    {
                        failCount = 0;
                        logger.println(pollerThread.getName() + ") Poll Status: " + pollerThread.statusString);

                        if (pollerThread.statusString.equals(AnalysisStatusTypeEnum.Waiting.name()) && pollerThread.scanSummaryDTO.getPauseDetails() != null)
                            printPauseMessages(pollerThread.scanSummaryDTO);
                        if (pollerThread.finished) {
                            finished = pollerThread.finished;
                            if (pollerThread.statusString.equals(AnalysisStatusTypeEnum.Canceled.name())) {
                                printCancelMessages(pollerThread.scanSummaryDTO);
                            } else if (pollerThread.statusString.equals(AnalysisStatusTypeEnum.Completed.name())) {
                                printPassFail(pollerThread.releaseDTO);
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
            if(pollerThread.isAlive()){
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
        if (scanSummary == null) {
            logger.println("Unable to retrieve scan summary data cancel reasons");
        } else {
            logger.println("-------Scan Cancelled------- ");
            logger.println();
            logger.println(String.format("Cancel reason:        %s", scanSummary.getCancelReason()));
            logger.println(String.format("Cancel reason notes:  %s", scanSummary.getAnalysisStatusReasonNotes()));
            logger.println();
            logger.println("For application status details see the customer portal: ");
            logger.println(String.format("%s/Redirect/Releases/%d", apiConnection.getBaseUrl(), scanSummary.getReleaseId()));
            logger.println();
        }
    }

    private void printPauseMessages(ScanSummaryDTO scanSummary) {
        if (scanSummary == null) {
            logger.println("Unable to retrieve scan summary data pause reasons");
        } else {
            logger.println("-------Scan Paused------- ");
            logger.println();
            // Leaving the for loop because of the data structure.
            // Should only be one object because a pause cancels the polling.
            for (ScanPauseDetail spd : scanSummary.getPauseDetails()) {
                logger.println(String.format("Pause reason:         %s", spd.getReason()));
                logger.println(String.format("Pause reason notes:   %s", spd.getNotes()));
                logger.println();
            }
        }
    }
}

class StatusPollerThread extends Thread {
    public Boolean fail = false;
    public Boolean finished = false;
    public String statusString;
    public PollReleaseStatusResult result = new PollReleaseStatusResult();
    public ScanSummaryDTO scanSummaryDTO = null;
    public ReleaseDTO releaseDTO = null;
    private PrintStream logger;
    private int releaseId;
    private int pollingInterval;
    private ReleaseController releaseController;
    private StaticScanSummaryController scanSummaryController;
    private List<LookupItemsModel> analysisStatusTypes;
    private List<String> completeStatusList;


    StatusPollerThread(String name, final int releaseId, List<LookupItemsModel> analysisStatusTypes,
                       FodApiConnection apiConnection, List<String> completeStatusList, PrintStream logger, int pollingInterval) {
        super(name);
        this.releaseId = releaseId;
        this.analysisStatusTypes = analysisStatusTypes;
        this.logger = logger;
        this.releaseController = new ReleaseController(apiConnection);
        this.scanSummaryController = new StaticScanSummaryController(apiConnection, logger);
        this.completeStatusList = completeStatusList;
        this.pollingInterval = pollingInterval;
    }

    public void run() {
        try {
            Thread.sleep(1000L * 60 * this.pollingInterval);
            processScanRelease();
        } catch (InterruptedException e) {
            logger.println("API call to retrieve scan status was terminated. Please contact your system adminstrator if termination was not intentional");
            Thread.currentThread().interrupt();
        }
    }

    private void processScanRelease() {
        int status = -1;
        try {
            releaseDTO = releaseController.getRelease(releaseId,
                    "currentAnalysisStatusTypeId,isPassed,passFailReasonTypeId,passFailReasonType,critical,high,medium,low,releaseId,rating,currentStaticScanId,releaseName");

            status = releaseDTO.getCurrentAnalysisStatusTypeId();
        } catch (IOException e) {
            logger.println("Unable to retreive release data");
        }

        if (releaseDTO == null) {
            fail = true;
            logger.println("Release data is not retrieved");
        }


        // Look for and print the status OR break the loop.
        for (LookupItemsModel o : analysisStatusTypes) {
            if (o != null) {
                int analysisStatusInt = Integer.parseInt(o.getValue());
                if (analysisStatusInt == status) {
                    this.statusString = o.getText().replace("_", " ");
                }
                if (completeStatusList.contains(Integer.toString(status))) {
                    finished = true;
                }
            } else {
                fail = true;
            }
        }
        if (this.statusString == null || this.statusString == "")
        {
            fail = true;
        } else {
            if (statusString.equals(AnalysisStatusTypeEnum.Waiting.name())) {
                try {
                    scanSummaryDTO = scanSummaryController.getReleaseScanSummary(releaseDTO.getReleaseId(), releaseDTO.getCurrentStaticScanId());
                    finished = true;
                } catch (IOException e) {
                    logger.println("Unable to retrieve scan summary data. Error: " + e.toString());
                    fail = true;
                }
            }
            if (finished) {
                result.setPassing(releaseDTO.isPassed());
                result.setPollingSuccessful(true);

                if (!Utils.isNullOrEmpty(releaseDTO.getPassFailReasonType()))
                    result.setFailReason(releaseDTO.getPassFailReasonType());

                if (statusString.equals(AnalysisStatusTypeEnum.Canceled.name())) {
                    try {
                        scanSummaryDTO = scanSummaryController.getReleaseScanSummary(releaseDTO.getReleaseId(), releaseDTO.getCurrentStaticScanId());
                    } catch (IOException e) {
                        logger.println("Unable to retrieve scan summary data. Error: " + e.toString());
                        fail = true;
                    }
                }
            }
        }
    }
}
