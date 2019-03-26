package org.jenkinsci.plugins.fodupload.steps;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.fodupload.SharedPollingBuildStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

public class FortifyPollResults extends FortifyStep {

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
    public FortifyPollResults(String bsiToken, int pollingInterval) {
        super();
        this.bsiToken = bsiToken != null ? bsiToken.trim() : "";
        this.pollingInterval = pollingInterval;
    }

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
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Poll Results PreBuild Running...");
        commonBuildStep = new SharedPollingBuildStep(bsiToken,
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
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Poll Results Running...");
        commonBuildStep = new SharedPollingBuildStep(bsiToken,
                overrideGlobalConfig,
                pollingInterval,
                policyFailureBuildResultPreference,
                clientId,
                clientSecret,
                username,
                personalAccessToken,
                tenantId);

        commonBuildStep.perform(build, workspace, launcher, listener);
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
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(SharedPollingBuildStep.USERNAME) final String username,
                                                                  @QueryParameter(SharedPollingBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                  @QueryParameter(SharedPollingBuildStep.TENANT_ID) final String tenantId)
        {
            return SharedPollingBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId);

        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPolicyFailureBuildResultPreferenceItems() {
            return SharedPollingBuildStep.doFillPolicyFailureBuildResultPreferenceItems();
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
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

        private static final long serialVersionUID = 1L;
    }
}
