package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.controllers.*;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.BsiToken;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.PutStaticScanSetupModel;
import org.jenkinsci.plugins.fodupload.models.response.PutStaticScanSetupResponse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 Freestyle Build Step
 */

@SuppressWarnings("unused")
public class StaticAssessmentBuildStep extends Recorder implements SimpleBuildStep {

    private static final BsiTokenParser tokenParser = new BsiTokenParser();

    SharedUploadBuildStep sharedBuildStep;


    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Entry point when building
    @DataBoundConstructor
    public StaticAssessmentBuildStep(String releaseId,
                                     String bsiToken,
                                     boolean overrideGlobalConfig,
                                     String username,
                                     String personalAccessToken,
                                     String tenantId,
                                     boolean purchaseEntitlements,
                                     String entitlementPreference,
                                     String userSelectedAssessmentType,
                                     String userSelectedEntitlementId,
                                     String userSelectedFrequencyType,
                                     String userSelectedTechnologyStack,
                                     String userSelectedLanguageLevel,
                                     boolean sonatypeEnabled,
                                     String userSelectedAuditPreference,
                                     String srcLocation,
                                     String remediationScanPreferenceType,
                                     String inProgressScanActionType,
                                     String inProgressBuildResultType,
                                     String selectedReleaseType,
                                     String userSelectedApplication,
                                     String userSelectedMicroservice,
                                     String userSelectedRelease,
                                     String selectedScanCentralBuildType,
                                     boolean scanCentralIncludeTests,
                                     boolean scanCentralSkipBuild,
                                     String scanCentralBuildCommand,
                                     String scanCentralBuildFile,
                                     String scanCentralExcludeFiles,
                                     String scanCentralBuildToolVersion,
                                     String scanCentralVirtualEnv,
                                     String scanCentralRequirementFile) throws IllegalArgumentException, FormValidation {
        int techStack = Utils.tryParseInt(userSelectedTechnologyStack);

        if (Utils.isNullOrEmpty(bsiToken) && techStack < 1) throw new IllegalArgumentException("Invalid Technology Stack");

        if (Utils.isNullOrEmpty(srcLocation)) {
            srcLocation = "./";
        }

        if (Utils.isNullOrEmpty(selectedReleaseType)) {
            throw new IllegalArgumentException("Invalid release selection (Pick a Realease)");
        }

        if (Utils.isNullOrEmpty(bsiToken)) {
            ValidationUtils.ScanCentralValidationResult vres = ValidationUtils.isValidScanCentralAndTechStack(selectedScanCentralBuildType, techStack);

            switch (vres) {
                case Mismatched:
                    throw new IllegalArgumentException(String.format("ScanCentral Build Type %s doesn't support %s Technology Stack", selectedScanCentralBuildType, techStack));
                case ScanCentralRequired:
                    throw new IllegalArgumentException(String.format("Technology Stack %s requires ScanCentral Build Type selection", techStack));
                case NoSelection:
                    throw new IllegalArgumentException("ScanCentral Build Type and/or Technology Stack not selected");
            }
        }

        boolean saveSettingsToFod = false;
        List<String> invalidFields = new ArrayList<>();

        switch (FodEnums.SelectedReleaseType.valueOf(selectedReleaseType)) {
            case UseBsiToken:
                userSelectedApplication = "";
                userSelectedMicroservice = "";
                userSelectedRelease = "";
                BsiToken bsi = tokenParser.tryParseBsiToken(bsiToken);

                if (bsi == null || bsi.getReleaseId() <= 0) invalidFields.add("bsiToken");
                break;
            case UseReleaseId:
                saveSettingsToFod = true;
                break;
            case UseAppAndReleaseName:
                releaseId = userSelectedRelease;
                saveSettingsToFod = true;
                break;
            default:
                throw new IllegalArgumentException("Invalid selectedReleaseType");
        }

        if (saveSettingsToFod) {
            if (Utils.tryParseInt(releaseId) <= 0) invalidFields.add("releaseId");
            if (Utils.tryParseInt(userSelectedAssessmentType) <= 0) invalidFields.add("userSelectedAssessmentType");
            if (Utils.tryParseInt(userSelectedEntitlementId) < 0) invalidFields.add("userSelectedEntitlementId");
            if (Utils.tryParseInt(userSelectedFrequencyType) <= 0) invalidFields.add("userSelectedFrequencyType");

            // Except .NET . Java , Python all other languages has no language levels
            if (isTechStackWithLanguageLevel(techStack) && Utils.tryParseInt(userSelectedLanguageLevel) <= 0) {
                invalidFields.add("userSelectedLanguageLevel");
            }
            if (sonatypeEnabled && ValidationUtils.isSonatypeScanNotAllowedForTechStack(techStack)) {
                sonatypeEnabled = false;
            }
            if (Utils.tryParseInt(userSelectedAuditPreference) <= 0) invalidFields.add("userSelectedAuditPreference");

            if (invalidFields.size() == 0) {
                FodApiConnection apiConnection = getApiConnection(overrideGlobalConfig, username, personalAccessToken, tenantId);

                saveReleaseSettings(apiConnection, releaseId, purchaseEntitlements, userSelectedAssessmentType,
                        userSelectedEntitlementId, userSelectedFrequencyType, userSelectedTechnologyStack, userSelectedLanguageLevel,
                        sonatypeEnabled, userSelectedAuditPreference);
            }
        }

        if (invalidFields.size() > 0) {
            throw new IllegalArgumentException("Invalid field(s): " + String.join(", ", invalidFields));
        }

        sharedBuildStep = new SharedUploadBuildStep(releaseId,
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
                inProgressBuildResultType,
                selectedReleaseType,
                userSelectedApplication,
                userSelectedMicroservice,
                userSelectedRelease,
                selectedScanCentralBuildType,
                scanCentralSkipBuild,
                scanCentralBuildCommand,
                scanCentralBuildFile,
                scanCentralExcludeFiles,
                scanCentralBuildToolVersion,
                scanCentralVirtualEnv,
                scanCentralRequirementFile);

    }

    private FodApiConnection getApiConnection(boolean overrideGlobalConfig, String username, String personalAccessToken, String tenantId) throws FormValidation {
        AuthenticationModel authModel = new AuthenticationModel(false, null, null, null);
        if (overrideGlobalConfig) {
            authModel = AuthenticationModel.fromPersonalAccessToken(username, personalAccessToken, tenantId);
        }

        return ApiConnectionFactory.createApiConnection(authModel, false, null, null);
    }

    private boolean isTechStackWithLanguageLevel(int techStack) {
        return (techStack == 1 || techStack == 7 || techStack == 10) ? true : false;
    }

    private void saveReleaseSettings(FodApiConnection apiConnection, String releaseIdStr, boolean purchaseEntitlements, String assessmentTypeIdStr, String entitlementIdStr, String entitlementFrequencyTypeStr, String technologyStackIdStr, String languageLevelIdStr, boolean performOpenSourceAnalysis, String auditPreferenceTypeStr) throws IllegalArgumentException {
        StaticScanController staticScanController = new StaticScanController(apiConnection, null, Utils.createCorrelationId());

        int releaseId = Integer.parseInt(releaseIdStr);
        int assessmentTypeId = Integer.parseInt(assessmentTypeIdStr);
        int entitlementId = Integer.parseInt(entitlementIdStr);
        int entitlementFrequencyType = Integer.parseInt(entitlementFrequencyTypeStr);
        int technologyStackId = Integer.parseInt(technologyStackIdStr);
        Integer languageLevelId = Utils.tryParseInt(languageLevelIdStr, null);
        int auditPreferenceType = Integer.parseInt(auditPreferenceTypeStr);

        try {
            PutStaticScanSetupResponse response = staticScanController.putStaticScanSettings(releaseId, new PutStaticScanSetupModel(assessmentTypeId, entitlementId, entitlementFrequencyType, technologyStackId, languageLevelId, performOpenSourceAnalysis, auditPreferenceType));
            if (response.isSuccess()) {
                System.out.println("Successfully saved settings for release id = " + releaseIdStr);
            } else if (response.getErrors() != null && !response.getErrors().isEmpty()) {
                String errs = response.getErrors().stream().map(s -> s.replace("\n", "\n\t\t")).collect(Collectors.joining("\n\t"));

                System.err.println("Error saving settings for release id = " + releaseIdStr + "\n\t" + errs);
                throw new IllegalArgumentException("Failed to save scan settings for release id = " + releaseIdStr);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to save scan settings for release id = " + releaseIdStr, e);
        }
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        return sharedBuildStep.prebuild(build, listener);
    }

    // logic run during a build
    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {

        PrintStream log = listener.getLogger();
        build.addAction(new CrossBuildAction());
        try {
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex.toString());
        }

        String correlationId = UUID.randomUUID().toString();

        sharedBuildStep.perform(build, workspace, launcher, listener, correlationId);

        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
        crossBuildAction.setPreviousStepBuildResult(build.getResult());
        if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
            crossBuildAction.setScanId(sharedBuildStep.getScanId());
            crossBuildAction.setCorrelationId(correlationId);
        }
        try {
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex.toString());
        }
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

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getReleaseId() {
        return sharedBuildStep.getModel().getReleaseId();
    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    @SuppressWarnings("unused")
    @JavaScriptMethod
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
    public String getEntitlementPreference() {
        return sharedBuildStep.getModel().getEntitlementPreference();
    }

    @SuppressWarnings("unused")
    public boolean getPurchaseEntitlements() {
        return sharedBuildStep.getModel().isPurchaseEntitlements();
    }

    @SuppressWarnings("unused")
    public String getSrcLocation() {
        return sharedBuildStep.getModel().getSrcLocation();
    }

    @SuppressWarnings("unused")
    public String getRemediationScanPreferenceType() {
        return sharedBuildStep.getModel().getRemediationScanPreferenceType();
    }

    @SuppressWarnings("unused")
    public String getInProgressScanActionType() {
        return sharedBuildStep.getModel().getInProgressScanActionType();
    }

    @SuppressWarnings("unused")
    public String getInProgressBuildResultType() {
        return sharedBuildStep.getModel().getInProgressBuildResultType();
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedApplication() {
        return sharedBuildStep.getModel().getUserSelectedApplication();
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedMicroservice() {
        return sharedBuildStep.getModel().getUserSelectedMicroservice();
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedRelease() {
        return sharedBuildStep.getModel().getUserSelectedRelease();
    }

    @SuppressWarnings("unused")
    public String getSelectedReleaseType() {
        return sharedBuildStep.getModel().getSelectedReleaseType();
    }

    @SuppressWarnings("unused")
    public String getSelectedScanCentralBuildType() {
        return sharedBuildStep.getModel().getSelectedScanCentralBuildType();
    }

    public boolean getScanCentralSkipBuild() {
        return sharedBuildStep.getModel().getScanCentralSkipBuild();
    }

    public String getScanCentralBuildCommand() {
        return sharedBuildStep.getModel().getScanCentralBuildCommand();
    }

    public String getScanCentralBuildFile() {
        return sharedBuildStep.getModel().getScanCentralBuildFile();
    }

    public String getScanCentralExcludeFiles() {
        return sharedBuildStep.getModel().getScanCentralExcludeFiles();
    }

    public String getScanCentralBuildToolVersion() {
        return sharedBuildStep.getModel().getScanCentralBuildToolVersion();
    }

    public String getScanCentralVirtualEnv() {
        return sharedBuildStep.getModel().getScanCentralVirtualEnv();
    }

    public String getScanCentralRequirementFile() {
        return sharedBuildStep.getModel().getScanCentralRequirementFile();
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

        public FormValidation doCheckReleaseId(@QueryParameter String releaseId, @QueryParameter String bsiToken) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return SharedUploadBuildStep.doCheckReleaseId(releaseId, bsiToken);
        }

        public FormValidation doCheckBsiToken(@QueryParameter String bsiToken, @QueryParameter String releaseId) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return SharedUploadBuildStep.doCheckBsiToken(bsiToken, releaseId);
        }

        @Override
        public String getDisplayName() {
            return "Fortify on Demand Static Assessment";
        }


        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(SharedUploadBuildStep.USERNAME) final String username,
                                                                  @QueryParameter(SharedUploadBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                  @QueryParameter(SharedUploadBuildStep.TENANT_ID) final String tenantId,
                                                                  @AncestorInPath Job job) throws FormValidation {
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

        @SuppressWarnings("unused")
        public ListBoxModel doFillSelectedReleaseTypeItems() {
            return SharedUploadBuildStep.doFillSelectedReleaseTypeItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillSelectedScanCentralBuildTypeItems() {
            return SharedUploadBuildStep.doFillSelectedScanCentralBuildTypeItems();
        }

        @JavaScriptMethod
        public String submitCreateApplication(JSONObject formObject, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedCreateApplicationForm.submitCreateApplication(authModel, formObject));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String submitCreateMicroservice(JSONObject formObject, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedCreateApplicationForm.submitCreateMicroservice(authModel, formObject));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String submitCreateRelease(JSONObject formObject, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedCreateApplicationForm.submitCreateRelease(authModel, formObject));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveApplicationList(String searchTerm, int offset, int limit, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedUploadBuildStep.customFillUserSelectedApplicationList(searchTerm, offset, limit, authModel));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveApplicationById(int applicationId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedUploadBuildStep.customFillUserApplicationById(applicationId, authModel));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveMicroserviceList(int selectedApplicationId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedUploadBuildStep.customFillUserSelectedMicroserviceList(selectedApplicationId, authModel));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveReleaseList(int selectedApplicationId, int microserviceId, String searchTerm, int offset, int limit, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedUploadBuildStep.customFillUserSelectedReleaseList(selectedApplicationId, microserviceId, searchTerm, offset, limit, authModel));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveReleaseById(int releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedUploadBuildStep.customFillUserReleaseById(releaseId, authModel));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveCurrentUserSession(JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                UsersController usersController = new UsersController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(usersController.getCurrentUserSession());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlements(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                AssessmentTypesController assessmentTypesController = new AssessmentTypesController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(assessmentTypesController.getStaticAssessmentTypeEntitlements(releaseId));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveAuditPreferences(Integer releaseId, Integer assessmentType, Integer frequencyType, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                ReleaseController releaseController = new ReleaseController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(releaseController.getAuditPreferences(releaseId, assessmentType, frequencyType));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveLookupItems(String type, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                LookupItemsController lookupItemsController = new LookupItemsController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(lookupItemsController.getLookupItems(FodEnums.APILookupItemTypes.valueOf(type)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveStaticScanSettings(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                StaticScanController staticScanController = new StaticScanController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(staticScanController.getStaticScanSettings(releaseId));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // ToDo: This is a mock, get rid of it
        @JavaScriptMethod
        public String getReleaseEntitlementSettings(int selectedReleaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedUploadBuildStep.customFillEntitlementSettings(selectedReleaseId, authModel));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
