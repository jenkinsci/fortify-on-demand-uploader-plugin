package org.jenkinsci.plugins.fodupload.steps;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.fodupload.SharedUploadBuildStep;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.PrintStream;
import java.util.Set;


public class FortifyUpload extends FortifyStep {

    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();

    private String bsiToken;

    boolean overrideGlobalConfig;
    String username;
    String personalAccessToken;
    String tenantId;
    boolean includeAllFiles;
    boolean isBundledAssessment;
    boolean purchaseEntitlements;
    int entitlementPreference = 2;
    boolean isRemediationPreferred;
    boolean runOpenSourceAnalysisOverride;
    boolean isExpressScanOverride;
    boolean isExpressAuditOverride;
    boolean includeThirdPartyOverride;

    SharedUploadBuildStep commonBuildStep;

    @DataBoundConstructor
    public FortifyUpload(String bsiToken) {
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
    public void setBundledAssessment(boolean bundledAssessment) {
        isBundledAssessment = bundledAssessment;
    }
    public boolean getBundledAssessment() {
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
    public void setIsRemediationPreferred(boolean remediationPreferred) {
        isRemediationPreferred = remediationPreferred;
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
    public void setExpressScanOverride(boolean expressScanOverride) {
        isExpressScanOverride = expressScanOverride;
    }
    public boolean getExpressScanOverride() {
        return isExpressScanOverride;
    }

    @DataBoundSetter
    public void setExpressAuditOverride(boolean expressAuditOverride) {
        isExpressAuditOverride = expressAuditOverride;
    }
    public boolean getExpressAuditOverride() {
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
        log.println("Fortify Upload PreBuild Running...");
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
        log.println("Fortify Upload Running...");
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
            return "fodUpload";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private transient FortifyUpload upload;

        protected Execution(FortifyUpload upload, StepContext context) {
            super(context);
            this.upload = upload;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running fodUpload step");
            upload.perform(getContext().get(Run.class), getContext().get(FilePath.class),
                    getContext().get(Launcher.class), getContext().get(TaskListener.class));

            return null;
        }

        private static final long serialVersionUID = 1L;
    }
}