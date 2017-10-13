package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.models.response.LookupItemsModel;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.APILookupItemTypes;

public class PollStatus {

    private final static int MAX_FAILS = 3;

    private FodApi fodApi;
    private int failCount = 0;
    private int pollingInterval;
    private boolean isPrettyLogging;

    private List<LookupItemsModel> analysisStatusTypes = null;

    /**
     * Constructor
     *
     * @param api             api connection to use
     * @param isPrettyLogging enables fancier formatting for logs
     * @param pollingInterval the polling interval in ???
     */
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    public PollStatus(FodApi api, boolean isPrettyLogging, int pollingInterval) {
        this.fodApi = api;
        this.pollingInterval = pollingInterval;
        this.isPrettyLogging = isPrettyLogging;
    }

    /**
     * Polls the release status
     *
     * @param releaseId release to poll
     * @return true if status is completed | cancelled.
     */
    public boolean releaseStatus(final int releaseId) throws IOException, InterruptedException {
        PrintStream logger = StaticAssessmentBuildStep.getLogger();
        boolean finished = false; // default is failure

        while (!finished) {
            Thread.sleep(1000L * 60 * 1); // TODO: Use the interval here
            // Get the status of the release
            ReleaseDTO release = fodApi.getReleaseController().getRelease(releaseId,
                    "currentAnalysisStatusTypeId,isPassed,passFailReasonId,critical,high,medium,low");
            if (release == null) {
                failCount++;
                continue;
            }

            int status = release.getCurrentAnalysisStatusTypeId();

            // Get the possible statuses only once
            if (analysisStatusTypes == null)
                analysisStatusTypes = fodApi.getLookupItemsController().getLookupItems(APILookupItemTypes.AnalysisStatusTypes);

            if (failCount < MAX_FAILS) {
                String statusString = "";

                // Create a list of values that will be used to break the loop if found
                // This way if any of this changes we don't need to redo the keys or something
                List<String> complete = new ArrayList<>();

                for (LookupItemsModel item : analysisStatusTypes) {
                    if (item.getText().equalsIgnoreCase("Completed") || item.getText().equalsIgnoreCase(("Canceled")))
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
                logger.println("Status: " + statusString);
                if (finished) {
                    printPassFail(release);
                }
            } else {
                logger.println("getStatus failed 3 consecutive times terminating polling");
                finished = true;
            }
        }

        return finished;
    }

    /**
     * Prints some info about the release including a vuln breakdown and pass/fail reason
     *
     * @param release release to print info on
     */
    private void printPassFail(ReleaseDTO release) {
        PrintStream logger = StaticAssessmentBuildStep.getLogger();
        try {
            // Break if release is null
            if (release == null) {
                this.failCount++;
                return;
            }
            boolean isPassed = release.isPassed();
            logger.println("Pass/Fail status:       " + (isPassed ? "Passed" : "Failed"));
            if (this.isPrettyLogging) {
                if (!isPassed) {
                    String passFailReason = release.getPassFailReasonType() == null ?
                            "Pass/Fail Policy requirements not met " :
                            release.getPassFailReasonType();
                    logger.println("Failure Reason:         " + passFailReason);
                } else {
                    logger.println("Passed");
                }
                logger.println("Number of criticals:    " + release.getCritical());
                logger.println("Number of highs:        " + release.getHigh());
                logger.println("Number of mediums:      " + release.getMedium());
                logger.println("Number of lows:         " + release.getLow());

            } else {
                logger.println("------------------------------------------------------------------------------------");
                logger.println("                        Fortify on Demand Assessment Results                        ");
                logger.println("------------------------------------------------------------------------------------");
                logger.println();
                logger.println(String.format("Star Rating: %d out of 5 with %d total issue(s).", release.getRating(), release.getIssueCount()));
                logger.println();
                logger.println(String.format("Critical: %d", release.getCritical()));
                logger.println(String.format("High:     %d", release.getHigh()));
                logger.println(String.format("Medium:   %d", release.getMedium()));
                logger.println(String.format("Low:      %d", release.getLow()));
                logger.println();
                logger.println("For application status details see the customer portal: ");
                logger.println(String.format("%s/Redirect/Releases/%d", fodApi.getBaseUrl(), release.getReleaseId()));
                logger.println();
                logger.println(String.format("Scan %s established policy check, marking build as %s.",
                        isPassed ? "passed" : "failed", isPassed ? "stable" : "unstable"));
                logger.println();
                logger.println("------------------------------------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
