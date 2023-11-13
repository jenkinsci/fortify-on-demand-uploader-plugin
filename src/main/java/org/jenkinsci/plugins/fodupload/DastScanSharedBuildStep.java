package org.jenkinsci.plugins.fodupload;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.model.Result;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.DastScanController;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.jenkinsci.plugins.fodupload.models.response.Dast.OpenApi;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PutDastScanSetupResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PostDastStartScanResponse;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static org.jenkinsci.plugins.fodupload.Utils.FOD_URL_ERROR_MESSAGE;
import static org.jenkinsci.plugins.fodupload.Utils.isValidUrl;

public class DastScanSharedBuildStep {
    private final DastScanJobModel model;
    private final AuthenticationModel authModel;

    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private int scanId;
    private OpenApi openApi;

    public DastScanSharedBuildStep(DastScanJobModel model, AuthenticationModel authModel) {
        this.model = model;
        this.authModel = authModel;
    }

    public DastScanSharedBuildStep(boolean overrideGlobalConfig, String username,
                                   String personalAccessToken, String tenantId,
                                   String releaseId, String selectedReleaseType,
                                   String webSiteUrl, String dastEnv,
                                   String scanTimebox,
                                   List<String> standardScanTypeExcludeUrlsRow,
                                   String scanPolicyType, boolean scanScope,
                                   String selectedScanType, String selectedDynamicTimeZone,
                                   boolean webSiteLoginMacroEnabled, boolean webSiteNetworkAuthSettingEnabled,
                                   boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
                                   String loginMacroId, String workflowMacroId, String allowedHost, String webSiteNetworkAuthPassword,
                                   String userSelectedApplication,
                                   String userSelectedRelease, String assessmentTypeId,
                                   String entitlementId,
                                   String entitlementFrequencyType, String userSelectedEntitlement,
                                   String selectedDynamicGeoLocation, String selectedNetworkAuthType,
                                   boolean timeBoxChecked,
                                  String selectedApiType,
                                  String openApiRadioSource, String openApiFileSource, String openApiurl, String apiKey,
                                  String postmanFile,
                                  String graphQlSource,String graphQlUpload, String graphQlUrl, String graphQLSchemeType, String graphQlApiHost, String graphQlApiServicePath,
                                  String grpcupload, String grpcSchemeType, String grpcApiHost, String grpcApiServicePath) {

        authModel = new AuthenticationModel(overrideGlobalConfig, username, personalAccessToken, tenantId);


        model = new DastScanJobModel(overrideGlobalConfig, username, personalAccessToken, tenantId,
                releaseId, selectedReleaseType, webSiteUrl
                , dastEnv, scanTimebox, standardScanTypeExcludeUrlsRow, scanPolicyType, scanScope, selectedScanType
                , selectedDynamicTimeZone, webSiteLoginMacroEnabled,
                webSiteNetworkAuthSettingEnabled, enableRedundantPageDetection,
                webSiteNetworkAuthUserName, loginMacroId, workflowMacroId, allowedHost
                , webSiteNetworkAuthPassword, userSelectedApplication,
                userSelectedRelease, assessmentTypeId, entitlementId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedDynamicGeoLocation, selectedNetworkAuthType,
                selectedApiType,openApiRadioSource,openApiFileSource, openApiurl, apiKey,
                postmanFile,
                graphQlSource, graphQlUpload, graphQlUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                grpcupload, grpcSchemeType, grpcApiHost, grpcApiServicePath);

    }

    private FodApiConnection getApiConnection() throws FormValidation {

        return ApiConnectionFactory.createApiConnection(this.getAuthModel(), false, null, null);
    }

    public int getScanId() {
        return scanId;
    }

    public DastScanJobModel getModel() {
        return model;
    }

    public AuthenticationModel getAuthModel() {
        return authModel;
    }

    private List<String> ValidateModel(FodApiConnection api, PrintStream logger, String scanType) throws FormValidation {

        try {

            switch (scanType)
            {
                case "Standard":



                    break;

                case "Workflow-driven":
                    break;

            }


        } catch (Exception ex) {
        }
        return null;
    }

    List<String> ValidateStandardScanType(DastScanJobModel model){

        List<String> error = new ArrayList<>();
        if(model.getWebSiteUrl().isEmpty())
        {
            error.add("Invalid Web Site URL");

        }
        return null;
    }

    public void saveReleaseSettingsForWebSiteScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq, String loginMacroId,
                                                  String timeZone, String scanPolicy, String webSiteAssessmentUrl,
                                                  boolean scanScope,
                                                  boolean redundantPageDetection, String scanEnvironment,
                                                  boolean requireNetworkAuth, boolean requireLoginMacroAuth,
                                                  String networkAuthUserName, String networkAuthPassword
            , String networkAuthType, String timeboxScan

    )
            throws IllegalArgumentException, IOException {

        DastScanController dynamicController = new DastScanController(getApiConnection(), null, Utils.createCorrelationId());

        try {

            PutDastWebSiteScanReqModel dynamicScanSetupReqModel;
            dynamicScanSetupReqModel = new PutDastWebSiteScanReqModel();
            dynamicScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dynamicScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dynamicScanSetupReqModel.setTimeZone(timeZone);
            dynamicScanSetupReqModel.setEnableRedundantPageDetection(redundantPageDetection);
            dynamicScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));

            if (!Objects.equals(loginMacroId, "") && loginMacroId != null && requireLoginMacroAuth) {
                dynamicScanSetupReqModel.setLoginMacroFileId(Integer.parseInt(loginMacroId));
            }

            try {
                if (!timeboxScan.isEmpty())
                    dynamicScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(timeboxScan));
            } catch (NumberFormatException exception) {
                // Should warn not throw in front end.
                throw new IllegalArgumentException(" value for TimeBox Scan");
            }

            dynamicScanSetupReqModel.setPolicy(scanPolicy);
            dynamicScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireLoginMacroAuth && (!Objects.equals(loginMacroId, "") && loginMacroId != null))
                if (Integer.parseInt(loginMacroId) != 0) {
                    dynamicScanSetupReqModel.setRequiresSiteAuthentication(true);
                }
            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkSetting = dynamicScanSetupReqModel.getNetworkAuthenticationSettings();
                networkSetting.setPassword(networkAuthPassword);
                networkSetting.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkSetting.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);

                networkSetting.setRequiresNetworkAuthentication(true);

                dynamicScanSetupReqModel.setNetworkAuthenticationSettings(networkSetting);
            }

            if (scanScope) //if true => Restrict scan to URL directories and subdirectories
                dynamicScanSetupReqModel.setRestrictToDirectoryAndSubdirectories(scanScope);
            else
                dynamicScanSetupReqModel.setRestrictToDirectoryAndSubdirectories(true);

            dynamicScanSetupReqModel.setDynamicSiteUrl(webSiteAssessmentUrl);

            PutDastScanSetupResponse response = dynamicController.putDastWebSiteScanSettings(Integer.parseInt(userSelectedRelease),
                    dynamicScanSetupReqModel);

            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {

                throw new Exception("Failed to save scan settings for release id: " + userSelectedRelease);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to save scan settings for release id = " + userSelectedRelease, e);
        }
    }


    public void saveReleaseSettingsForWorkflowDrivenScan(String userSelectedRelease, String assessmentTypeID,
                                                         String entitlementId, String entitlementFreq, String workflowMacroId,
                                                         String workflowMacroHosts,
                                                         String timeZone, String scanPolicy,
                                                         boolean redundantPageDetection, String scanEnvironment,
                                                         boolean requireNetworkAuth,
                                                         String networkAuthUserName, String networkAuthPassword
            , String networkAuthType)
            throws IllegalArgumentException, IOException {

        DastScanController dynamicController = new DastScanController(getApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastWorkflowDrivenScanReqModel dastWorkflowScanSetupReqModel;
            dastWorkflowScanSetupReqModel = new PutDastWorkflowDrivenScanReqModel();
            dastWorkflowScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastWorkflowScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastWorkflowScanSetupReqModel.setTimeZone(timeZone);
            dastWorkflowScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastWorkflowScanSetupReqModel.setEnableRedundantPageDetection(redundantPageDetection);

            if (workflowMacroId.isEmpty() || workflowMacroHosts.isEmpty()) {

                throw new IllegalArgumentException(String.format("WorkflowMacro FileId=%s or WorkflowHost=%s not set for release Id={%s}"
                        , workflowMacroId, workflowMacroHosts, userSelectedRelease));
            } else {
                //Support for only one file upload. need to add the validation as part of this.
                dastWorkflowScanSetupReqModel.workflowDrivenMacro = new ArrayList<>();
                WorkflowDrivenMacro wrkDrivenMacro = new WorkflowDrivenMacro();
                wrkDrivenMacro.fileId = Integer.parseInt(workflowMacroId);
                wrkDrivenMacro.allowedHosts = workflowMacroHosts.split(",");
                dastWorkflowScanSetupReqModel.workflowDrivenMacro.add(wrkDrivenMacro);
            }

            dastWorkflowScanSetupReqModel.setPolicy(scanPolicy);
            dastWorkflowScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastWorkflowScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);
                networkAuthentication.setRequiresNetworkAuthentication(true);
                dastWorkflowScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }

            PutDastScanSetupResponse response = dynamicController.putDastWorkflowDrivenScanSettings(Integer.parseInt(userSelectedRelease),
                    dastWorkflowScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                throw new Exception(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }
    public void saveReleaseSettingsForOpenApiScan(String userSelectedRelease, String assessmentTypeID,
                                                         String entitlementId, String entitlementFreq,
                                                         String timeZone,
                                                         boolean allowSameHostRedirect,
                                                         String scanEnvironment,
                                                         boolean requireNetworkAuth,
                                                         String networkAuthUserName, String networkAuthPassword,
                                                         String networkAuthType,String openApiSourceType, String sourceUrn, String openApiKey)
            throws IllegalArgumentException, IOException {

        DastScanController dynamicController = new DastScanController(getApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastAutomatedOpenApiReqModel dastOpenApiScanSetupReqModel;
            dastOpenApiScanSetupReqModel = new PutDastAutomatedOpenApiReqModel();
            dastOpenApiScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastOpenApiScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastOpenApiScanSetupReqModel.setTimeZone(timeZone);
            dastOpenApiScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastOpenApiScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastOpenApiScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);
                networkAuthentication.setRequiresNetworkAuthentication(true);
                dastOpenApiScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            dastOpenApiScanSetupReqModel.setSourceType(openApiSourceType);
            dastOpenApiScanSetupReqModel.setSourceUrn(sourceUrn);
            dastOpenApiScanSetupReqModel.setApiKey(openApiKey);
            if (sourceUrn == null || sourceUrn == "") {
                throw new IllegalArgumentException(String.format("OpenAPI Source= %s  not set for release Id={%s}"
                        , sourceUrn, userSelectedRelease));
            }
            else {
                dastOpenApiScanSetupReqModel.SourceUrn = sourceUrn;
            }

            PutDastScanSetupResponse response = dynamicController.putDastOpenApiScanSettings(Integer.parseInt(userSelectedRelease),
                    dastOpenApiScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                throw new Exception(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }

    public void saveReleaseSettingsForGraphQlScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq,
                                                  String timeZone,
                                                  boolean allowSameHostRedirect,
                                                  String scanEnvironment,
                                                  boolean requireNetworkAuth,
                                                  String networkAuthUserName, String networkAuthPassword,
                                                  String networkAuthType, String sourceUrn, String sourceType,
                                                  String schemeType, String host, String servicePath)
            throws IllegalArgumentException, IOException {

        DastScanController dynamicController = new DastScanController(getApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastAutomatedGraphQlReqModel dastGraphQlScanSetupReqModel;
            dastGraphQlScanSetupReqModel = new PutDastAutomatedGraphQlReqModel();
            dastGraphQlScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastGraphQlScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastGraphQlScanSetupReqModel.setTimeZone(timeZone);
            dastGraphQlScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastGraphQlScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastGraphQlScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);
                networkAuthentication.setRequiresNetworkAuthentication(true);
                dastGraphQlScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            dastGraphQlScanSetupReqModel.setSourceType(sourceType);
            dastGraphQlScanSetupReqModel.setSchemeType(schemeType);
            dastGraphQlScanSetupReqModel.setServicePath(servicePath);
            dastGraphQlScanSetupReqModel.setHost(host);
            dastGraphQlScanSetupReqModel.setSourceUrn(sourceUrn);


            PutDastScanSetupResponse response = dynamicController.putDastGraphQLScanSettings(Integer.parseInt(userSelectedRelease),
                    dastGraphQlScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                throw new Exception(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }

    public void saveReleaseSettingsForGrpcScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq,
                                                  String timeZone,
                                                  boolean allowSameHostRedirect,
                                                  String scanEnvironment,
                                                  boolean requireNetworkAuth,
                                                  String networkAuthUserName, String networkAuthPassword,
                                                  String networkAuthType,
                                                  String grpcFileId, String schemeType, String host, String servicePath)
            throws IllegalArgumentException, IOException {

        DastScanController dynamicController = new DastScanController(getApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastAutomatedGrpcReqModel dastgrpcScanSetupReqModel;
            dastgrpcScanSetupReqModel = new PutDastAutomatedGrpcReqModel();
            dastgrpcScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastgrpcScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastgrpcScanSetupReqModel.setTimeZone(timeZone);
            dastgrpcScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastgrpcScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastgrpcScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);
                networkAuthentication.setRequiresNetworkAuthentication(true);
                dastgrpcScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            int fileId = Integer.parseInt(grpcFileId);
            if (fileId == 0) {
                throw new IllegalArgumentException(String.format("GRPC Source= %s  not set for release Id={%s}"
                         ,fileId, userSelectedRelease));
            }
            else {
                dastgrpcScanSetupReqModel.FileId = Integer.parseInt(grpcFileId);
            }

            PutDastScanSetupResponse response = dynamicController.putDastGrpcScanSettings(Integer.parseInt(userSelectedRelease),
                    dastgrpcScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                throw new Exception(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }

    public void saveReleaseSettingsForPostmanScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq,
                                                  String timeZone,
                                                  boolean allowSameHostRedirect,
                                                  String scanEnvironment,
                                                  boolean requireNetworkAuth,
                                                  String networkAuthUserName, String networkAuthPassword,
                                                  String networkAuthType, String postmanIdCollection)
            throws IllegalArgumentException, IOException {

        DastScanController dynamicController = new DastScanController(getApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastAutomatedPostmanReqModel dastPostmanScanSetupReqModel;
            dastPostmanScanSetupReqModel = new PutDastAutomatedPostmanReqModel();
            dastPostmanScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastPostmanScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastPostmanScanSetupReqModel.setTimeZone(timeZone);
            dastPostmanScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastPostmanScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);


            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastPostmanScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);
                networkAuthentication.setRequiresNetworkAuthentication(true);
                dastPostmanScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            if (postmanIdCollection == null) {
                throw new IllegalArgumentException(String.format("Postman Scan - one of the id is  not set for release Id={%s}"
                        , userSelectedRelease));
            }
            else {
                    dastPostmanScanSetupReqModel.setCollectionFileIds(ConvertStringtoIntArr(postmanIdCollection));
                }

            PutDastScanSetupResponse response = dynamicController.putDastPostmanScanSettings(Integer.parseInt(userSelectedRelease),
                    dastPostmanScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                throw new Exception(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }
    public int[] ConvertStringtoIntArr(String fileIds) {
        String[] postmanIds = fileIds.split(",");
        int[] postmanIdArr = new int[postmanIds.length];
        for (int i = 0; i < postmanIds.length; i++) {
            postmanIdArr[i] = Integer.parseInt(postmanIds[i]);
        }
       return postmanIdArr;
    }


    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace,
                        Launcher launcher, TaskListener listener, String correlationId) {
        final PrintStream logger = listener.getLogger();
        FodApiConnection apiConnection = null;

        try {
            taskListener.set(listener);
            // check to see if sensitive fields are encrypted. If not halt scan and recommend encryption.
            if (authModel != null) {
                if (authModel.getOverrideGlobalConfig()) {
                    if (!Utils.isCredential(authModel.getPersonalAccessToken())) {
                        build.setResult(Result.UNSTABLE);
                        logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                        return;
                    }
                } else {
                    if (Objects.requireNonNull(GlobalConfiguration.all().get(FodGlobalDescriptor.class)).getAuthTypeIsApiKey()) {
                        if (!Utils.isCredential(Objects.requireNonNull(GlobalConfiguration.all().get(FodGlobalDescriptor.class)).getOriginalClientSecret())) {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return;
                        }
                    } else {
                        if (!Utils.isCredential(Objects.requireNonNull(GlobalConfiguration.all().get(FodGlobalDescriptor.class)).getOriginalPersonalAccessToken())) {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return;
                        }
                    }
                }
            }
            Result currentResult = build.getResult();
            if (Result.FAILURE.equals(currentResult)
                    || Result.ABORTED.equals(currentResult)
                    || Result.UNSTABLE.equals(currentResult)) {

                logger.println("Error: Build Failed or Unstable.  Halting with Fortify on Demand upload.");
                return;
            }
            logger.println("Correlation Id = " + correlationId);

            Integer releaseId = Integer.parseInt(model.get_releaseId());

            try {
                apiConnection = ApiConnectionFactory.createApiConnection(this.getAuthModel(), false, null, null);
            } catch (IOException e) {
                build.setResult(Result.FAILURE);
                throw new RuntimeException(e);
            }
            DastScanController dynamicController = new DastScanController(apiConnection, null, Utils.createCorrelationId());
            PostDastStartScanResponse response = dynamicController.StartDastScan(releaseId);

            if (response.errors == null && response.getScanId() > 0) {
                build.setResult(Result.SUCCESS);
                build.setDescription(String.format("Successfully triggered Dynamic scan for scan id %d", response.getScanId()));
            } else {
                build.setResult(Result.FAILURE);
                build.setDescription(String.format("Failed to trigger Dynamic scan for release id %d with error %s", releaseId, ""));
            }


        } catch (IOException e) {
            build.setResult(Result.FAILURE);
            throw new RuntimeException(e);
        }
    }

    public static ListBoxModel doFillEntitlementPreferenceItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.EntitlementPreferenceType preferenceType : FodEnums.EntitlementPreferenceType.values()) {
            items.add(new ListBoxModel.Option(preferenceType.toString(), preferenceType.getValue()));
        }

        return items;
    }

    public static ListBoxModel doFillStringCredentialsItems(@AncestorInPath Job job) {
        job.checkPermission(Item.CONFIGURE);
        ListBoxModel items = CredentialsProvider.listCredentials(
                StringCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                null,
                null
        );

        return items;
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
    public static ListBoxModel doFillScanPolicyItems() {
        return doFillFromEnum(FodEnums.DastPolicy.class);
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillInProgressScanActionTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.InProgressScanActionType scanActionType : FodEnums.InProgressScanActionType.values()) {
            items.add(new ListBoxModel.Option(scanActionType.toString(), scanActionType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillInProgressBuildResultTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.InProgressBuildResultType buildResultType : FodEnums.InProgressBuildResultType.values()) {
            items.add(new ListBoxModel.Option(buildResultType.toString(), buildResultType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillSelectedReleaseTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.SelectedReleaseType selectedReleaseType : FodEnums.SelectedReleaseType.values()) {
            items.add(new ListBoxModel.Option(selectedReleaseType.toString(), selectedReleaseType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillSelectedScanCentralBuildTypeItems() {
        return doFillFromEnum(FodEnums.SelectedScanCentralBuildType.class);
    }

    private static <T extends Enum<T>> ListBoxModel doFillFromEnum(Class<T> enumClass) {
        ListBoxModel items = new ListBoxModel();
        for (T selected : EnumSet.allOf(enumClass)) {
            items.add(new ListBoxModel.Option(selected.toString(), selected.name()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static GenericListResponse<ApplicationApiResponse> customFillUserSelectedApplicationList(String searchTerm, int offset, int limit, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getApplicationList(searchTerm, offset, limit);
    }

    public static org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> customFillUserApplicationById(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> result = applicationsController.getApplicationById(applicationId);

        return result;
    }

    public static List<MicroserviceApiResponse> customFillUserSelectedMicroserviceList(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getMicroserviceListByApplication(applicationId);
    }

    public static GenericListResponse<ReleaseApiResponse> customFillUserSelectedReleaseList(int applicationId, int microserviceId, String searchTerm, Integer offset, Integer limit, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getReleaseListByApplication(applicationId, microserviceId, searchTerm, offset, limit);
    }

    public static org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> customFillUserReleaseById(int releaseId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> result = applicationsController.getReleaseById(releaseId);

        return result;
    }

    public static EntitlementSettings customFillEntitlementSettings(int releaseId, AuthenticationModel authModel) throws IOException {
        return new EntitlementSettings(
                1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
                1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
                1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
                1, 1, false);
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @POST
    public static FormValidation doTestPersonalAccessTokenConnection(final String username,
                                                                     final String personalAccessToken,
                                                                     final String tenantId,
                                                                     @AncestorInPath Job job) throws FormValidation {
        job.checkPermission(Item.CONFIGURE);
        FodApiConnection testApi;
        String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
        String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
        String plainTextPersonalAccessToken = Utils.retrieveSecretDecryptedValue(personalAccessToken);
        if (Utils.isNullOrEmpty(isValidUrl(baseUrl)))
            return FormValidation.error(FOD_URL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(isValidUrl(apiUrl)))
            return FormValidation.error(FOD_URL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(username))
            return FormValidation.error("Username is empty!");
        if (!Utils.isCredential(personalAccessToken))
            return FormValidation.error("Personal Access Token is empty!");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");
        testApi = new FodApiConnection(tenantId + "\\" + username, plainTextPersonalAccessToken, baseUrl, apiUrl, FodEnums.GrantType.PASSWORD, "api-tenant", false, null, null);
        return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);

    }
}



