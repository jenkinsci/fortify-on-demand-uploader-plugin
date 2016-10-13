package org.jenkinsci.plugins.fodupload;

import org.jenkinsci.plugins.fodupload.models.JobConfigModel;
import org.jenkinsci.plugins.fodupload.models.response.LookupItemsModel;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.*;

public class PollStatus {
    private FodApi fodApi;
    private JobConfigModel jobModel;
    private int failCount = 0;
    private final int MAX_FAILS = 3;

    private List<LookupItemsModel> analysisStatusTypes = null;

    /**
     * Constructor
     * @param api api connection to use
     * @param jobModel fod api data
     */
    public PollStatus(FodApi api, final JobConfigModel jobModel) {
        fodApi = api;
        this.jobModel = jobModel;
    }

    /**
     * Polls the release status
     * @param releaseId release to poll
     * @return true if status is completed | cancelled.
     */
    public boolean releaseStatus(final int releaseId) {
        PrintStream logger = FodUploaderPlugin.getLogger();
        boolean finished = false; // default is failure

        try
        {
            while(!finished)
            {
                Thread.sleep(jobModel.getPollingInterval()*60*1000);
                // Get the status of the release
                ReleaseDTO release = fodApi.getReleaseController().getRelease(releaseId,
                        "currentAnalysisStatusTypeId,isPassed,passFailReasonId,critical,high,medium,low");
                if (release == null) {
                    failCount++;
                    continue;
                }

                int status = release.getCurrentAnalysisStatusTypeId();

                // Get the possible statuses only once
                if(analysisStatusTypes == null)
                    analysisStatusTypes = fodApi.getLookupItemsController().getLookupItems(APILookupItemTypes.AnalysisStatusTypes);

                if(failCount < MAX_FAILS)
                {
                    if (!fodApi.isAuthenticated()) {
                        fodApi.authenticate();
                    }

                    String statusString = "";

                    // Create a list of values that will be used to break the loop if found
                    // This way if any of this changes we don't need to redo the keys or something
                    List<String> complete = analysisStatusTypes.stream()
                            .filter(p -> p.getText().equals("Completed") || p.getText().equals("Canceled"))
                            .map(l -> l.getValue())
                            .collect(Collectors.toCollection(ArrayList::new));

                    // Look for and print the status OR break the loop.
                    for(LookupItemsModel o: analysisStatusTypes) {
                        if(o != null) {
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
                    if(finished)
                    {
                        printPassFail(release);
                    }
                }
                else
                {
                    logger.println("getStatus failed 3 consecutive times terminating polling");
                    finished = true;
                }
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return finished;
    }

    /**
     * Prints some info about the release including a vuln breakdown and pass/fail reason
     * @param release release to print info on
     */
    private void printPassFail(ReleaseDTO release) {
        PrintStream logger = FodUploaderPlugin.getLogger();
        try
        {
            // Break if release is null
            if (release == null) {
                this.failCount++;
                return;
            }
            boolean isPassed = release.isPassed();
            logger.println("Pass/Fail status:       " + (isPassed ? "Passed" : "Failed") );
            if (!jobModel.getDoPrettyLogOutput()) {
                if (!isPassed) {
                    String passFailReason = release.getPassFailReasonType() == null ?
                            "Pass/Fail Policy requirements not met " :
                            release.getPassFailReasonType();
                    logger.println("Failure Reason:         " + passFailReason);
                } else {
                    logger.println("Passed");
                }
                logger.println("Number of criticals:    " +  release.getCritical());
                logger.println("Number of highs:        " +  release.getHigh());
                logger.println("Number of mediums:      " +  release.getMedium());
                logger.println("Number of lows:         " +  release.getLow());

            } else {
                logger.println("------------------------------------------------------------------------------------");
                logger.println("                        Fortify on Demand Assessment Results                        ");
                logger.println("------------------------------------------------------------------------------------");
                logger.println();
                logger.println(String.format("Star Rating: %d out of 5 with %d total issue(s).",release.getRating(), release.getIssueCount()));
                logger.println();
                logger.println(String.format("Critical: %d", release.getCritical()));
                logger.println(String.format("High:     %d", release.getHigh()));
                logger.println(String.format("Medium:   %d", release.getMedium()));
                logger.println(String.format("Low:      %d", release.getLow()));
                logger.println();
                logger.println("For application status details see the customer portal: ");
                logger.println(fodApi.getBaseUrl() + "/Releases/" + release.getReleaseId() + "/Overview");
                logger.println();
                logger.println(String.format("Scan %s established policy check, marking build as %sstable.",
                        isPassed ? "passed" : "failed", isPassed ? "" : "un"));
                logger.println();
                logger.println("------------------------------------------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
