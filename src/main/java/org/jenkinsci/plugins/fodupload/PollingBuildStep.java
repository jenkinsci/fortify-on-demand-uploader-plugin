package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.polling.PollReleaseStatusResult;
import org.jenkinsci.plugins.fodupload.polling.ScanStatusPoller;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import static org.jenkinsci.plugins.fodupload.SharedPollingBuildStep.*;
import org.kohsuke.stapler.AncestorInPath;

@SuppressWarnings("unused")
public class PollingBuildStep extends Recorder implements SimpleBuildStep {

    SharedPollingBuildStep sharedBuildStep;

    @DataBoundConstructor
    public PollingBuildStep(String releaseId,
                            String bsiToken,
                            boolean overrideGlobalConfig,
                            int pollingInterval,
                            int policyFailureBuildResultPreference,
                            String clientId,
                            String clientSecret,
                            String username,
                            String personalAccessToken,
                            String tenantId) {

        sharedBuildStep = new SharedPollingBuildStep(releaseId, bsiToken,
                overrideGlobalConfig, pollingInterval,
                policyFailureBuildResultPreference, clientId, clientSecret,
                username, personalAccessToken, tenantId);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        // If the CrossBuildAction fails to save during the upload step, the polling fails semi-gracefully.
        if(run.getAction(CrossBuildAction.class) != null && run.getAction(CrossBuildAction.class).allowPolling()) {
            sharedBuildStep.setUploadScanId(run.getAction(CrossBuildAction.class).currentScanId());
            sharedBuildStep.setCorrelationId(run.getAction(CrossBuildAction.class).currentCorrelationId());
            sharedBuildStep.perform(run, filePath, launcher, taskListener);
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @SuppressWarnings("unused")
    public String getReleaseId() {
        return sharedBuildStep.getReleaseId();
    }

    @SuppressWarnings("unused")
    public String getBsiToken() {
        return sharedBuildStep.getBsiToken();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getPollingInterval() {
        return sharedBuildStep.getPollingInterval();
    }

    @SuppressWarnings("unused")
    public int getPolicyFailureBuildResultPreference() {
        return sharedBuildStep.getPolicyFailureBuildResultPreference();
    }

    @SuppressWarnings("unused")
    public String getUsername() {
        return sharedBuildStep.getAuthModel().getUsername();
    }

    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        return sharedBuildStep.getAuthModel().getPersonalAccessToken();
    }

    @SuppressWarnings("unused")
    public String getTenantId() {
        return sharedBuildStep.getAuthModel().getTenantId();
    }

    @SuppressWarnings("unused")
    public boolean getOverrideGlobalConfig() {
        return sharedBuildStep.getAuthModel().getOverrideGlobalConfig();
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

        public FormValidation doCheckReleaseId(@QueryParameter String releaseId, @QueryParameter String bsiToken) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return SharedPollingBuildStep.doCheckReleaseId(releaseId, bsiToken);
        }

        public FormValidation doCheckBsiToken(@QueryParameter String bsiToken, @QueryParameter String releaseId) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return SharedPollingBuildStep.doCheckBsiToken(bsiToken, releaseId);
        }

        public FormValidation doCheckPollingInterval(@QueryParameter String pollingInterval) {
            return SharedPollingBuildStep.doCheckPollingInterval(pollingInterval);
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(USERNAME) final String username,
                                                                  @QueryParameter(PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                  @QueryParameter(TENANT_ID) final String tenantId,
                                                                  @AncestorInPath Job job) {
            job.checkPermission(Item.CONFIGURE);
            return SharedPollingBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPolicyFailureBuildResultPreferenceItems() {
            return SharedPollingBuildStep.doFillPolicyFailureBuildResultPreferenceItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillUsernameItems(@AncestorInPath Job job) {
            return SharedPollingBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPersonalAccessTokenItems(@AncestorInPath Job job) {
            return SharedPollingBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTenantIdItems(@AncestorInPath Job job) {
            return SharedPollingBuildStep.doFillStringCredentialsItems(job);
        }
    }
}
