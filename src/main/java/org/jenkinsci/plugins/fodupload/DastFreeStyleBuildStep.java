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
import java.util.List;
import java.util.UUID;

public class DastFreeStyleBuildStep extends Recorder implements SimpleBuildStep {

    DastScanSharedBuildStep dynamicSharedBuildStep;

    @DataBoundConstructor
    public DastFreeStyleBuildStep(boolean overrideGlobalConfig, String username,
                                  String personalAccessToken, String tenantId,
                                  String releaseId, String selectedReleaseType,
                                  String webSiteUrl, String dastEnv,
                                  String scanTimeBox,
                                  List<String> standardScanTypeExcludedUrls,
                                  String scanPolicy, boolean scanScope,
                                  String selectedScanType, String selectedDynamicTimeZone,
                                  boolean webSiteLoginMacroEnabled, boolean webSiteNetworkAuthSettingEnabled,
                                  boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
                                  String loginMacroId, String workflowMacroId, String workflowMacroHosts, String webSiteNetworkAuthPassword,
                                  String userSelectedApplication,
                                  String userSelectedRelease, String assessmentTypeId,
                                  String entitlementId,
                                  String entitlementFrequencyType, String userSelectedEntitlement,
                                  String selectedDynamicGeoLocation, String selectedNetworkAuthType, boolean timeBoxChecked,
                                      String selectedApiType,
                                      String openApiRadioSource, String openApiFileId, String openApiUrl, String openApiKey,
                                      String postmanFileId,
                                      String graphQlRadioSource,String graphQLFileId, String graphQLUrl, String graphQLSchemeType, String graphQlApiHost, String graphQlApiServicePath,
                                      String grpcFileId, String grpcSchemeType, String grpcApiHost, String grpcApiServicePath


    ) throws IllegalArgumentException, IOException {

        dynamicSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig, username,
                personalAccessToken, tenantId,
                releaseId, selectedReleaseType,
                webSiteUrl, dastEnv,
                scanTimeBox,
                standardScanTypeExcludedUrls,
                scanPolicy, scanScope,
                selectedScanType, selectedDynamicTimeZone,
                webSiteLoginMacroEnabled, webSiteNetworkAuthSettingEnabled,
                enableRedundantPageDetection, webSiteNetworkAuthUserName,
                loginMacroId, workflowMacroId, workflowMacroHosts, webSiteNetworkAuthPassword,
                userSelectedApplication,
                userSelectedRelease, assessmentTypeId,
                entitlementId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedDynamicGeoLocation, selectedNetworkAuthType, timeBoxChecked,
                selectedApiType, openApiRadioSource, openApiFileId, openApiUrl, openApiKey,
                postmanFileId,
                graphQlRadioSource, graphQLFileId, graphQLUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath);

        if (FodEnums.DastScanType.Standard.toString().equalsIgnoreCase(selectedScanType)) {

            dynamicSharedBuildStep.saveReleaseSettingsForWebSiteScan(userSelectedRelease, assessmentTypeId, entitlementId,
                    entitlementFrequencyType, loginMacroId, selectedDynamicTimeZone, scanPolicy,
                    webSiteUrl, scanScope, enableRedundantPageDetection, dastEnv,
                    webSiteNetworkAuthSettingEnabled, webSiteLoginMacroEnabled, webSiteNetworkAuthUserName,
                    webSiteNetworkAuthPassword, selectedNetworkAuthType, scanTimeBox);

        } else if (FodEnums.DastScanType.Workflow.toString().equalsIgnoreCase(selectedScanType)) {

            dynamicSharedBuildStep.saveReleaseSettingsForWorkflowDrivenScan(userSelectedRelease, assessmentTypeId, entitlementId,
                    entitlementFrequencyType, workflowMacroId, workflowMacroHosts, selectedDynamicTimeZone, scanPolicy,
                    enableRedundantPageDetection, dastEnv,
                    webSiteNetworkAuthSettingEnabled, webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType);
        } else if (FodEnums.DastScanType.API.toString().equalsIgnoreCase(selectedScanType)) {
            if (FodEnums.DastApiType.OpenApi.toString().equalsIgnoreCase(selectedApiType)) {
                String sourceUrn = openApiRadioSource.equals("Url") ? openApiUrl : openApiFileId;
                dynamicSharedBuildStep.saveReleaseSettingsForOpenApiScan(userSelectedRelease, assessmentTypeId, entitlementId,
                        entitlementFrequencyType, selectedDynamicTimeZone,
                        enableRedundantPageDetection, dastEnv, webSiteNetworkAuthSettingEnabled,
                        webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType,
                        openApiRadioSource, sourceUrn, openApiKey);

            }
                if (FodEnums.DastApiType.GraphQL.toString().equalsIgnoreCase(selectedApiType)) {
                    String sourceUrn = graphQlRadioSource.equals("Url") ? graphQLUrl : graphQLFileId;
                    dynamicSharedBuildStep.saveReleaseSettingsForGraphQlScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            enableRedundantPageDetection, dastEnv, webSiteNetworkAuthSettingEnabled,
                            webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType,
                            sourceUrn, graphQlRadioSource, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath);

                } else if (FodEnums.DastApiType.Grpc.toString().equalsIgnoreCase(selectedApiType)) {
                    dynamicSharedBuildStep.saveReleaseSettingsForGrpcScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            enableRedundantPageDetection, dastEnv, webSiteNetworkAuthSettingEnabled,
                            webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType,
                            grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath);

                } else if (FodEnums.DastApiType.Postman.toString().equalsIgnoreCase(selectedApiType)) {
                    dynamicSharedBuildStep.saveReleaseSettingsForPostmanScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            enableRedundantPageDetection, dastEnv, webSiteNetworkAuthSettingEnabled,
                            webSiteNetworkAuthUserName, webSiteNetworkAuthPassword, selectedNetworkAuthType,
                            postmanFileId);
                } else {

                }
            } else
                throw new IllegalArgumentException("Not Valid Dast Scan Type set for releaseId: " + userSelectedRelease);

        }


    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {

        if (dynamicSharedBuildStep.getModel() == null) {
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
        dynamicSharedBuildStep.perform(build, workspace, launcher, listener, correlationId);

        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
        crossBuildAction.setPreviousStepBuildResult(build.getResult());


        if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
            crossBuildAction.setScanId(dynamicSharedBuildStep.getScanId());
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
        if (dynamicSharedBuildStep != null && dynamicSharedBuildStep.getModel() != null)
            return dynamicSharedBuildStep.getModel().getSelectedReleaseType();
        return "";
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getReleaseId() {
        if (dynamicSharedBuildStep != null && dynamicSharedBuildStep.getModel() != null)
            return dynamicSharedBuildStep.getModel().get_releaseId();
        else return "";
    }


    @SuppressWarnings("unused")
    public String getUsername() {
        if (dynamicSharedBuildStep != null && dynamicSharedBuildStep.getModel() != null)
            return dynamicSharedBuildStep.getAuthModel().getUsername();
        else return "";
    }

    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        if (dynamicSharedBuildStep != null && dynamicSharedBuildStep.getModel() != null) {
            return dynamicSharedBuildStep.getAuthModel().getPersonalAccessToken();
        } else return "";
    }

    @SuppressWarnings("unused")
    public String getTenantId() {
        if (dynamicSharedBuildStep != null && dynamicSharedBuildStep.getModel() != null)
            return dynamicSharedBuildStep.getAuthModel().getTenantId();
        else {
            return "";
        }
    }

    @SuppressWarnings("unused")
    public boolean getOverrideGlobalConfig() {
        if (dynamicSharedBuildStep != null && dynamicSharedBuildStep.getModel() != null) {
            return dynamicSharedBuildStep.getAuthModel().getOverrideGlobalConfig();
        }
        return false;
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedRelease() {
        System.out.println("user selected release");
        return dynamicSharedBuildStep.getModel().getUserSelectedRelease();
    }
    @JavaScriptMethod
    public String getUseSelectedApiType() {
        System.out.println("user selected release");
        return dynamicSharedBuildStep.getModel().getUseSelectedApiType();
    }


    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedApplication() {
        System.out.println("user selected application");
        return dynamicSharedBuildStep.getModel().getUserSelectedApplication();
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

        @SuppressWarnings("unused")
        public ListBoxModel doFillSelectedReleaseTypeItems() {
            return DastScanSharedBuildStep.doFillSelectedReleaseTypeItems();
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillDastEnvItems() {
            return DastScanSharedBuildStep.doFillDastEnvItems();

        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanTypeItems() {
            return DastScanSharedBuildStep.doFillScanTypeItems();
            }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanPolicyItems() {
            return DastScanSharedBuildStep.doFillScanPolicyItems();
        }

        @SuppressWarnings("unused")
        public static org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> customFillUserApplicationById(int applicationId, AuthenticationModel authModel) throws IOException {
            FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
            ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);

            return applicationsController.getApplicationById(applicationId);
        }

        //ToDo:- delete this dead code after completing pipeline.
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


        @SuppressWarnings("unused")
        @JavaScriptMethod
        public PatchDastFileUploadResponse patchSetupManifestFile(String releaseId, JSONObject authModelObject, String fileContent, String fileType) throws FormValidation {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                DastScanController dastScanController = new DastScanController(apiConnection, null, Utils.createCorrelationId());
                PatchDastScanFileUploadReq patchDastScanFileUploadReq = new PatchDastScanFileUploadReq();
                patchDastScanFileUploadReq.releaseId = releaseId;

                switch (fileType) {
                    case "LoginMacro":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.LoginMacro;
                        break;
                    case "WorkflowDrivenMacro":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.WorkflowDrivenMacro;
                        break;
                    case "OpenAPIDefinition":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.OpenAPIDefinition;
                        break;
                    case "GraphQLDefinition":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.GraphQLDefinition;
                        break;
                    case "GRPCDefinition":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.GRPCDefinition;
                        break;
                    case "PostmanCollection":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DynamicScanFileTypes.PostmanCollection;
                        break;
                    default:
                        throw new IllegalArgumentException("Manifest upload file type is not set for the release: " + releaseId);
                }

                patchDastScanFileUploadReq.Content = fileContent.getBytes();
                return dastScanController.PatchDynamicScan(patchDastScanFileUploadReq);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return null;
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
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
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
                DastScanController dastScanController = new DastScanController(apiConnection, null, Utils.createCorrelationId());
                return Utils.createResponseViewModel(dastScanController.getDastScanSettings(releaseId));

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
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
        public String submitCreateRelease(JSONObject formObject, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                return Utils.createResponseViewModel(SharedCreateApplicationForm.submitCreateRelease(authModel, formObject));
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

        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(SharedUploadBuildStep.USERNAME) final String username,
                                                                  @QueryParameter(SharedUploadBuildStep.PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                                  @QueryParameter(SharedUploadBuildStep.TENANT_ID) final String tenantId,
                                                                  @AncestorInPath Job job) throws FormValidation {
            job.checkPermission(Item.CONFIGURE);
            return DastScanSharedBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);
        }

    }


}
