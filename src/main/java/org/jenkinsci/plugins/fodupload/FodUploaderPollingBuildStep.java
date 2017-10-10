package org.jenkinsci.plugins.fodupload;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.models.BsiUrl;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class FodUploaderPollingBuildStep extends Recorder implements SimpleBuildStep {

    private String bsiUrl;
    private int pollingInterval;
    private boolean isPrettyLogging;

    @DataBoundConstructor
    public FodUploaderPollingBuildStep(String bsiUrl,
                                       int pollingInterval,
                                       boolean isPrettyLogging) {

        this.bsiUrl = bsiUrl;
        this.pollingInterval = pollingInterval;
        this.isPrettyLogging = isPrettyLogging;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        final PrintStream logger = taskListener.getLogger();
        FodApi api = getDescriptor().createFodApi();

        try {
            BsiUrl token = new BsiUrl(this.bsiUrl);
            if (this.getPollingInterval() > 0) {
                PollStatus poller = new PollStatus(api, this.isPrettyLogging, this.pollingInterval);
                boolean success = poller.releaseStatus(token.getProjectVersionId());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
}
