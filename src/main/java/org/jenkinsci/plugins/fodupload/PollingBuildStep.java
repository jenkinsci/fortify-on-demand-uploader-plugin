package org.jenkinsci.plugins.fodupload;

import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.polling.PollReleaseStatusResult;
import org.jenkinsci.plugins.fodupload.polling.ScanStatusPoller;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class PollingBuildStep extends Recorder implements SimpleBuildStep {

    private static final BsiTokenParser tokenParser = new BsiTokenParser();

    private String bsiToken;
    private int pollingInterval;
    private int policyFailureBuildResultPreference;

    @DataBoundConstructor
    public PollingBuildStep(String bsiToken,
                            int pollingInterval,
                            int policyFailureBuildResultPreference) {

        this.bsiToken = bsiToken;
        this.pollingInterval = pollingInterval;
        this.policyFailureBuildResultPreference = policyFailureBuildResultPreference;
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
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

        if (this.getPollingInterval() <= 0) {
            logger.println("Error: Invalid polling interval (" + this.getPollingInterval() + " minutes)");
            run.setResult(Result.UNSTABLE);
        }

        FodApiConnection apiConnection = GlobalConfiguration.all().get(FodGlobalDescriptor.class).createFodApiConnection();

        try {

            BsiToken token = tokenParser.parse(this.bsiToken);
            apiConnection.authenticate();
            ScanStatusPoller poller = new ScanStatusPoller(apiConnection, this.pollingInterval, logger);
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

    @SuppressWarnings("unused")
    public String getBsiToken() {
        return bsiToken;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getPollingInterval() {
        return pollingInterval;
    }

    @SuppressWarnings("unused")
    public int getPolicyFailureBuildResultPreference() {
        return this.policyFailureBuildResultPreference;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public PollingStepDescriptor getDescriptor() {
        return (PollingStepDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class PollingStepDescriptor extends BuildStepDescriptor<Publisher> {

        public PollingStepDescriptor() {
            super();
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Poll Fortify on Demand for Results";
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPolicyFailureBuildResultPreferenceItems() {
            ListBoxModel items = new ListBoxModel();
            for (PollingBuildStep.PolicyFailureBuildResultPreference preferenceType : PollingBuildStep.PolicyFailureBuildResultPreference.values()) {
                items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.getValue())));
            }

            return items;
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
