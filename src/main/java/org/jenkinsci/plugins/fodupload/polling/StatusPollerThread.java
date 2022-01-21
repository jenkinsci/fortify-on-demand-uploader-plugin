package org.jenkinsci.plugins.fodupload.polling;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.ReleaseController;
import org.jenkinsci.plugins.fodupload.models.AnalysisStatusTypeEnum;
import org.jenkinsci.plugins.fodupload.models.response.LookupItemsModel;
import org.jenkinsci.plugins.fodupload.models.response.PollingSummaryDTO;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

class StatusPollerThread extends Thread {
    public Boolean fail = false;
    public Boolean finished = false;
    public String statusString;
    public PollReleaseStatusResult result = new PollReleaseStatusResult();
    public PollingSummaryDTO pollingSummaryDTO = null;
    private PrintStream logger;
    private int releaseId;
    private int pollingInterval;
    private ReleaseController releaseController;
    private List<LookupItemsModel> analysisStatusTypes;
    private List<String> completeStatusList;
    private int scanId;


    StatusPollerThread(String name, final int releaseId, List<LookupItemsModel> analysisStatusTypes,
                       FodApiConnection apiConnection, List<String> completeStatusList, PrintStream logger, int pollingInterval, final int scanId, final String correlationId) {
        super(name);
        this.releaseId = releaseId;
        this.analysisStatusTypes = analysisStatusTypes;
        this.logger = logger;
        this.releaseController = new ReleaseController(apiConnection, logger, correlationId);
        this.completeStatusList = completeStatusList;
        this.pollingInterval = pollingInterval;
        this.scanId = scanId;
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
    @SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ") //Need to make string comparisons more concise on next release. See line 90 comments
    private void processScanRelease() {
        int status = -1;
        try {
            pollingSummaryDTO = releaseController.getReleaseByScanId(releaseId, scanId);
            
            if (pollingSummaryDTO == null) {
                fail = true;
                logger.println("Release data is not retrieved");
                return;
            }
    
            status = pollingSummaryDTO.getAnalysisStatusId();
        } catch (IOException e) {
            logger.println("Unable to retreive release data");
        }

        if (completeStatusList.contains(Integer.toString(status))) {
            finished = true;
        }
        for (LookupItemsModel o : analysisStatusTypes) {
            if (o != null) {
                int analysisStatusInt = Integer.parseInt(o.getValue());
                if (analysisStatusInt == status) {
                    this.statusString = o.getText().replace("_", " ");
                }
            } else {
                fail = true;
                return;
            }
        }
        if (this.statusString == null || this.statusString.equals(""))
        {
            fail = true;
        } else {
            if(statusString.equals(AnalysisStatusTypeEnum.InProgress.name()) || statusString.equals(AnalysisStatusTypeEnum.Queued.name()) || statusString.equals(AnalysisStatusTypeEnum.Scheduled.name())) {
                result.setScanInProgress(true);
                result.setPassing(true);
            }
            if (finished) {
                if(statusString.equals(AnalysisStatusTypeEnum.Completed.name())){
                    result.setPassing(pollingSummaryDTO.getPassFailStatus());
                }else {
                    result.setPassing(true);
                }
                result.setPollingSuccessful(true);
                if (!statusString.equals(AnalysisStatusTypeEnum.Waiting.name())) {
                        result.setFailReason(pollingSummaryDTO.getPassFailReasonType());
                }
            }
        }
    }
}
