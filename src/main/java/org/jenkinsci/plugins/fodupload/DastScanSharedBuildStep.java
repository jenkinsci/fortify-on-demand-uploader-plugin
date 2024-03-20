package org.jenkinsci.plugins.fodupload;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.model.Result;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.CharEncoding;
import org.jenkinsci.plugins.fodupload.Config.FodGlobalConstants;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.DastScanController;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PostDastStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PutDastScanSetupResponse;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.jenkinsci.plugins.fodupload.Utils.*;

public class DastScanSharedBuildStep {
    private final DastScanJobModel model;
    private final AuthenticationModel authModel;
    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";
    public static final String BSI_TOKEN = "useBsiToken";
    private int scanId;
    private PrintStream _printStream;

    private transient FodApiConnection _fodApiConnection;

    public DastScanSharedBuildStep(DastScanJobModel model, AuthenticationModel authModel) {
        this.model = model;
        this.authModel = authModel;
    }

    public DastScanSharedBuildStep(boolean overrideGlobalConfig, String username,
                                   String personalAccessToken, String tenantId,
                                   String releaseId,
                                   String dastEnv,
                                   String scanTimebox,
                                   String scanPolicyType, boolean scanScope,
                                   String selectedScanType, String selectedDynamicTimeZone,
                                   String networkAuthUserName,
                                   String networkAuthPassword,
                                   String userSelectedApplication,
                                   String assessmentTypeId,
                                   String entitlementId,
                                   String entitlementFrequencyType, String userSelectedEntitlement,
                                   boolean timeBoxChecked,
                                   String selectedApiType,
                                   String openApiRadioSource, String openApiFileSource, String openApiurl, String apiKey,
                                   String postmanFile,
                                   String graphQlSource, String graphQlUpload, String graphQlUrl, String graphQLSchemeType, String graphQlApiHost, String graphQlApiServicePath,
                                   String grpcUpload, String grpcSchemeType, String grpcApiHost,
                                   String grpcApiServicePath, String openApiFilePath,
                                   String postmanFilePath, String graphQLFilePath,
                                   String grpcFilePath, boolean requestFalsePositiveRemoval
    ) {

        authModel = new AuthenticationModel(overrideGlobalConfig, username, personalAccessToken, tenantId);
        model = new DastScanJobModel(overrideGlobalConfig, username, personalAccessToken, tenantId,
                releaseId, dastEnv, scanTimebox, scanPolicyType, scanScope, selectedScanType
                , selectedDynamicTimeZone, networkAuthUserName
                , networkAuthPassword, userSelectedApplication
                , assessmentTypeId, entitlementId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedApiType, openApiRadioSource, openApiFileSource, openApiurl, apiKey,
                postmanFile,
                graphQlSource, graphQlUpload, graphQlUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                grpcUpload, grpcSchemeType, grpcApiHost, grpcApiServicePath, openApiFilePath, postmanFilePath, graphQLFilePath, grpcFilePath, requestFalsePositiveRemoval);

    }

    public DastScanSharedBuildStep(Boolean overrideGlobalConfig, String username,
                                   String tenantId, String personalAccessToken, String releaseId,
                                   String webSiteUrl, String dastEnv, String scanTimebox, String excludedUrlList,
                                   String scanPolicy, boolean scanScope, String selectedScanType,
                                   String selectedDynamicTimeZone, boolean enableRedundantPageDetection, String loginMacroFilePath,
                                   String workflowMacroPath,
                                   int loginMacroId, String workflowMacroId, String allowedHost, String networkAuthUserName,
                                   String networkAuthPassword, String applicationId, String assessmentTypeId, String entitlementId,
                                   String entitlementFrequencyType, String selectedNetworkAuthType, boolean timeBoxChecked,
                                   boolean requestLoginMacroFileCreation, String loginMacroPrimaryUserName, String loginMacroPrimaryPassword,
                                   String loginMacroSecondaryUsername, String loginMacroSecondaryPassword, boolean requestFalsePositiveRemoval) {

        authModel = new AuthenticationModel(overrideGlobalConfig, username, personalAccessToken, tenantId);
        model = new DastScanJobModel(overrideGlobalConfig, username, personalAccessToken, tenantId,
                releaseId, webSiteUrl
                , dastEnv, scanTimebox, scanScope, selectedScanType, scanPolicy
                , selectedDynamicTimeZone
                , enableRedundantPageDetection,
                networkAuthUserName, loginMacroFilePath, workflowMacroPath, loginMacroId, workflowMacroId, allowedHost
                , networkAuthPassword
                , assessmentTypeId, entitlementId,
                entitlementFrequencyType
                , selectedNetworkAuthType, timeBoxChecked,
                requestLoginMacroFileCreation, loginMacroPrimaryUserName, loginMacroPrimaryPassword, loginMacroSecondaryUsername,
                loginMacroSecondaryPassword, requestFalsePositiveRemoval, excludedUrlList);
    }

    public FodApiConnection getFodApiConnection() throws Exception {
        if (this._fodApiConnection == null) {
            throw new Exception("FOD API connection not set");
        }
        return this._fodApiConnection;
    }

    public void setFodApiConnection
            (FodApiConnection apiConnection) {
        this._fodApiConnection = apiConnection;
    }

    public void setLogger(PrintStream printStream) {
        this._printStream = printStream;
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

    public List<String> validateAuthModel(boolean overrideGlobalAuth, String username, String tenantId, String personalAccessToken) throws FormValidation {
        List<String> errors = new ArrayList<>();

        if (overrideGlobalAuth && (Utils.isNullOrEmpty(username) || Utils.isNullOrEmpty(tenantId) || Utils.isNullOrEmpty(personalAccessToken))) {
            errors.add("Personal access token override requires all 3 be provided: username, personalAccessToken, tenantId");
        }

        return errors;
    }

    public List<String> validateForAutoProv() {
        List<String> errors = new ArrayList<>();
        //Check for mandate fields based on scan type.
        if (Utils.isNullOrEmpty(this.model.getSelectedScanType())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanTypeNotFound);
        }

        if (Utils.isNullOrEmpty(this.model.getEntitlementId())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanEntitlementIdNotFound);
        }
       if (Utils.isNullOrEmpty(this.model.getEntitlementFrequencyType())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanEntitlementFreqTypeNotFound);
        }

        if (Utils.isNullOrEmpty(this.model.getSelectedDynamicTimeZone())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastTimeZoneNotFound);
        }
        if (getModel().isWebSiteNetworkAuthEnabled()) {
            if (Utils.isNullOrEmpty(getModel().getNetworkAuthPassword())) {
                errors.add(FodGlobalConstants.FodDastValidation.DastScanNetworkPasswordNotFound);
            } else if (Utils.isNullOrEmpty(getModel().getNetworkAuthUserName())) {
                errors.add(FodGlobalConstants.FodDastValidation.DastScanNetworkUserNameNotFound);
            } else if (Utils.isNullOrEmpty(getModel().getNetworkAuthType())) {
                errors.add(FodGlobalConstants.FodDastValidation.DastScanNetworkAuthTypeNotFound);
            }
        }
        FodEnums.DastScanType dastScanType;
        if (Objects.equals(this.model.getSelectedScanType(), FodEnums.DastScanType.Workflow.toString())) {
            dastScanType = FodEnums.DastScanType.Workflow; //hack to match the workflow-driven enum, as naming an enum using - is not valid one
        } else
            dastScanType = FodEnums.DastScanType.valueOf(this.model.getSelectedScanType());

        switch (dastScanType) {
            case Website:
                if (Utils.isNullOrEmpty(this.model.getWebSiteUrl())) {
                    errors.add(FodGlobalConstants.FodDastValidation.DastPipelineWebSiteUrlNotFound);
                }
                if (Utils.isNullOrEmpty(this.model.getScanPolicyType()))
                    errors.add(FodGlobalConstants.FodDastValidation.DastScanPolicyNotFound);
                break;
            case Workflow:
                if (Utils.isNullOrEmpty(this.model.getWorkflowMacroFilePath()) && this.model.getWorkflowMacroFileId() <= 0)
                    errors.add(FodGlobalConstants.FodDastValidation.DastPipelineWorkflowMacroFilePathOrMacroIdNotFound);

                if (Utils.isNullOrEmpty(this.model.getScanPolicyType()))
                    errors.add(FodGlobalConstants.FodDastValidation.DastScanPolicyNotFound);
                break;
            case API: {
                if (Utils.isNullOrEmpty(this.model.getSelectedApiType())) {
                    errors.add(FodGlobalConstants.FodDastValidation.DastScanAPITypeNotFound);
                    break;
                }

                switch (this.model.getSelectedApiType()) {
                    case "openApi":
                        if (isNullOrEmpty(this.model.getSelectedOpenApiurl()) && isNullOrEmpty(this.model.getSelectedOpenApiFileSource())
                                && isNullOrEmpty(this.model.getOpenApiFilePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanOpenApiSourceNotFound);
                        }
                        break;
                    case "grpc":
                        if (isNullOrEmpty(this.model.getSelectedGrpcUpload())
                                && isNullOrEmpty(this.model.getGrpcFilePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcSourceNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGrpcApiHost())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcHostNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGrpcApiServicePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcServicePathNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGrpcSchemeType())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcSchemeTypeNotFound);
                        }
                        break;
                    case "graphQl":
                        if (isNullOrEmpty(this.model.getSelectedGraphQlUpload())
                                && isNullOrEmpty(this.model.getGraphQlFilePath())
                                && isNullOrEmpty(this.model.getSelectedGraphQlUrl())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlSourceNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGraphQlApiHost())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlHostNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGraphQlApiServicePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlServicePathNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGraphQLSchemeType())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlSchemeTypeNotFound);
                        }
                        break;
                    case "postman":
                        if (isNullOrEmpty(this.model.getSelectedPostmanFile()) && isNullOrEmpty(this.model.getPostmanFilePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanPostmanSourceNotFound);
                        }
                        break;
                    default:
                        errors.add(FodGlobalConstants.FodDastValidation.DastScanAPITypeNotFound);
                        break;

                }
                break;
            }
            default:
                errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanTypeNotFound);
                break;
        }
        return errors;
    }

    public List<String> validateModel() {

        List<String> errors = new ArrayList<>();

        //Check for mandate fields based on scan type.
        if (Utils.isNullOrEmpty(this.model.getSelectedScanType())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanTypeNotFound);
        }
        if (Utils.isNullOrEmpty(this.model.get_releaseId())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineReleaseIdNotFound);
        }
        if (Utils.isNullOrEmpty(this.model.getEntitlementId())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanEntitlementIdNotFound);
        }

        if (Utils.isNullOrEmpty(this.model.getEntitlementFrequencyType())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanEntitlementFreqTypeNotFound);
        }

        if (Utils.isNullOrEmpty(this.model.getSelectedDynamicTimeZone())) {
            errors.add(FodGlobalConstants.FodDastValidation.DastTimeZoneNotFound);
        }

        if (getModel().isWebSiteNetworkAuthEnabled()) {
            if (getModel().getNetworkAuthPassword().isEmpty()) {
                errors.add(FodGlobalConstants.FodDastValidation.DastScanNetworkPasswordNotFound);
            } else if (getModel().getNetworkAuthUserName().isEmpty()) {

                errors.add(FodGlobalConstants.FodDastValidation.DastScanNetworkUserNameNotFound);
            } else if (getModel().getNetworkAuthType().isEmpty()) {
                errors.add(FodGlobalConstants.FodDastValidation.DastScanNetworkAuthTypeNotFound);
            }
        }
        FodEnums.DastScanType dastScanType;
        if (Objects.equals(this.model.getSelectedScanType(), FodEnums.DastScanType.Workflow.toString())) {
            dastScanType = FodEnums.DastScanType.Workflow;
        } else
            dastScanType = FodEnums.DastScanType.valueOf(this.model.getSelectedScanType());

        switch (dastScanType) {
            case Website:
                if (this.model.getWebSiteUrl().isEmpty()) {
                    errors.add(FodGlobalConstants.FodDastValidation.DastPipelineWebSiteUrlNotFound);
                }
                if (this.model.getScanPolicyType().isEmpty())
                    errors.add(FodGlobalConstants.FodDastValidation.DastScanPolicyNotFound);

                break;
            case Workflow:
                if (Utils.isNullOrEmpty(this.model.getWorkflowMacroFilePath()) && this.model.getWorkflowMacroFileId() <= 0)
                    errors.add(FodGlobalConstants.FodDastValidation.DastPipelineWorkflowMacroFilePathOrMacroIdNotFound);

                if (this.model.getAllowedHost() == null || this.model.getAllowedHost().isEmpty())
                    errors.add(FodGlobalConstants.FodDastValidation.DastWorkflowAllowedHostNotFound);

                if (this.model.getScanPolicyType().isEmpty())
                    errors.add(FodGlobalConstants.FodDastValidation.DastScanPolicyNotFound);
                break;
            case API: {
                if (isNullOrEmpty(this.model.getSelectedApiType())) {
                    errors.add(FodGlobalConstants.FodDastValidation.DastScanAPITypeNotFound);
                    break;
                }
                switch (this.model.getSelectedApiType()) {
                    case "openApi":
                        if (isNullOrEmpty(this.model.getSelectedOpenApiurl()) && isNullOrEmpty(this.model.getSelectedOpenApiFileSource())
                                && isNullOrEmpty(this.model.getOpenApiFilePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanOpenApiSourceNotFound);
                        }
                        break;
                    case "grpc":
                        if (isNullOrEmpty(this.model.getSelectedGrpcUpload())
                                && isNullOrEmpty(this.model.getGrpcFilePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcSourceNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGrpcApiHost())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcHostNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGrpcApiServicePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcServicePathNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGrpcSchemeType())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGrpcSchemeTypeNotFound);
                        }
                        break;
                    case "graphQl":
                        if (isNullOrEmpty(this.model.getSelectedGraphQlUpload())
                                && isNullOrEmpty(this.model.getGraphQlFilePath())
                                && isNullOrEmpty(this.model.getSelectedGraphQlUrl())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlSourceNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGraphQlApiHost())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlHostNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGraphQlApiServicePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlServicePathNotFound);
                        }
                        if (isNullOrEmpty(this.model.getSelectedGraphQLSchemeType())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanGraphQlSchemeTypeNotFound);
                        }
                        break;
                    case "postman":
                        if (isNullOrEmpty(this.model.getSelectedPostmanFile()) && isNullOrEmpty(this.model.getPostmanFilePath())) {
                            errors.add(FodGlobalConstants.FodDastValidation.DastScanPostmanSourceNotFound);
                        }
                        break;
                    default:
                        errors.add(FodGlobalConstants.FodDastValidation.DastScanAPITypeNotFound);
                        break;
                }
                break;
            }
            default:
                errors.add(FodGlobalConstants.FodDastValidation.DastPipelineScanTypeNotFound);
                break;
        }
        return errors;
    }

    public void saveReleaseSettingsForWebSiteScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq, String loginMacroId,
                                                  String timeZone, String scanPolicy, String webSiteAssessmentUrl,
                                                  boolean scanScope,
                                                  boolean redundantPageDetection, String scanEnvironment,
                                                  String networkAuthUserName, String networkAuthPassword
            , String networkAuthType, String timeboxScan, boolean requestLoginMacroFileCreation, String loginMacroPrimaryUserName,
                                                  String loginMacroPrimaryPassword,
                                                  String loginMacroSecondaryUsername, String loginMacroSecondaryPassword, boolean requestFalsePositiveRemoval, String excludedUrlList)
            throws Exception {

        DastScanController dynamicController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId());

        try {
            PutDastWebSiteScanReqModel dynamicScanSetupReqModel;
            dynamicScanSetupReqModel = new PutDastWebSiteScanReqModel();
            dynamicScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dynamicScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dynamicScanSetupReqModel.setTimeZone(timeZone);
            dynamicScanSetupReqModel.setEnableRedundantPageDetection(redundantPageDetection);
            dynamicScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));

            if (!Utils.isNullOrEmpty(loginMacroId)) {
                if (Integer.parseInt(loginMacroId) > 0) {
                    dynamicScanSetupReqModel.setLoginMacroFileId(Integer.parseInt(loginMacroId));
                    dynamicScanSetupReqModel.setRequiresSiteAuthentication(true);
                }
                else
                {
                    dynamicScanSetupReqModel.setRequiresSiteAuthentication(false);
                }
            }
            try {
                if (!Utils.isNullOrEmpty(timeboxScan))
                    dynamicScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(timeboxScan));
                else
                    dynamicScanSetupReqModel.setTimeBoxInHours(null);
            } catch (NumberFormatException exception) {
                // Should warn not throw in front end.
                throw new IllegalArgumentException(" Invalid value for TimeBox Scan");
            }
            if (!Utils.isNullOrEmpty(scanPolicy))
                dynamicScanSetupReqModel.setPolicy(scanPolicy);

            dynamicScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (!Utils.isNullOrEmpty(networkAuthType) &&
                    !Utils.isNullOrEmpty(networkAuthPassword) && !Utils.isNullOrEmpty(networkAuthUserName)
            ) {
                PutDastScanSetupReqModel.NetworkAuthentication networkSetting = dynamicScanSetupReqModel.getNetworkAuthenticationSettings();
                networkSetting.setPassword(networkAuthPassword);
                networkSetting.setUserName(networkAuthUserName);
                dynamicScanSetupReqModel.setRequiresNetworkAuthentication(true);
                networkSetting.setNetworkAuthenticationType(networkAuthType);
                dynamicScanSetupReqModel.setNetworkAuthenticationSettings(networkSetting);
            }

            if (!isNullOrEmpty(loginMacroPrimaryUserName) &&
                    !isNullOrEmpty(loginMacroPrimaryPassword) &&
                    !isNullOrEmpty(loginMacroSecondaryUsername) &&
                    !isNullOrEmpty(loginMacroSecondaryPassword)) {
                dynamicScanSetupReqModel.setRequestLoginMacroFileCreation(true);
                dynamicScanSetupReqModel.setRequiresSiteAuthentication(true);
                LoginMacroFileCreationDetails loginMacroDetails = new LoginMacroFileCreationDetails();
                loginMacroDetails.setPrimaryUsername(loginMacroPrimaryUserName);
                loginMacroDetails.setPrimaryPassword(loginMacroPrimaryPassword);
                loginMacroDetails.setSecondaryUsername(loginMacroSecondaryUsername);
                loginMacroDetails.setSecondaryPassword(loginMacroSecondaryPassword);
                dynamicScanSetupReqModel.setLoginMacroFileCreationDetails(loginMacroDetails);
            }
            dynamicScanSetupReqModel.setRequestFalsePositiveRemoval(requestFalsePositiveRemoval);

            /* if scanScope is false then Scan Entire Host is set and if true scope is restricted to Directories and sub-directories. */
            dynamicScanSetupReqModel.setRestrictToDirectoryAndSubdirectories(scanScope);
            dynamicScanSetupReqModel.setDynamicSiteUrl(webSiteAssessmentUrl);

            if (!isNullOrEmpty(excludedUrlList)) {
                List<ExclusionDTO> exclusionsLists = new ArrayList<ExclusionDTO>();
                String[] urls = excludedUrlList.split(",");
                if (urls.length > 0) {
                    for (int cnt = 0; cnt < urls.length; cnt++) {
                        if (!Utils.isNullOrEmpty(urls[cnt])) {
                            ExclusionDTO urlToExclude = new ExclusionDTO();
                            urlToExclude.value = urls[cnt];
                            exclusionsLists.add(urlToExclude);
                        }
                    }
                }
                dynamicScanSetupReqModel.setExclusionsList(exclusionsLists);
            }
            PutDastScanSetupResponse response = dynamicController.saveDastWebSiteScanSettings(Integer.parseInt(userSelectedRelease),
                    dynamicScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                Utils.logger(_printStream, "Successfully saved settings for release id = " + userSelectedRelease);
            } else {

                String errMsg  = response.errors != null ? response.errors.stream().map(e -> e.errorCode + ":" + e.message)
                        .collect(Collectors.joining(",")) : "";
                throw new Exception(String.format("FOD API Error %s",errMsg));
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
                                                         String scanEnvironment,
                                                         String networkAuthUserName, String networkAuthPassword
            , String networkAuthType, boolean requestFalsePositiveRemoval)
            throws Exception {

        DastScanController dynamicController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId()
        );
        try {

            PutDastWorkflowDrivenScanReqModel dastWorkflowScanSetupReqModel;
            dastWorkflowScanSetupReqModel = new PutDastWorkflowDrivenScanReqModel();
            dastWorkflowScanSetupReqModel.setRequestFalsePositiveRemoval(requestFalsePositiveRemoval);
            dastWorkflowScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastWorkflowScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastWorkflowScanSetupReqModel.setTimeZone(timeZone);
            if (scanPolicy != null && !scanPolicy.isEmpty())
                dastWorkflowScanSetupReqModel.setPolicy(scanPolicy);

            dastWorkflowScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));

            if (workflowMacroId.isEmpty() || Integer.parseInt(workflowMacroId) <= 0 || workflowMacroHosts.isEmpty()) {

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
            if (scanPolicy != null && !scanPolicy.isEmpty()) {
                dastWorkflowScanSetupReqModel.setPolicy(scanPolicy);
            }
            dastWorkflowScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (networkAuthType != null &&
                    networkAuthPassword != null && networkAuthUserName != null
                    && !networkAuthType.isEmpty()
                    && !networkAuthPassword.isEmpty()
                    && !networkAuthUserName.isEmpty()) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastWorkflowScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);
                dastWorkflowScanSetupReqModel.setRequiresNetworkAuthentication(true);
                networkAuthentication.setNetworkAuthenticationType(networkAuthType);
                dastWorkflowScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }

            PutDastScanSetupResponse response = dynamicController.saveDastWorkflowDrivenScanSettings(Integer.parseInt(userSelectedRelease),
                    dastWorkflowScanSetupReqModel);

            if (response.isSuccess && response.errors == null) {
                Utils.logger(_printStream, "Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                String errMsg  = response.errors != null ? response.errors.stream().map(e -> e.errorCode + ":" + e.message)
                            .collect(Collectors.joining(",")) : "";
                throw new Exception(String.format("FOD API Error %s",errMsg));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }

    public PatchDastFileUploadResponse dastManifestFileUpload(String fileContent, String fileType, String filename) throws Exception {

        DastScanController dastScanController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId()
        );
        PatchDastScanFileUploadReq patchDastScanFileUploadReq = new PatchDastScanFileUploadReq();
        patchDastScanFileUploadReq.releaseId = getModel().get_releaseId();
        patchDastScanFileUploadReq.fileName = filename;

        FodEnums.DastScanFileTypes dastScanFileTypes = FodEnums.DastScanFileTypes.valueOf(fileType);
        switch (dastScanFileTypes) {
            case LoginMacro:
                patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.LoginMacro;
                break;
            case WorkflowDrivenMacro:
                patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.WorkflowDrivenMacro;
                break;
            case OpenAPIDefinition:
                patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.OpenAPIDefinition;
                break;
            case GraphQLDefinition:
                patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.GraphQLDefinition;
                break;
            case GRPCDefinition:
                patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.GRPCDefinition;
                break;
            case PostmanCollection:
                patchDastScanFileUploadReq.dastFileType = FodEnums.DastScanFileTypes.PostmanCollection;
                break;
            default:
                throw new IllegalArgumentException("Manifest upload file type is not set for the release: " + getModel().get_releaseId());
        }
        patchDastScanFileUploadReq.Content = fileContent.getBytes(CharEncoding.UTF_8);
        return dastScanController.dastFileUpload(patchDastScanFileUploadReq);
    }


    public PatchDastFileUploadResponse dastManifestFileUpload(FilePath workspace, String payLoadPath, PrintStream logger,
                                                              FodEnums.DastScanFileTypes fileType, FodApiConnection apiConnection) throws Exception {

        FilePath dastPayload = new FilePath(workspace, payLoadPath);
        if (!dastPayload.exists()) {
            logger.printf("FilePath for the Payload not constructed for releaseId %s%n", getModel().get_releaseId());
            throw new Exception(String.format("FilePath for the Payload not constructed for releaseId %s%n", getModel().get_releaseId()));
        }
        DastScanController dastScanController = new DastScanController(apiConnection, logger, Utils.createCorrelationId());
        PatchDastScanFileUploadReq patchDastScanFileUploadReq = new PatchDastScanFileUploadReq();
        patchDastScanFileUploadReq.releaseId = getModel().get_releaseId();
        patchDastScanFileUploadReq.dastFileType = fileType;
        return dastScanController.dastFileUpload(dastPayload, logger, patchDastScanFileUploadReq);
    }

    public void saveReleaseSettingsForOpenApiScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq,
                                                  String timeZone,
                                                  boolean allowSameHostRedirect,
                                                  String scanEnvironment,
                                                  boolean requireNetworkAuth,
                                                  String networkAuthUserName, String networkAuthPassword,
                                                  String networkAuthType, String openApiSourceType,
                                                  String sourceUrn, String openApiKey,
                                                  boolean requestFalsePositiveRemoval, String scanTimeBox)
            throws Exception {

        DastScanController dynamicController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId()
        );
        try {

            PutDastAutomatedOpenApiReqModel dastOpenApiScanSetupReqModel;
            dastOpenApiScanSetupReqModel = new PutDastAutomatedOpenApiReqModel();
            dastOpenApiScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastOpenApiScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastOpenApiScanSetupReqModel.setTimeZone(timeZone);
            dastOpenApiScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastOpenApiScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);
            dastOpenApiScanSetupReqModel.setRequestFalsePositiveRemoval(requestFalsePositiveRemoval);

            if (networkAuthType != null &&
                    networkAuthPassword != null && networkAuthUserName != null &&
                    !networkAuthPassword.isEmpty() && !networkAuthUserName.isEmpty() && !networkAuthType.isEmpty()) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastOpenApiScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);
                networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                dastOpenApiScanSetupReqModel.setRequiresNetworkAuthentication(true);
                dastOpenApiScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            dastOpenApiScanSetupReqModel.setSourceType(openApiSourceType);
            dastOpenApiScanSetupReqModel.setSourceUrn(sourceUrn);
            dastOpenApiScanSetupReqModel.setApiKey(openApiKey);
            if (sourceUrn == null || sourceUrn.isEmpty()) {
                throw new IllegalArgumentException(String.format("OpenAPI Source= %s  not set for release Id={%s}"
                        , sourceUrn, userSelectedRelease));
            } else {
                dastOpenApiScanSetupReqModel.setSourceUrn(sourceUrn);
            }
            if (!Utils.isNullOrEmpty(scanTimeBox))
                dastOpenApiScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(scanTimeBox));
            else
                dastOpenApiScanSetupReqModel.setTimeBoxInHours(null);

            PutDastScanSetupResponse response = dynamicController.putDastOpenApiScanSettings(Integer.parseInt(userSelectedRelease),
                    dastOpenApiScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {

                Utils.logger(_printStream, "Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                String errMsg  = response.errors != null ? response.errors.stream().map(e -> e.errorCode + ":" + e.message)
                        .collect(Collectors.joining(",")) : "";
                throw new Exception(String.format("FOD API Error %s",errMsg));

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
                                                  String schemeType, String host, String servicePath,
                                                  boolean requestFalsePositiveRemoval, String scanTimeBox)
            throws Exception {

        DastScanController dynamicController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId()
        );
        try {

            PutDastAutomatedGraphQlReqModel dastGraphQlScanSetupReqModel;
            dastGraphQlScanSetupReqModel = new PutDastAutomatedGraphQlReqModel();
            dastGraphQlScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastGraphQlScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastGraphQlScanSetupReqModel.setTimeZone(timeZone);
            dastGraphQlScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastGraphQlScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);
            dastGraphQlScanSetupReqModel.setRequestFalsePositiveRemoval(requestFalsePositiveRemoval);

            if (!networkAuthType.isEmpty() && !networkAuthPassword.isEmpty() && !networkAuthUserName.isEmpty()) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastGraphQlScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);
                networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                dastGraphQlScanSetupReqModel.setRequiresNetworkAuthentication(true);
                dastGraphQlScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            dastGraphQlScanSetupReqModel.setSourceType(sourceType);
            dastGraphQlScanSetupReqModel.setSchemeType(schemeType);
            dastGraphQlScanSetupReqModel.setServicePath(servicePath);
            dastGraphQlScanSetupReqModel.setHost(host);
            dastGraphQlScanSetupReqModel.setSourceUrn(sourceUrn);

            if (!Utils.isNullOrEmpty(scanTimeBox))
                dastGraphQlScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(scanTimeBox));
            else
                dastGraphQlScanSetupReqModel.setTimeBoxInHours(null);

            PutDastScanSetupResponse response = dynamicController.putDastGraphQLScanSettings(Integer.parseInt(userSelectedRelease),
                    dastGraphQlScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {

                Utils.logger(_printStream, "Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                String errMsg  = response.errors != null ? response.errors.stream().map(e -> e.errorCode + ":" + e.message)
                        .collect(Collectors.joining(",")) : "";
                throw new Exception(String.format("FOD API Error %s",errMsg));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }

    public void saveReleaseSettingsForGrpcScan(String userSelectedRelease, String assessmentTypeID,
                                               String entitlementId, String entitlementFreq,
                                               String timeZone,
                                               String scanEnvironment,
                                               String networkAuthUserName, String networkAuthPassword,
                                               String networkAuthType,
                                               String grpcFileId, String schemeType, String host,
                                               String servicePath, boolean requestFalsePositiveRemoval,
                                               String scanTimeBox)
            throws Exception {

        DastScanController dynamicController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastAutomatedGrpcReqModel dastgrpcScanSetupReqModel;
            dastgrpcScanSetupReqModel = new PutDastAutomatedGrpcReqModel();
            dastgrpcScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastgrpcScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastgrpcScanSetupReqModel.setTimeZone(timeZone);
            dastgrpcScanSetupReqModel.setSchemeType(schemeType);
            dastgrpcScanSetupReqModel.setHost(host);
            dastgrpcScanSetupReqModel.setServicePath(servicePath);
            dastgrpcScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastgrpcScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);
            dastgrpcScanSetupReqModel.setRequestFalsePositiveRemoval(requestFalsePositiveRemoval);

            if (networkAuthType != null &&
                    networkAuthPassword != null && networkAuthUserName != null &&
                    !networkAuthPassword.isEmpty() && !networkAuthType.isEmpty() && !networkAuthUserName.isEmpty()) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastgrpcScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);
                networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                dastgrpcScanSetupReqModel.setRequiresNetworkAuthentication(true);
                dastgrpcScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            int fileId = Integer.parseInt(grpcFileId);
            if (fileId == 0) {
                throw new IllegalArgumentException(String.format("GRPC Source= %s  not set for release Id={%s}"
                        , fileId, userSelectedRelease));
            } else {
                dastgrpcScanSetupReqModel.setFileId(Integer.parseInt(grpcFileId));
            }

            if (!Utils.isNullOrEmpty(scanTimeBox))
                dastgrpcScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(scanTimeBox));
            else
                dastgrpcScanSetupReqModel.setTimeBoxInHours(null);


            PutDastScanSetupResponse response = dynamicController.putDastGrpcScanSettings(Integer.parseInt(userSelectedRelease),
                    dastgrpcScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                Utils.logger(_printStream, "Successfully saved settings for release id = " + userSelectedRelease);

            } else {

                String errMsg  = response.errors != null ? response.errors.stream().map(e -> e.errorCode + ":" + e.message)
                        .collect(Collectors.joining(",")) : "";
                throw new Exception(String.format("FOD API Error %s",errMsg));
            }

        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }

    public void saveReleaseSettingsForPostmanScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq,
                                                  String timeZone,
                                                  String scanEnvironment,
                                                  String networkAuthUserName, String networkAuthPassword,
                                                  String networkAuthType, String postmanIdCollection,
                                                  boolean requestFalsePositiveRemoval, String scanTimeBox)
            throws Exception {

        DastScanController dynamicController = new DastScanController(getFodApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastAutomatedPostmanReqModel dastPostmanScanSetupReqModel;
            dastPostmanScanSetupReqModel = new PutDastAutomatedPostmanReqModel();
            dastPostmanScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastPostmanScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastPostmanScanSetupReqModel.setTimeZone(timeZone);
            dastPostmanScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));
            dastPostmanScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);
            dastPostmanScanSetupReqModel.setRequestFalsePositiveRemoval(requestFalsePositiveRemoval);


            if (networkAuthType != null &&
                    networkAuthPassword != null && networkAuthUserName != null &&
                    !networkAuthPassword.isEmpty() && !networkAuthType.isEmpty() && !networkAuthUserName.isEmpty()) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastPostmanScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);
                networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                dastPostmanScanSetupReqModel.setRequiresNetworkAuthentication(true);
                dastPostmanScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }
            if (postmanIdCollection == null) {
                throw new IllegalArgumentException(String.format("Postman Scan - one of the id is  not set for release Id={%s}"
                        , userSelectedRelease));
            } else {
                dastPostmanScanSetupReqModel.setCollectionFileIds(convertStringToIntArr(postmanIdCollection));
            }
            if (!Utils.isNullOrEmpty(scanTimeBox))
                dastPostmanScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(scanTimeBox));
            else
                dastPostmanScanSetupReqModel.setTimeBoxInHours(null);

            PutDastScanSetupResponse response = dynamicController.putDastPostmanScanSettings(Integer.parseInt(userSelectedRelease),
                    dastPostmanScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                String errMsg  = response.errors != null ? response.errors.stream().map(e -> e.errorCode + ":" + e.message)
                        .collect(Collectors.joining(",")) : "";
                throw new Exception(String.format("FOD API Error %s",errMsg));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
        }
    }


    public int[] convertStringToIntArr(String fileIds) {

        String[] postmanIds = fileIds.split(",");

        if (postmanIds.length > 0) {
            int[] postmanIdArr = new int[postmanIds.length];
            for (int i = 0; i < postmanIds.length; i++) {
                postmanIdArr[i] = Integer.parseInt(postmanIds[i]);
            }
            return postmanIdArr;
        }
        return null;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build,
                        TaskListener listener, String correlationId, FodApiConnection apiConnection) throws IOException {
        final PrintStream logger = listener.getLogger();

            taskListener.set(listener);

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
            Integer releaseId = Integer.parseInt(model.get_releaseId());

            if (releaseId <= 0) {
                build.setResult(Result.FAILURE);
                Utils.logger(logger, "Invalid release ID.");
                return;
            }

            if (apiConnection != null) {

                DastScanController dynamicController = new DastScanController(apiConnection, logger, Utils.createCorrelationId());
                PostDastStartScanResponse response = dynamicController.startDastScan(releaseId);
                if (response.errors == null && response.scanId != null && response.scanId > 0) {
                    build.setResult(Result.SUCCESS);
                    Utils.logger(logger, String.format("Dynamic scan successfully triggered for scan Id %d ", response.scanId));
                    this.scanId = response.scanId;
                } else {
                    String errMsg = response.errors != null ? response.errors.stream().map(e -> e.message)
                            .collect(Collectors.joining(",")) : "";

                    Utils.logger(logger, String.format("Scan Failed for release Id %d,error:%s", releaseId, errMsg));
                    build.setResult(Result.FAILURE);
                }
            } else {
                Utils.logger(logger, "Failed to create Fod API connection.");
                build.setResult(Result.FAILURE);
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
            if (!selectedReleaseType.getValue().equalsIgnoreCase(BSI_TOKEN))
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

    @SuppressWarnings("unused")
    public static org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> customFillUserApplicationById(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> result = applicationsController.getApplicationById(applicationId);

        return result;
    }

    @SuppressWarnings("unused")
    public static GenericListResponse<ReleaseApiResponse> customFillUserSelectedReleaseList(int applicationId, int microserviceId, String searchTerm, Integer offset, Integer limit, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getReleaseListByApplication(applicationId, microserviceId, searchTerm, offset, limit);
    }

    @SuppressWarnings("unused")
    public static org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> customFillUserReleaseById(int releaseId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> result = applicationsController.getReleaseById(releaseId);

        return result;
    }

    @SuppressWarnings("unused")
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



