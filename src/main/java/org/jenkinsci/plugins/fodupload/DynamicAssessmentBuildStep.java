package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.controllers.*;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class DynamicAssessmentBuildStep extends Recorder implements SimpleBuildStep {

    DynamicScanSharedBuildStep dynamicSharedUploadBuildStep;
    boolean allowSameHostRedirects;
    boolean restrictToDirectoryAndSubdirectories;

    @DataBoundConstructor
    public DynamicAssessmentBuildStep(boolean overrideGlobalConfig, String username,
                                      String personalAccessToken, String tenantId,
                                      String releaseId, String selectedReleaseType,
                                      List<String> webSiteUrl, String dastEnv,
                                      String scanTimebox,
                                      List<String> standardScanTypeExcludeUrlsRow,
                                      String scanPolicyType, boolean restrictScan,
                                      boolean scanEntireHost,
                                      boolean allowHttp, boolean allowFormSubmissionCrawl,
                                      String selectedScanType, String selectedDynamicTimeZone,
                                      boolean webSiteLoginMacroEnabled, boolean webSiteNetworkAuthSettingEnabled,
                                      boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
                                      String loginMacroId, String workflowMacroId, String allowedHost, String webSiteNetworkAuthPassword,
                                      String userSelectedApplication,
                                      String userSelectedRelease, String assessmentTypeId,
                                      String entitlementId, String entitlementFrequencyId,
                                      String entitlementFrequencyType, String userSelectedEntitlement,
                                      String selectedDynamicGeoLocation, String selectedNetworkAuthType
    ) throws IllegalArgumentException, IOException {

        // assert FodEnums.DastTimeBoxScan.toInt(scanTimebox) != null;
        int timebox = FodEnums.DastTimeBoxScan.toInt(scanTimebox).getInteger();

        dynamicSharedUploadBuildStep = new DynamicScanSharedBuildStep(overrideGlobalConfig, username,
                personalAccessToken, tenantId,
                releaseId, selectedReleaseType,
                webSiteUrl, dastEnv,
                scanTimebox,
                standardScanTypeExcludeUrlsRow,
                scanPolicyType, scanEntireHost, restrictScan,
                allowHttp, allowFormSubmissionCrawl,
                selectedScanType, selectedDynamicTimeZone,
                webSiteLoginMacroEnabled, webSiteNetworkAuthSettingEnabled,
                enableRedundantPageDetection, webSiteNetworkAuthUserName,
                loginMacroId, workflowMacroId, allowedHost, webSiteNetworkAuthPassword,
                userSelectedApplication,
                userSelectedRelease, assessmentTypeId,
                entitlementId, entitlementFrequencyId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedDynamicGeoLocation, selectedNetworkAuthType);

        if (FodEnums.DastScanType.Standard.toString().equalsIgnoreCase(selectedScanType)) {

            dynamicSharedUploadBuildStep.saveReleaseSettingsForWebSiteScan(userSelectedRelease, assessmentTypeId, entitlementId,
                    entitlementFrequencyType, selectedDynamicGeoLocation, loginMacroId, selectedDynamicTimeZone, selectedScanType, scanPolicyType,
                    webSiteUrl, allowFormSubmissionCrawl, scanEntireHost, restrictScan, enableRedundantPageDetection, dastEnv,
                    webSiteNetworkAuthSettingEnabled, webSiteLoginMacroEnabled, webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType, scanTimebox
            );
        } else if (FodEnums.DastScanType.Workflow.toString().equalsIgnoreCase(selectedScanType)) {

            dynamicSharedUploadBuildStep.saveReleaseSettingsForWorkflowDrivenScan(userSelectedRelease, assessmentTypeId, entitlementId,
                    entitlementFrequencyType, selectedDynamicGeoLocation, loginMacroId, workflowMacroId, allowedHost, selectedDynamicTimeZone, selectedScanType, scanPolicyType,
                    webSiteUrl, allowFormSubmissionCrawl, scanEntireHost, restrictScan, enableRedundantPageDetection, dastEnv,
                    webSiteNetworkAuthSettingEnabled, webSiteLoginMacroEnabled, webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType, scanTimebox
            );
        } else if (FodEnums.DastScanType.API.toString().equalsIgnoreCase(selectedScanType)) {

        } else
            throw new IllegalArgumentException("Not Valid Dast Scan Type set for releaseId: " + releaseId);

    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        //Reconstruct the model from get settings,
        //Save the settings in Jenkins Config.xml file.

        if (dynamicSharedUploadBuildStep.getModel() == null) {
            System.out.println("job model is null");
            throw new IllegalArgumentException("DAST model not been set");
        }

        return true;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {
        PrintStream log = listener.getLogger();
        build.addAction(new CrossBuildAction());
        try {
            System.out.println("saves the jobs information");
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex);
        }

        String correlationId = UUID.randomUUID().toString();
        dynamicSharedUploadBuildStep.perform(build, workspace, launcher, listener, correlationId);

        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
        crossBuildAction.setPreviousStepBuildResult(build.getResult());


        if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
            crossBuildAction.setScanId(dynamicSharedUploadBuildStep.getScanId());
            crossBuildAction.setCorrelationId(correlationId);
        }
        try {
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex);
        }

    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getSelectedReleaseType() {
        if (dynamicSharedUploadBuildStep != null && dynamicSharedUploadBuildStep.getModel() != null)
            return dynamicSharedUploadBuildStep.getModel().getSelectedReleaseType();
        return "";
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getReleaseId() {
        if (dynamicSharedUploadBuildStep != null && dynamicSharedUploadBuildStep.getModel() != null)
            return dynamicSharedUploadBuildStep.getModel().get_releaseId();
        else return "";
    }


    @SuppressWarnings("unused")
    public String getUsername() {
        if (dynamicSharedUploadBuildStep != null && dynamicSharedUploadBuildStep.getModel() != null)
            return dynamicSharedUploadBuildStep.getAuthModel().getUsername();
        else return "";
    }

    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        if (dynamicSharedUploadBuildStep != null && dynamicSharedUploadBuildStep.getModel() != null) {
            return dynamicSharedUploadBuildStep.getAuthModel().getPersonalAccessToken();
        } else return "";
    }

    @SuppressWarnings("unused")
    public String getTenantId() {
        if (dynamicSharedUploadBuildStep != null && dynamicSharedUploadBuildStep.getModel() != null)
            return dynamicSharedUploadBuildStep.getAuthModel().getTenantId();
        else {
            return "";
        }
    }

    @SuppressWarnings("unused")
    public boolean getOverrideGlobalConfig() {
        if (dynamicSharedUploadBuildStep != null && dynamicSharedUploadBuildStep.getModel() != null) {
            return dynamicSharedUploadBuildStep.getAuthModel().getOverrideGlobalConfig();
        }
        return false;
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedRelease() {
        System.out.println("user selected release");
        return dynamicSharedUploadBuildStep.getModel().getUserSelectedRelease();
    }


    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedApplication() {
        System.out.println("user selected application");
        return dynamicSharedUploadBuildStep.getModel().getUserSelectedApplication();
    }

    @Override
    public DynamicAssessmentBuilderDescriptor getDescriptor() {

        return (DynamicAssessmentBuilderDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class DynamicAssessmentBuilderDescriptor extends BuildStepDescriptor<Publisher> {
        public DynamicAssessmentBuilderDescriptor() {
            super();
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Fortify on Demand Dynamic Assessment";
        }

        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter("username") String username, @QueryParameter("tenantId") String tenantId, @QueryParameter("personalAccessToken") String personalAccessToken, @AncestorInPath Job job) throws FormValidation {
            job.checkPermission(Item.CONFIGURE);
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillSelectedReleaseTypeItems() {
            return doFillFromEnum(FodEnums.DastReleaseType.class);
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillSelectedScanCentralBuildTypeItems() {
            return doFillFromEnum(FodEnums.SelectedScanCentralBuildType.class);
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillDastEnvItems() {
            return doFillFromEnum(FodEnums.DastEnvironmentType.class);
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanTypeItems() {
            return doFillFromEnum(FodEnums.DastScanType.class);
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanPolicyTypeItems() {
            return doFillFromEnum(FodEnums.DastPolicy.class);
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanTimeboxItems() {
            return doFillFromEnum(FodEnums.DastTimeBoxScan.class);
        }


        private static <T extends Enum<T>> ListBoxModel doFillFromEnum(Class<T> enumClass) {
            ListBoxModel items = new ListBoxModel();
            for (T selected : EnumSet.allOf(enumClass)) {
                items.add(new ListBoxModel.Option(selected.toString(), selected.name()));
            }
            return items;
        }

        @SuppressWarnings("unused")
        public static org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> customFillUserApplicationById(int applicationId, AuthenticationModel authModel) throws IOException {
            FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
            ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);

            return applicationsController.getApplicationById(applicationId);
        }

//        public static GenericListResponse<ReleaseApiResponse> customFillUserSelectedReleaseList(int applicationId, int microserviceId, String searchTerm, Integer offset, Integer limit, AuthenticationModel authModel) throws IOException {
//            FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
//            ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
//            return applicationController.getReleaseListByApplication(applicationId, microserviceId, searchTerm, offset, limit);
//        }
//
//
//        public static org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> customFillUserReleaseById(int releaseId, AuthenticationModel authModel) throws IOException {
//            FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
//            ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
//            org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> result = applicationsController.getReleaseById(releaseId);
//            return result;
//        }

        //        public static EntitlementSettings customFillEntitlementSettings(int releaseId, AuthenticationModel authModel) throws IOException {
//            return new EntitlementSettings(
//                    1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
//                    1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
//                    1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
//                    1, 1, false);
//        }
        @SuppressWarnings("unused")
        @JavaScriptMethod
        public PatchDastFileUploadResponse patchSetupManifestFile(String releaseId, JSONObject authModelObject, String fileContent, String fileType) throws FormValidation {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                DynamicScanController dynamicScanController = new DynamicScanController(apiConnection, null, Utils.createCorrelationId());
                PatchDastScanFileUploadReq patchDastScanFileUploadReq = new PatchDastScanFileUploadReq();
                patchDastScanFileUploadReq.releaseId = releaseId;

                switch (fileType) {
                    case "LoginMacro":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.LoginMacro;
                        break;
                    case "WorkflowDrivenMacro":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.WorkflowDrivenMacro;
                        break;
                    default:
                        throw new IllegalArgumentException("Manifest upload file type is not set for the release: " + releaseId);
                }

                patchDastScanFileUploadReq.Content = fileContent.getBytes();
                PatchDastFileUploadResponse response = dynamicScanController.PatchDynamicScan(patchDastScanFileUploadReq);

                if (response.fileId <= 0) {
                    throw new IllegalArgumentException("At least one host must be selected for releaseId " + releaseId);
                }
                return response;

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                throw ex;
            }

        }

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        public ListBoxModel doFillPersonalAccessTokenItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlements(Boolean isMicroservice, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                AssessmentTypesController assessmentTypesController = new AssessmentTypesController(apiConnection, null, Utils.createCorrelationId());
                return Utils.createResponseViewModel(assessmentTypesController.getDynamicAssessmentTypeEntitlements(false));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveDynamicScanSettings(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                DynamicScanController dynamicScanController = new DynamicScanController(apiConnection, null, Utils.createCorrelationId());
                String result = Utils.createResponseViewModel(dynamicScanController.getDynamicScanSettings(releaseId));
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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


//        @POST
//        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter("username") String username, @QueryParameter("tenantId") String tenantId, @QueryParameter("personalAccessToken") String personalAccessToken, @AncestorInPath Job job) throws IOException {
//
//            job.checkPermission(Item.CONFIGURE);
//            System.out.println(username);
//            FodApiConnection testApi;
//            String baseUrl = "https://fodtenant";
//            String apiUrl = "http://fodapi";
//            testApi = new FodApiConnection(tenantId + "\\" + username, personalAccessToken, baseUrl, apiUrl, "api-tenant");
//            try {
//                testApi.authenticate();
//            }catch (Exception e){
//                System.out.println(e);
//            }
//
//            String token= testApi.retrieveToken();
//            System.out.println(token);
//            if (token == null) {
//                return FormValidation.error("Unable to retrieve authentication token.");
//            }
//            return !token.isEmpty() ?
//                    FormValidation.ok("Successfully authenticated to Fortify on Demand.") :
//                    FormValidation.error("Invalid connection information. Please check your credentials and try again.");
//        }
    }

}
