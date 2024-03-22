package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.CharEncoding;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.controllers.*;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.PatchDastScanFileUploadReq;
import org.jenkinsci.plugins.fodupload.models.response.ApplicationApiResponse;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;
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

public class FortifyDastFreeStyleBuildStep extends Recorder implements SimpleBuildStep {

    DastScanSharedBuildStep dastSharedBuildStep;

    @DataBoundConstructor
    public FortifyDastFreeStyleBuildStep(boolean overrideGlobalConfig, String username,
                                         String personalAccessToken, String tenantId,
                                         String webSiteUrl, String dastEnv,
                                         String scanTimeBox,
                                         String excludedUrls,
                                         String scanPolicy, boolean scanScope,
                                         String selectedScanType, String selectedDynamicTimeZone,
                                         boolean webSiteNetworkAuthSettingEnabled,
                                         boolean enableRedundantPageDetection, String networkAuthUserName,
                                         String loginMacroId, String workflowMacroId, String workflowMacroHosts,
                                         String networkAuthPassword,
                                         String userSelectedApplication,
                                         String userSelectedRelease, String assessmentTypeId,
                                         String entitlementId,
                                         String entitlementFrequencyType, String userSelectedEntitlement,
                                         String selectedNetworkAuthType, boolean timeBoxChecked,
                                         String selectedApiType,
                                         String openApiRadioSource, String openApiFileId, String openApiUrl, String openApiKey,
                                         String postmanFileId,
                                         String graphQlRadioSource, String graphQLFileId, String graphQLUrl, String graphQLSchemeType, String graphQlApiHost, String graphQlApiServicePath,
                                         String grpcFileId, String grpcSchemeType, String grpcApiHost, String grpcApiServicePath, String openApiFilePath, String postmanFilePath, String graphQlFilePath, String grpcFilePath,
                                         boolean requestLoginMacroFileCreation, String loginMacroPrimaryUserName, String loginMacroPrimaryPassword, String loginMacroSecondaryUsername,
                                         String loginMacroSecondaryPassword, boolean requestFalsePositiveRemoval

    ) throws Exception {
        try {

            if (selectedScanType.equals(FodEnums.DastScanType.Workflow.toString()) || selectedScanType.equals(FodEnums.DastScanType.Website.toString())) {
                dastSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig, username, tenantId,
                        personalAccessToken, userSelectedRelease,
                        webSiteUrl, dastEnv,
                        scanTimeBox,
                        excludedUrls,
                        scanPolicy, scanScope,
                        selectedScanType, selectedDynamicTimeZone, enableRedundantPageDetection, null,null,
                        loginMacroId.isEmpty() ? 0 : Integer.parseInt(loginMacroId), workflowMacroId, workflowMacroHosts,
                        networkAuthUserName, networkAuthPassword, userSelectedApplication,
                        assessmentTypeId, entitlementId, entitlementFrequencyType, selectedNetworkAuthType, timeBoxChecked,
                        requestLoginMacroFileCreation, loginMacroPrimaryUserName, loginMacroPrimaryPassword,
                        loginMacroSecondaryUsername, loginMacroSecondaryPassword, requestFalsePositiveRemoval
                );

            } else if (selectedScanType.equals(FodEnums.DastScanType.API.toString())) {
                dastSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig, username,
                        personalAccessToken, tenantId,
                        userSelectedRelease,
                        dastEnv,
                        scanTimeBox,
                        scanPolicy, scanScope,
                        selectedScanType, selectedDynamicTimeZone,
                        networkAuthUserName, networkAuthPassword,
                        userSelectedApplication, assessmentTypeId, entitlementId,
                        entitlementFrequencyType, userSelectedEntitlement,
                        timeBoxChecked, selectedApiType, openApiRadioSource, openApiFileId, openApiUrl, openApiKey,
                        postmanFileId,
                        graphQlRadioSource, graphQLFileId, graphQLUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                        grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath, openApiFilePath, postmanFilePath, graphQlFilePath, grpcFilePath, requestFalsePositiveRemoval);
            } else {
                throw new IllegalArgumentException("Invalid Scan Type");
            }

            List<String> error = dastSharedBuildStep.validateModel();
            if (!error.isEmpty()) {
                throw new IllegalArgumentException("Invalid save scan settings for release id: " + String.join(", ", error));
            }

            FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(this.dastSharedBuildStep.getAuthModel(), false,
                    null, null);

            if(apiConnection ==null)
            {
                throw new Exception("FOD API Connection not set.");
            }

            dastSharedBuildStep.setFodApiConnection(apiConnection);

            if (FodEnums.DastScanType.Website.toString().equalsIgnoreCase(selectedScanType)) {

                dastSharedBuildStep.saveReleaseSettingsForWebSiteScan(userSelectedRelease, assessmentTypeId, entitlementId,
                        entitlementFrequencyType, loginMacroId, selectedDynamicTimeZone, scanPolicy,
                        webSiteUrl, scanScope, enableRedundantPageDetection, dastEnv,
                        networkAuthUserName,
                        networkAuthPassword, selectedNetworkAuthType, scanTimeBox,
                        requestLoginMacroFileCreation, loginMacroPrimaryUserName,
                        loginMacroPrimaryPassword, loginMacroSecondaryUsername ,loginMacroSecondaryPassword, requestFalsePositiveRemoval
                ,excludedUrls);

            } else if (FodEnums.DastScanType.Workflow.toString().equalsIgnoreCase(selectedScanType)) {

                dastSharedBuildStep.saveReleaseSettingsForWorkflowDrivenScan(userSelectedRelease, assessmentTypeId, entitlementId,
                        entitlementFrequencyType, workflowMacroId, workflowMacroHosts, selectedDynamicTimeZone, scanPolicy,
                        dastEnv,
                        networkAuthUserName, networkAuthPassword, selectedNetworkAuthType, requestFalsePositiveRemoval);

            } else if (FodEnums.DastScanType.API.toString().equalsIgnoreCase(selectedScanType)) {
                if (FodEnums.DastApiType.OpenApi.toString().equalsIgnoreCase(selectedApiType)) {
                    String sourceUrn = openApiRadioSource.equals("Url") ? openApiUrl : openApiFileId;
                    dastSharedBuildStep.saveReleaseSettingsForOpenApiScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            enableRedundantPageDetection, dastEnv, webSiteNetworkAuthSettingEnabled,
                            networkAuthUserName, networkAuthPassword, selectedNetworkAuthType,
                            openApiRadioSource, sourceUrn, openApiKey, requestFalsePositiveRemoval,scanTimeBox);

                }
                else if (FodEnums.DastApiType.GraphQL.toString().equalsIgnoreCase(selectedApiType)) {

                    String sourceUrn = graphQlRadioSource.equals("Url") ? graphQLUrl : graphQLFileId;
                    dastSharedBuildStep.saveReleaseSettingsForGraphQlScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            enableRedundantPageDetection, dastEnv, webSiteNetworkAuthSettingEnabled,
                            networkAuthUserName, networkAuthPassword, selectedNetworkAuthType,
                            sourceUrn, graphQlRadioSource, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath, requestFalsePositiveRemoval, scanTimeBox);

                } else if (FodEnums.DastApiType.Grpc.toString().equalsIgnoreCase(selectedApiType)) {

                    dastSharedBuildStep.saveReleaseSettingsForGrpcScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            dastEnv,
                            networkAuthUserName, networkAuthPassword, selectedNetworkAuthType,
                            grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath, requestFalsePositiveRemoval, scanTimeBox);

                } else if (FodEnums.DastApiType.Postman.toString().equalsIgnoreCase(selectedApiType)) {

                    dastSharedBuildStep.saveReleaseSettingsForPostmanScan(userSelectedRelease, assessmentTypeId, entitlementId,
                            entitlementFrequencyType, selectedDynamicTimeZone,
                            dastEnv,
                            networkAuthUserName, networkAuthPassword, selectedNetworkAuthType,
                            postmanFileId, requestFalsePositiveRemoval, scanTimeBox);
                } else {

                    throw new IllegalArgumentException("Not Valid Dast API Scan Type set for releaseId: " + userSelectedRelease);
                }
            } else
                throw new IllegalArgumentException("Not Valid Dast Scan Type set for releaseId: " + userSelectedRelease);
        }
        catch (Exception ex) {
            throw new Exception(String.format("Fortify onDemand: %s %s", ex.getMessage(), ex.getCause()));
        }
    }


    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {

        if (dastSharedBuildStep.getModel() == null) {
            Utils.logger(listener.getLogger(), "Dast job model not constructed");
            throw new RuntimeException("Dast job model not constructed");
        } else {
            dastSharedBuildStep.validateModel();
            return true;
        }
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {
        PrintStream printStream = listener.getLogger();
        try {

            build.addAction(new CrossBuildAction());
            FodApiConnection apiConnection = null;

            apiConnection = ApiConnectionFactory.createApiConnection(this.dastSharedBuildStep.getAuthModel(), workspace.isRemote(), launcher, printStream);
            build.save();

            if (apiConnection == null) {
                throw new Exception("Fod API Connection not set.");
            }

            String correlationId = UUID.randomUUID().toString();
            dastSharedBuildStep.perform(build, listener, correlationId, apiConnection);

            CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
            crossBuildAction.setPreviousStepBuildResult(build.getResult());

            if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
                crossBuildAction.setScanId(dastSharedBuildStep.getScanId());
                crossBuildAction.setCorrelationId(correlationId);
            }
            build.save();
        } catch (Exception ex) {
            Utils.logger(printStream, String.format("Build failed for release Id: %s with error: %s %s", getReleaseId(), ex.getMessage(), ex.getCause()));
            build.setResult(Result.FAILURE);

        }
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getSelectedReleaseType() {
        if (dastSharedBuildStep != null && dastSharedBuildStep.getModel() != null)
            return dastSharedBuildStep.getModel().getSelectedReleaseType();
        return "";
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getReleaseId() {
        if (dastSharedBuildStep != null && dastSharedBuildStep.getModel() != null)
            return dastSharedBuildStep.getModel().get_releaseId();
        else return "";
    }


    @SuppressWarnings("unused")
    public String getUsername() {
        if (dastSharedBuildStep != null && dastSharedBuildStep.getModel() != null)
            return dastSharedBuildStep.getAuthModel().getUsername();
        else return "";
    }

    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        if (dastSharedBuildStep != null && dastSharedBuildStep.getModel() != null) {
            return dastSharedBuildStep.getAuthModel().getPersonalAccessToken();
        } else return "";
    }

    @SuppressWarnings("unused")
    public String getTenantId() {
        if (dastSharedBuildStep != null && dastSharedBuildStep.getModel() != null)
            return dastSharedBuildStep.getAuthModel().getTenantId();
        else {
            return "";
        }
    }

    @SuppressWarnings("unused")
    public boolean getOverrideGlobalConfig() {
        if (dastSharedBuildStep != null && dastSharedBuildStep.getModel() != null) {
            return dastSharedBuildStep.getAuthModel().getOverrideGlobalConfig();
        }
        return false;
    }

    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedRelease() {
        return dastSharedBuildStep.getModel().getUserSelectedRelease();
    }

    @JavaScriptMethod
    public String getUseSelectedApiType() {
        return dastSharedBuildStep.getModel().getSelectedApiType();
    }


    @SuppressWarnings("unused")
    @JavaScriptMethod
    public String getUserSelectedApplication() {
        return dastSharedBuildStep.getModel().getUserSelectedApplication();
    }

    @Override
    public FortifyDastFreeStyleBuilderDescriptor getDescriptor() {

        return (FortifyDastFreeStyleBuilderDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class FortifyDastFreeStyleBuilderDescriptor extends BuildStepDescriptor<Publisher> {
        public FortifyDastFreeStyleBuilderDescriptor() {
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

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public PatchDastFileUploadResponse dastManifestFileUpload(String releaseId, JSONObject authModelObject, String fileContent, String fileType, String fileName) throws FormValidation {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                DastScanController dastScanController = new DastScanController(apiConnection, null, Utils.createCorrelationId());
                PatchDastScanFileUploadReq patchDastScanFileUploadReq = new PatchDastScanFileUploadReq();
                patchDastScanFileUploadReq.releaseId = releaseId;

                switch (fileType) {
                    case "LoginMacro":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.LoginMacro;
                        break;
                    case "WorkflowDrivenMacro":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.WorkflowDrivenMacro;
                        break;
                    case "OpenAPIDefinition":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.OpenAPIDefinition;
                        break;
                    case "GraphQLDefinition":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.GraphQLDefinition;
                        break;
                    case "GRPCDefinition":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.GRPCDefinition;
                        break;
                    case "PostmanCollection":
                        patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.PostmanCollection;
                        break;
                    default:
                        throw new IllegalArgumentException("Dast Manifest upload file type is not set for the release Id: " + releaseId);
                }

                patchDastScanFileUploadReq.Content = fileContent.getBytes(CharEncoding.UTF_8);
                patchDastScanFileUploadReq.fileName = fileName;
                return dastScanController.dastFileUpload(patchDastScanFileUploadReq);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return null;
            }
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        @SuppressFBWarnings("REC_CATCH_EXCEPTION")
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
        @SuppressFBWarnings("REC_CATCH_EXCEPTION")
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
        @SuppressFBWarnings("REC_CATCH_EXCEPTION")
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
        @SuppressFBWarnings("REC_CATCH_EXCEPTION")
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
