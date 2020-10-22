package org.jenkinsci.plugins.fodupload.steps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.SharedUploadBuildStep;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
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
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.verb.POST;


@SuppressFBWarnings("unused")
public class FortifyStaticAssessment extends FortifyStep {

    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();

    private String releaseId;
    private String bsiToken;

    private boolean overrideGlobalConfig;
    private String username;
    private String personalAccessToken;
    private String tenantId;
    private boolean purchaseEntitlements;
    private String entitlementPreference;
    private String srcLocation;
    private String remediationScanPreferenceType;
    private String inProgressScanActionType;
    private String inProgressBuildResultType;

    private SharedUploadBuildStep commonBuildStep;

    @DataBoundConstructor
    public FortifyStaticAssessment(String releaseId, String bsiToken) {
        super();
        this.releaseId = releaseId != null ? releaseId.trim() : "";
        this.bsiToken = bsiToken != null ? bsiToken.trim() : "";
    }

    public String getBsiToken() {
        return bsiToken;
    }

    public String getReleaseId() { return releaseId; }

    public boolean getOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    @DataBoundSetter
    public void setOverrideGlobalConfig(boolean overrideGlobalConfig) {
        this.overrideGlobalConfig = overrideGlobalConfig;
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

    public boolean getPurchaseEntitlements() {
        return purchaseEntitlements;
    }

    @DataBoundSetter
    public void setPurchaseEntitlements(boolean purchaseEntitlements) {
        this.purchaseEntitlements = purchaseEntitlements;
    }

    public String getEntitlementPreference() {
        return entitlementPreference;
    }

    @DataBoundSetter
    public void setEntitlementPreference(String entitlementPreference) {
        this.entitlementPreference = entitlementPreference;
    }

    public String getSrcLocation() {
        return srcLocation;
    }

    @DataBoundSetter
    public void setSrcLocation(String srcLocation) {
        this.srcLocation = srcLocation != null ? srcLocation.trim() : "";
    }

    public String getRemediationScanPreferenceType() {
        return remediationScanPreferenceType;
    }

    @DataBoundSetter
    public void setRemediationScanPreferenceType(String remediationScanPreferenceType) {
        this.remediationScanPreferenceType = remediationScanPreferenceType;
    }

    public String getInProgressScanActionType() {
        return inProgressScanActionType;
    }

    @DataBoundSetter
    public void setInProgressScanActionType(String inProgressScanActionType) {
        this.inProgressScanActionType = inProgressScanActionType;
    }

    public String getInProgressBuildResultType() {
        return inProgressBuildResultType;
    }

    @DataBoundSetter
    public void setInProgressBuildResultType(String inProgressBuildResultType) {
        this.inProgressBuildResultType = inProgressBuildResultType;
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Upload PreBuild Running...");
        commonBuildStep = new SharedUploadBuildStep(releaseId,
                bsiToken,
                overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType);

        return true;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Upload Running...");
        build.addAction(new CrossBuildAction());
        try{build.save();} catch(IOException ex){log.println("Error saving settings. Error message: " + ex.toString());}

        
        remediationScanPreferenceType = remediationScanPreferenceType != null ? remediationScanPreferenceType : FodEnums.RemediationScanPreferenceType.RemediationScanIfAvailable.getValue();
        inProgressScanActionType = inProgressScanActionType != null ? inProgressScanActionType : FodEnums.InProgressScanActionType.DoNotStartScan.getValue();
        inProgressBuildResultType = inProgressBuildResultType != null ? inProgressBuildResultType : FodEnums.InProgressBuildResultType.FailBuild.getValue();

        commonBuildStep = new SharedUploadBuildStep(releaseId,
                bsiToken,
                overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType);

        commonBuildStep.perform(build, workspace, launcher, listener);
        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
        crossBuildAction.setPreviousStepBuildResult(build.getResult());
        if(Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
            crossBuildAction.setScanId(commonBuildStep.getScanId());
        }
        try{build.save();} catch(IOException ex){log.println("Error saving settings. Error message: " + ex.toString());}
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
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(SharedUploadBuildStep.USERNAME) final String username,
                                                                  @QueryParameter(SharedUploadBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                  @QueryParameter(SharedUploadBuildStep.TENANT_ID) final String tenantId,
                                                                  @AncestorInPath Job job) {
            job.checkPermission(Item.CONFIGURE);
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);


        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            return SharedUploadBuildStep.doFillEntitlementPreferenceItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillRemediationScanPreferenceTypeItems() {
            return SharedUploadBuildStep.doFillRemediationScanPreferenceTypeItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillUsernameItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPersonalAccessTokenItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTenantIdItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressScanActionTypeItems() {
            return SharedUploadBuildStep.doFillInProgressScanActionTypeItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressBuildResultTypeItems() {
            return SharedUploadBuildStep.doFillInProgressBuildResultTypeItems();
        }
    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
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
    }
}