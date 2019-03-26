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
import org.jenkinsci.plugins.fodupload.SharedUploadBuildStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.PrintStream;
import java.util.Set;


public class FortifyStaticAssessment extends FortifyStep {

    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();

    private String bsiToken;

    private boolean overrideGlobalConfig;
    private String username;
    private String personalAccessToken;
    private String tenantId;
    private boolean includeAllFiles;
    private boolean isBundledAssessment;
    private boolean purchaseEntitlements;
    private int entitlementPreference;
    private boolean isRemediationPreferred;
    private boolean runOpenSourceAnalysisOverride;
    private boolean isExpressScanOverride;
    private boolean isExpressAuditOverride;
    private boolean includeThirdPartyOverride;

    private SharedUploadBuildStep commonBuildStep;

    @DataBoundConstructor
    public FortifyStaticAssessment(String bsiToken) {
        super();
        this.bsiToken = bsiToken != null ? bsiToken.trim() : "";
    }

    public String getBsiToken() {
        return bsiToken;
    }

    @DataBoundSetter
    public void setOverrideGlobalConfig(boolean overrideGlobalConfig) {
        this.overrideGlobalConfig = overrideGlobalConfig;
    }

    public boolean getOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    @DataBoundSetter
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    @DataBoundSetter
    public void setIncludeAllFiles(boolean includeAllFiles) {
        this.includeAllFiles = includeAllFiles;
    }

    public boolean getIncludeAllFiles() {
        return includeAllFiles;
    }

    @DataBoundSetter
    public void setIsBundledAssessment(boolean isBundledAssessment) {
        this.isBundledAssessment = isBundledAssessment;
    }
    public boolean getIsBundledAssessment() {
        return isBundledAssessment;
    }


    @DataBoundSetter
    public void setPurchaseEntitlements(boolean purchaseEntitlements) {
        this.purchaseEntitlements = purchaseEntitlements;
    }
    public boolean getPurchaseEntitlements() {
        return purchaseEntitlements;
    }

    @DataBoundSetter
    public void setEntitlementPreference(int entitlementPreference) {
        this.entitlementPreference = entitlementPreference;
    }
    public int getEntitlementPreference() {
        return entitlementPreference;
    }

    @DataBoundSetter
    public void setIsRemediationPreferred(boolean isRemediationPreferred) {
        this.isRemediationPreferred = isRemediationPreferred;
    }
    public boolean getIsRemediationPreferred() {
        return isRemediationPreferred;
    }

    @DataBoundSetter
    public void setRunOpenSourceAnalysisOverride(boolean runOpenSourceAnalysisOverride) {
        this.runOpenSourceAnalysisOverride = runOpenSourceAnalysisOverride;
    }

    public boolean getRunOpenSourceAnalysisOverride() {
        return runOpenSourceAnalysisOverride;
    }

    @DataBoundSetter
    public void setIsExpressScanOverride(boolean isExpressScanOverride) {
        this.isExpressScanOverride = isExpressScanOverride;
    }
    public boolean getIsExpressScanOverride() {
        return isExpressScanOverride;
    }

    @DataBoundSetter
    public void setIsExpressAuditOverride(boolean isExpressAuditOverride) {
        this.isExpressAuditOverride = isExpressAuditOverride;
    }
    public boolean getIsExpressAuditOverride() {
        return isExpressAuditOverride;
    }

    @DataBoundSetter
    public void setIncludeThirdPartyOverride(boolean includeThirdPartyOverride) {
        this.includeThirdPartyOverride = includeThirdPartyOverride;
    }
    public boolean getIncludeThirdPartyOverride() {
        return includeThirdPartyOverride;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Upload PreBuild Running...");
        commonBuildStep = new SharedUploadBuildStep(bsiToken,
                overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId,
                includeAllFiles,
                isBundledAssessment,
                purchaseEntitlements,
                entitlementPreference,
                isRemediationPreferred,
                runOpenSourceAnalysisOverride,
                isExpressScanOverride,
                isExpressAuditOverride,
                includeThirdPartyOverride);

        return true;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Upload Running...");
        commonBuildStep = new SharedUploadBuildStep(bsiToken,
                overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId,
                includeAllFiles,
                isBundledAssessment,
                purchaseEntitlements,
                entitlementPreference,
                isRemediationPreferred,
                runOpenSourceAnalysisOverride,
                isExpressScanOverride,
                isExpressAuditOverride,
                includeThirdPartyOverride);

        commonBuildStep.perform(build, workspace, launcher, listener);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getDisplayName() {
            return "Run Fortify on Demand Upload";
        }

        @Override
        public String getFunctionName() {
            return "fodStaticAssessment";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        public FormValidation doTestPersonalAccessTokenConnection( @QueryParameter(SharedUploadBuildStep.USERNAME) final String username,
                                                                   @QueryParameter(SharedUploadBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                   @QueryParameter(SharedUploadBuildStep.TENANT_ID) final String tenantId)
        {
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId);

        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            return SharedUploadBuildStep.doFillEntitlementPreferenceItems();
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private transient FortifyStaticAssessment upload;

        protected Execution(FortifyStaticAssessment upload, StepContext context) {
            super(context);
            this.upload = upload;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running fodStaticAssessment step");
            upload.perform(getContext().get(Run.class), getContext().get(FilePath.class),
                    getContext().get(Launcher.class), getContext().get(TaskListener.class));

            return null;
        }

        private static final long serialVersionUID = 1L;
    }
}