package org.jenkinsci.plugins.fodupload;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.models.BsiUrl;
import org.jenkinsci.plugins.fodupload.polling.PollReleaseStatusResult;
import org.jenkinsci.plugins.fodupload.polling.ScanStatusPoller;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class PollingBuildStep extends Recorder implements SimpleBuildStep {

    private String bsiUrl;
    private int pollingInterval;
    private int policyFailureBuildResultPreference;
    private boolean isPrettyLogging;

    @DataBoundConstructor
    public PollingBuildStep(String bsiUrl,
                            int pollingInterval,
                            int policyFailureBuildResultPreference,
                            boolean isPrettyLogging) {

        this.bsiUrl = bsiUrl;
        this.pollingInterval = pollingInterval;
        this.policyFailureBuildResultPreference = policyFailureBuildResultPreference;
        this.isPrettyLogging = isPrettyLogging;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        final PrintStream logger = taskListener.getLogger();

        Result currentResult = run.getResult();
        if (Result.FAILURE.equals(currentResult)
                || Result.ABORTED.equals(currentResult)
                || Result.UNSTABLE.equals(currentResult)) {

            logger.println("Error: Build Failed or Unstable.  No reason to poll Fortify on Demand for results.");
            return;
        }

        FodApiConnection apiConnection = getDescriptor().createFodApiConnection();

        try {
            BsiUrl token = new BsiUrl(this.bsiUrl);
            if (this.getPollingInterval() > 0) {
                ScanStatusPoller poller = new ScanStatusPoller(apiConnection, this.isPrettyLogging, this.pollingInterval, logger);
                PollReleaseStatusResult result = poller.pollReleaseStatus(token.getProjectVersionId());

                // if the polling fails, crash the build
                if (!result.isPollingSuccessful()) {
                    run.setResult(Result.FAILURE);
                    return;
                }

                if (!result.isPassing()) {

                    PolicyFailureBuildResultPreference pref = PolicyFailureBuildResultPreference.fromInt(this.policyFailureBuildResultPreference);

                    switch (pref) {

                        case MarkFailure:
                            run.setResult(Result.FAILURE);
                            break;

                        case MarkUnstable:
                            run.setResult(Result.UNSTABLE);
                            break;

                        case None:
                        default:
                            break;
                    }
                }

            }
        } catch (URISyntaxException e) {
            logger.println("Failed to parse BSI.");
            e.printStackTrace(logger);
        } finally {
            if (apiConnection != null) {
                apiConnection.retireToken();
            }
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public String getBsiUrl() {
        return bsiUrl;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public int getPolicyFailureBuildResultPreference() {
        return this.policyFailureBuildResultPreference;
    }

    public boolean getIsPrettyLogging() {
        return isPrettyLogging;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public PollingStepDescriptor getDescriptor() {
        return (PollingStepDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class PollingStepDescriptor extends FodDescriptor {

        public PollingStepDescriptor() {
            super();
            load();
        }

        @Override
        public String getDisplayName() {
            return "Poll Fortify on Demand for Results";
        }
    }

    public enum PolicyFailureBuildResultPreference {
        None(0),
        MarkUnstable(1),
        MarkFailure(2);

        private final int _val;

        PolicyFailureBuildResultPreference(int val) {
            this._val = val;
        }

        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 2:
                    return "Mark Failure";
                case 1:
                    return "Mark Unstable";
                case 0:
                default:
                    return "Do nothing";
            }
        }

        public static PolicyFailureBuildResultPreference fromInt(int val) {
            switch (val) {
                case 2:
                    return MarkFailure;
                case 1:
                    return MarkUnstable;
                case 0:
                default:
                    return None;
            }
        }
    }
}
