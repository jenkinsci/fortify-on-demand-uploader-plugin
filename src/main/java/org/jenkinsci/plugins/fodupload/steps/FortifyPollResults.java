package org.jenkinsci.plugins.fodupload.steps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.SharedPollingBuildStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.kohsuke.stapler.AncestorInPath;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;

import org.kohsuke.stapler.verb.POST;

@SuppressFBWarnings("unused")
public class FortifyPollResults extends FortifyStep {

    private String releaseId;
    private String bsiToken;
    private int pollingInterval;

    private boolean overrideGlobalConfig;
    private int policyFailureBuildResultPreference;
    private String clientId;
    private String clientSecret;
    private String username;
    private String personalAccessToken;
    private String tenantId;

    private SharedPollingBuildStep commonBuildStep;

    @DataBoundConstructor
    public FortifyPollResults(String releaseId, String bsiToken, int pollingInterval) {
        super();
        this.releaseId = releaseId != null ? releaseId.trim() : "";
        this.bsiToken = bsiToken != null ? bsiToken.trim() : "";
        this.pollingInterval = pollingInterval;
    }

    public String getReleaseId() { return this.releaseId; }

    public String getBsiToken() {
        return this.bsiToken;
    }

    public int getPollingInterval() {
        return this.pollingInterval;
    }

    public boolean getOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    @DataBoundSetter
    public void setOverrideGlobalConfig(boolean overrideGlobalConfig) {
        this.overrideGlobalConfig = overrideGlobalConfig;
    }

    public int getPolicyFailureBuildResultPreference() {
        return policyFailureBuildResultPreference;
    }

    @DataBoundSetter
    public void setPolicyFailureBuildResultPreference(int policyFailureBuildResultPreference) {
        this.policyFailureBuildResultPreference = policyFailureBuildResultPreference;
    }

    public String getClientId() {
        return clientId;
    }

    @DataBoundSetter
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    @DataBoundSetter
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    @DataBoundSetter
    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    @DataBoundSetter
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Poll Results PreBuild Running...");
        commonBuildStep = new SharedPollingBuildStep(releaseId,
                bsiToken,
                overrideGlobalConfig,
                pollingInterval,
                policyFailureBuildResultPreference,
                clientId,
                clientSecret,
                username,
                personalAccessToken,
                tenantId);

        return true;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new FortifyPollResults.Execution(this, context);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Poll Results Running...");
        commonBuildStep = new SharedPollingBuildStep(releaseId,
                bsiToken,
                overrideGlobalConfig,
                pollingInterval,
                policyFailureBuildResultPreference,
                clientId,
                clientSecret,
                username,
                personalAccessToken,
                tenantId);
// If the CrossBuildAction fails to save during the upload step, the polling fails semi-gracefully.
        if(build.getAction(CrossBuildAction.class) != null && build.getAction(CrossBuildAction.class).allowPolling()) {
            commonBuildStep.setUploadScanId(build.getAction(CrossBuildAction.class).currentScanId());
            commonBuildStep.perform(build, workspace, launcher, listener);
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getDisplayName() {
            return "Poll Fortify on Demand for Results";
        }

        @Override
        public String getFunctionName() {
            return "fodPollResults";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(SharedPollingBuildStep.USERNAME) final String username,
                                                                  @QueryParameter(SharedPollingBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                  @QueryParameter(SharedPollingBuildStep.TENANT_ID) final String tenantId,
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

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
        private transient FortifyPollResults polling;

        protected Execution(FortifyPollResults polling, StepContext context) {
            super(context);
            this.polling = polling;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running fodPollResults step");
            polling.perform(getContext().get(Run.class), getContext().get(FilePath.class),
                    getContext().get(Launcher.class), getContext().get(TaskListener.class));

            return null;
        }
    }
}
