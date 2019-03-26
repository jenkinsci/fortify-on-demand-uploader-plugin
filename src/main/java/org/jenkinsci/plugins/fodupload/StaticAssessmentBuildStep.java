package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;


@SuppressWarnings("unused")
public class StaticAssessmentBuildStep extends Recorder implements SimpleBuildStep {

    SharedUploadBuildStep sharedBuildStep;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Entry point when building
    @DataBoundConstructor
    public StaticAssessmentBuildStep(String bsiToken,
                                     boolean overrideGlobalConfig,
                                     String username,
                                     String personalAccessToken,
                                     String tenantId,
                                     boolean includeAllFiles,
                                     boolean isBundledAssessment,
                                     boolean purchaseEntitlements,
                                     int entitlementPreference,
                                     boolean isRemediationPreferred,
                                     boolean runOpenSourceAnalysisOverride,
                                     boolean isExpressScanOverride,
                                     boolean isExpressAuditOverride,
                                     boolean includeThirdPartyOverride) {

        sharedBuildStep = new SharedUploadBuildStep(bsiToken,
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

    }



    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener)
    {
        return sharedBuildStep.prebuild(build, listener);
    }

    // logic run during a build
    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {

        sharedBuildStep.perform(build, workspace, launcher, listener);
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public StaticAssessmentStepDescriptor getDescriptor() {
        return (StaticAssessmentStepDescriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension

    public static final class StaticAssessmentStepDescriptor extends BuildStepDescriptor<Publisher> {

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        // Entry point when accessing global configuration
        public StaticAssessmentStepDescriptor() {
            super();
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public FormValidation doCheckBsiToken(@QueryParameter String bsiToken)
        {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return SharedUploadBuildStep.doCheckBsiToken(bsiToken);
        }

        @Override
        public String getDisplayName() {
            return "Fortify on Demand Static Assessment";
        }


        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection( @QueryParameter(SharedUploadBuildStep.USERNAME) final String username,
                                                                   @QueryParameter(SharedUploadBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                   @QueryParameter(SharedUploadBuildStep.TENANT_ID) final String tenantId)
        {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            return SharedUploadBuildStep.doFillEntitlementPreferenceItems();
        }


    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    @SuppressWarnings("unused")
    public String getBsiToken() {
        return sharedBuildStep.getModel().getBsiTokenOriginal();
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


    @SuppressWarnings("unused")
    public boolean getIncludeAllFiles() {
        return sharedBuildStep.getModel().isIncludeAllFiles();
    }

    @SuppressWarnings("unused")
    public int getEntitlementPreference() {
        return sharedBuildStep.getModel().getEntitlementPreference();
    }

    @SuppressWarnings("unused")
    public boolean getIsBundledAssessment() {
        return sharedBuildStep.getModel().isBundledAssessment();
    }

    @SuppressWarnings("unused")
    public boolean getPurchaseEntitlements() {
        return sharedBuildStep.getModel().isPurchaseEntitlements();
    }

    @SuppressWarnings("unused")
    public boolean getIsRemediationPreferred() {
        return sharedBuildStep.getModel().isRemediationPreferred();
    }

    @SuppressWarnings("unused")
    public boolean getRunOpenSourceAnalysisOverride() {
        return sharedBuildStep.getModel().isRunOpenSourceAnalysisOverride();
    }

    @SuppressWarnings("unused")
    public boolean getIsExpressScanOverride() {
        return sharedBuildStep.getModel().isExpressScanOverride();
    }

    @SuppressWarnings("unused")
    public boolean getIsExpressAuditOverride() {
        return sharedBuildStep.getModel().isExpressAuditOverride();
    }

    @SuppressWarnings("unused")
    public boolean getIncludeThirdPartyOverride() {
        return sharedBuildStep.getModel().isIncludeThirdPartyOverride();
    }


}
