package org.jenkinsci.plugins.fodupload.steps;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileUploadException;
import org.jenkinsci.plugins.fodupload.ApiConnectionFactory;
import org.jenkinsci.plugins.fodupload.DastScanSharedBuildStep;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.SharedUploadBuildStep;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.controllers.*;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.response.AssessmentTypeEntitlementsForAutoProv;
import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.interceptor.JsonOutputFilter;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.APILookupItemTypes;

@SuppressFBWarnings("unused")
public class FortifyDastPipelineAssessment extends FortifyStep {
    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    private final String correlationId = UUID.randomUUID().toString();
    private String releaseId;
    @Deprecated
    private Boolean overrideGlobalConfig;
    private String username;

    public String getLoginMacroFileId() {
        return loginMacroFileId;
    }

    @DataBoundSetter
    public void setLoginMacroFileId(String loginMacroFileId) {
        this.loginMacroFileId = loginMacroFileId;
    }

    private String loginMacroFileId;
    private String personalAccessToken;
    private String tenantId;
    private String webSiteUrl;
    private boolean purchaseEntitlements;
    private String entitlementId;
    private String entitlementFrequency;
    private String remediationScanPreferenceType;
    private String inProgressScanActionType;
    private String inProgressBuildResultType;
    private String assessmentType;
    private String auditPreference;
    private String applicationName;
    private String applicationType;

    private String releaseName;
    private Integer owner;
    private String attributes;
    private String businessCriticality;
    private String sdlcStatus;
    private boolean enableRedundantPageDetection;


    String networkAuthType;

    public java.lang.String getSelectedApiType() {
        return selectedApiType;
    }

    @DataBoundSetter
    public void setSelectedApiType(java.lang.String selectedApiType) {
        this.selectedApiType = selectedApiType;
    }

    String selectedApiType;

    public java.lang.String getOpenApiRadioSource() {
        return openApiRadioSource;
    }

    @DataBoundSetter
    public void setOpenApiRadioSource(java.lang.String openApiRadioSource) {
        this.openApiRadioSource = openApiRadioSource;
    }

    String openApiRadioSource;

    public java.lang.String getOpenApiFileId() {
        return openApiFileId;
    }

    @DataBoundSetter

    public void setOpenApiFileId(java.lang.String openApiFileId) {
        this.openApiFileId = openApiFileId;
    }

    String openApiFileId;

    public java.lang.String getOpenApiUrl() {
        return openApiUrl;
    }

    @DataBoundSetter
    public void setOpenApiUrl(java.lang.String openApiUrl) {
        this.openApiUrl = openApiUrl;
    }

    String openApiUrl;

    public java.lang.String getOpenApiKey() {
        return openApiKey;
    }

    @DataBoundSetter

    public void setOpenApiKey(java.lang.String openApiKey) {
        this.openApiKey = openApiKey;
    }

    String openApiKey;

    public String getOpenApiFilePath() {
        return openApiFilePath;
    }

    @DataBoundSetter
    public void setOpenApiFilePath(String openApiFilePath) {
        this.openApiFilePath = openApiFilePath;
    }

    String openApiFilePath;

    public java.lang.String getPostmanFileId() {
        return postmanFileId;
    }

    @DataBoundSetter
    public void setPostmanFileId(java.lang.String postmanFileId) {
        this.postmanFileId = postmanFileId;
    }

    String postmanFileId;

    public String getPostmanFilePath() {
        return postmanFilePath;
    }

    @DataBoundSetter
    public void setPostmanFilePath(String postmanFilePath) {
        this.postmanFilePath = postmanFilePath;
    }

    String postmanFilePath;
    public String getGraphQlRadioSource() {
        return graphQlRadioSource;
    }

    @DataBoundSetter
    public void setGraphQlRadioSource(String graphQlRadioSource) {
        this.graphQlRadioSource = graphQlRadioSource;
    }

    String graphQlRadioSource;

    public String getGraphQLFileId() {
        return graphQLFileId;
    }

    @DataBoundSetter
    public void setGraphQLFileId(String graphQLFileId) {
        this.graphQLFileId = graphQLFileId;
    }

    String graphQLFileId;

    public String getGraphQLFilePath() {
        return graphQLFilePath;
    }

    @DataBoundSetter
    public void setGraphQLFilePath(String graphQLFilePath) {
        this.graphQLFilePath = graphQLFilePath;
    }

    String graphQLFilePath;

    public String getGraphQLUrl() {
        return graphQLUrl;
    }

    @DataBoundSetter
    public void setGraphQLUrl(String graphQLUrl) {
        this.graphQLUrl = graphQLUrl;
    }

    String graphQLUrl;

    public String getGraphQLSchemeType() {
        return graphQLSchemeType;
    }

    @DataBoundSetter
    public void setGraphQLSchemeType(String graphQLSchemeType) {
        this.graphQLSchemeType = graphQLSchemeType;
    }

    String graphQLSchemeType;

    public String getGraphQlApiHost() {
        return graphQlApiHost;
    }

    @DataBoundSetter
    public void setGraphQlApiHost(String graphQlApiHost) {
        this.graphQlApiHost = graphQlApiHost;
    }

    String graphQlApiHost;

    public String getGraphQlApiServicePath() {
        return graphQlApiServicePath;
    }


    @DataBoundSetter
    public void setGraphQlApiServicePath(String graphQlApiServicePath) {
        this.graphQlApiServicePath = graphQlApiServicePath;
    }

    String graphQlApiServicePath;

    public String getGrpcFileId() {
        return grpcFileId;
    }

    @DataBoundSetter
    public void setGrpcFileId(String grpcFileId) {
        this.grpcFileId = grpcFileId;
    }

    String grpcFileId;

    public String getGrpcSchemeType() {
        return grpcSchemeType;
    }

    @DataBoundSetter
    public void setGrpcSchemeType(String grpcSchemeType) {
        this.grpcSchemeType = grpcSchemeType;
    }

    public String getGrpcFilePath() {
        return grpcFilePath;
    }

    @DataBoundSetter
    public void setGrpcFilePath(String grpcFilePath) {
        this.grpcFilePath = grpcFilePath;
    }

    String grpcFilePath;
    String grpcSchemeType;

    public String getGrpcApiHost() {
        return grpcApiHost;
    }

    @DataBoundSetter
    public void setGrpcApiHost(String grpcApiHost) {
        this.grpcApiHost = grpcApiHost;
    }

    String grpcApiHost;

    public String getGrpcApiServicePath() {
        return grpcApiServicePath;
    }

    @DataBoundSetter
    public void setGrpcApiServicePath(String grpcApiServicePath) {
        this.grpcApiServicePath = grpcApiServicePath;
    }

    String grpcApiServicePath;

    public String getScanTimeBox() {
        return scanTimeBox;
    }

    @DataBoundSetter
    public void setScanTimeBox(String scanTimeBox) {
        this.scanTimeBox = scanTimeBox;
    }

    private String scanTimeBox;
    private boolean requireLoginMacro;
    private String loginMacroFilePath;
    private String workflowMacroHosts;

    public String getWorkflowMacroHosts() {
        return this.workflowMacroHosts;
    }

    @DataBoundSetter
    public void setWorkflowMacroHosts(String workflowMacroHosts) {
        this.workflowMacroHosts = workflowMacroHosts;
    }

    public String getWorkflowMacroFilePath() {
        return workflowMacroFilePath;
    }

    @DataBoundSetter
    public void setWorkflowMacroFilePath(String workflowMacroFilePath) {
        this.workflowMacroFilePath = workflowMacroFilePath;
    }

    private String workflowMacroFilePath;

    public String getLoginMacroFilePath() {
        return loginMacroFilePath;
    }

    @DataBoundSetter
    public void setLoginMacroFilePath(String loginMacroFilePath) {
        this.loginMacroFilePath = loginMacroFilePath;
    }

    public boolean isRequireLoginMacro() {
        return requireLoginMacro;
    }

    public void setRequireLoginMacro(boolean requireLoginMacro) {
        this.requireLoginMacro = requireLoginMacro;
    }

    public String getNetworkAuthUserName() {
        return networkAuthUserName;
    }

    @DataBoundSetter
    public void setNetworkAuthUserName(String networkAuthUserName) {
        this.networkAuthUserName = networkAuthUserName;
    }

    private String networkAuthUserName;

    public String getNetworkAuthType() {
        return networkAuthType;
    }

    @DataBoundSetter
    public void setNetworkAuthType(String networkAuthType) {
        this.networkAuthType = networkAuthType;
    }

    public boolean isTimeBoxChecked() {
        return timeBoxChecked;
    }

    @DataBoundSetter
    public void setTimeBoxChecked(boolean timeBoxChecked) {
        this.timeBoxChecked = timeBoxChecked;
    }

    private boolean timeBoxChecked;

    public String getAssessmentTypeId() {
        return assessmentTypeId;
    }

    @DataBoundSetter
    public void setAssessmentTypeId(String assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    private String assessmentTypeId;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    private String applicationId;

    public String getNetworkAuthPassword() {
        return networkAuthPassword;
    }

    @DataBoundSetter
    public void setNetworkAuthPassword(String networkAuthPassword) {
        this.networkAuthPassword = networkAuthPassword;
    }

    private String networkAuthPassword;

    private DastScanSharedBuildStep dastScanSharedBuildStep;

    @DataBoundConstructor
    public FortifyDastPipelineAssessment() {
        super();
    }

    public String getScanType() {
        return scanType;
    }

    @DataBoundSetter
    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public boolean getScanScope() {
        return scanScope;
    }

    @DataBoundSetter
    public void setScanScope(boolean scanScope) {
        this.scanScope = scanScope;
    }

    private boolean scanScope;
    private String scanType;

    private String workflowMacroId;

    public String getWorkflowMacroId() {
        return workflowMacroId;
    }

    @DataBoundSetter
    public void setWorkflowMacroId(String workflowMacroId) {
        this.workflowMacroId = workflowMacroId;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    @DataBoundSetter
    public void setWebSiteUrl(String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }


    public String getEntitlementFrequency() {
        return entitlementFrequency;
    }

    @DataBoundSetter
    public void setEntitlementFrequency(String entitlementFrequency) {
        this.entitlementFrequency = entitlementFrequency;
    }

    /*
    PostConstruct method invokes finally after all the get and set properties invoked.
    So we don't need to save as part of build perform function.
    But whenever the scan settings got update through portal, Then there would be discrepancy when the Scripts not updated through the
    Script generator.
     */
    @PostConstruct
    public final void SaveScanSettings() throws Exception {

        try {
            DastScanSharedBuildStep dastScanSharedBuildStep = null;

            if (Objects.equals(scanType, FodEnums.DastScanType.Standard.toString()) || Objects.equals(scanType, FodEnums.DastScanType.Workflow.toString())) {

                int loginFileId = 0;
                if (!loginMacroFileId.isEmpty()) {
                    loginFileId = Integer.parseInt(loginMacroFileId);
                }
                dastScanSharedBuildStep = new DastScanSharedBuildStep(
                        overrideGlobalConfig,
                        username,
                        tenantId,
                        personalAccessToken,
                        releaseId,
                        webSiteUrl,
                        envFacing,
                        scanTimeBox,
                        null,
                        scanPolicy,
                        scanScope,
                        scanType,
                        selectedDynamicTimeZone,
                        enableRedundantPageDetection,
                        webSiteUrl,
                        loginFileId,
                        workflowMacroId,
                        workflowMacroHosts,
                        networkAuthUserName,
                        networkAuthPassword,
                        applicationId,
                        assessmentTypeId,
                        entitlementId,
                        entitlementFrequency,
                        networkAuthType,
                        timeBoxChecked
                );
            } else if (Objects.equals(scanType, FodEnums.DastScanType.API.toString()) ) {
                dastScanSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig, username, personalAccessToken, tenantId,
                        releaseId, envFacing, scanTimeBox, scanPolicy, scanScope, scanType,
                        selectedDynamicTimeZone,
                        networkAuthUserName,
                        networkAuthPassword, applicationId,
                        assessmentTypeId, entitlementId,
                        entitlementFrequency, entitlementId, timeBoxChecked,
                        selectedApiType, openApiRadioSource, openApiFileId, openApiUrl, openApiKey,
                        postmanFileId,
                        graphQlRadioSource, graphQLFileId, graphQLUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                        grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath, openApiFilePath, postmanFilePath, graphQLFilePath, grpcFilePath);
            }

            if (dastScanSharedBuildStep == null) {
                throw new RuntimeException("DastScanSharedBuildStep Object not set");
            }

            List<String> errors = dastScanSharedBuildStep.ValidateModel();

            if (!errors.isEmpty()) {
                throw new IllegalArgumentException("Missing or invalid fields: " + String.join(", ", errors));
            }

            switch (this.scanType) {
                case "Standard":
                    saveWebSiteScanSettings(dastScanSharedBuildStep);
                    break;
                case "Workflow-driven":
                    saveWorkflowSiteScanSettings(dastScanSharedBuildStep);
                    break;

                case "API":
                    saveApiScanSettings(dastScanSharedBuildStep);
                    break;
            }


        } catch (Exception ex) {
            throw ex;
        }
    }


    public String getSelectedDynamicTimeZone() {
        return selectedDynamicTimeZone;
    }

    @DataBoundSetter
    public void setSelectedDynamicTimeZone(String selectedDynamicTimeZone) {
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
    }

    private String selectedDynamicTimeZone;


    public String getEnvFacing() {
        return envFacing;
    }

    @DataBoundSetter
    public void setEnvFacing(String envFacing) {
        this.envFacing = envFacing;
    }

    private String envFacing;

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    @DataBoundSetter
    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }

    public String getScanPolicy() {
        return scanPolicy;
    }

    @DataBoundSetter
    public void setScanPolicy(String scanPolicy) {
        this.scanPolicy = scanPolicy;
    }

    private String scanPolicy;


    public String getReleaseId() {
        return releaseId;
    }

    @DataBoundSetter
    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId.trim();
    }

    @Deprecated
    public Boolean getOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    @Deprecated
    @DataBoundSetter
    public void setOverrideGlobalConfig(Boolean overrideGlobalConfig) {
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

    public String getEntitlementId() {
        return entitlementId;
    }

    @DataBoundSetter
    public void setEntitlementId(String entitlementId) {
        this.entitlementId = entitlementId;
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

    @SuppressWarnings("unused")
    public String getApplicationName() {
        return applicationName;
    }

    @DataBoundSetter
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @SuppressWarnings("unused")
    public String getApplicationType() {
        return applicationType;
    }

    @DataBoundSetter
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    @SuppressWarnings("unused")
    public String getReleaseName() {
        return releaseName;
    }

    @DataBoundSetter
    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    @SuppressWarnings("unused")
    public Integer getOwner() {
        return owner;
    }

    @DataBoundSetter
    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unused")
    public String getAttributes() {
        return attributes;
    }

    @DataBoundSetter
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    @SuppressWarnings("unused")
    public String getBusinessCriticality() {
        return businessCriticality;
    }

    @DataBoundSetter
    public void setBusinessCriticality(String businessCriticality) {
        this.businessCriticality = businessCriticality;
    }

    @SuppressWarnings("unused")
    public String getSdlcStatus() {
        return sdlcStatus;
    }

    @DataBoundSetter
    public void setSdlcStatus(String sdlcStatus) {
        this.sdlcStatus = sdlcStatus;
    }

    @SuppressWarnings("unused")
    public String getAssessmentType() {
        return assessmentType;
    }

    @DataBoundSetter
    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    @SuppressWarnings("unused")
    public String getAuditPreference() {
        return auditPreference;
    }

    @DataBoundSetter
    public void setAuditPreference(String auditPreference) {
        this.auditPreference = auditPreference;
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();
        DastScanSharedBuildStep dastScanSharedBuildStep = null;
        log.println("Fortify on Demand Dynamic Scan PreBuild Running...");
        if(Objects.equals(scanType, FodEnums.DastScanType.Standard) || Objects.equals(scanType, FodEnums.DastScanType.Workflow)) {
            dastScanSharedBuildStep = new DastScanSharedBuildStep(
                    overrideGlobalConfig,
                    username,
                    tenantId,
                    personalAccessToken,
                    releaseId,
                    webSiteUrl,
                    envFacing,
                    scanTimeBox,
                    null,
                    scanPolicy,
                    scanScope,
                    scanType,
                    selectedDynamicTimeZone,
                    enableRedundantPageDetection,
                    webSiteUrl,
                    Integer.parseInt(loginMacroFileId),
                    workflowMacroId,
                    workflowMacroHosts,
                    networkAuthUserName,
                    networkAuthPassword,
                    applicationId,
                    assessmentTypeId,
                    entitlementId,
                    entitlementFrequency,
                    networkAuthType,
                    timeBoxChecked
            );
        }
        else if(Objects.equals(FodEnums.DastScanType.API, scanType)){
            dastScanSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig, username, personalAccessToken, tenantId,
                    releaseId, envFacing, scanTimeBox, scanPolicy, scanScope, scanType,
                    selectedDynamicTimeZone,
                    networkAuthUserName,
                    networkAuthPassword, applicationId,
                    assessmentTypeId, entitlementId,
                    entitlementFrequency, entitlementId, timeBoxChecked,
                    selectedApiType, openApiRadioSource, openApiFileId, openApiUrl, openApiKey,
                    postmanFileId,
                    graphQlRadioSource, graphQLFileId, graphQLUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                    grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath, openApiFilePath, postmanFilePath, graphQLFilePath, grpcFilePath);
        }

        // When does this happen? If this only happens in syntax gen, then just use ServerClient
        boolean overrideGlobalAuthConfig = !Utils.isNullOrEmpty(username);
        List<String> errors = null;
        try {
            errors = dastScanSharedBuildStep.ValidateAuthModel(overrideGlobalAuthConfig, username, tenantId, personalAccessToken);
            if (errors.isEmpty()) {
                AuthenticationModel authModel = new AuthenticationModel(overrideGlobalAuthConfig,
                        username,
                        personalAccessToken,
                        tenantId);
            }

            errors = dastScanSharedBuildStep.ValidateModel();

        } catch (FormValidation e) {
            throw new RuntimeException(e);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments: Missing or invalid fields for auto provisioning: " + String.join(", ", errors));
        }
        try {
            switch (scanType) {
                case ("Standard"): {
                    saveWebSiteScanSettings(dastScanSharedBuildStep);
                    log.printf("Fortify On Demand Dynamic Scan Settings Saved Successfully for release Id %s", releaseId);
                    break;
                }
                case "Workflow-driven": {
                    saveWorkflowSiteScanSettings(dastScanSharedBuildStep);
                    break;
                }

                case "API": {

                    saveApiScanSettings(dastScanSharedBuildStep);
                    break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }

    private List<String> ValidateAuthModel(boolean overrideGlobalAuth) throws FormValidation {
        List<String> errors = new ArrayList<>();

        // Any have value and any don't have value
        if (overrideGlobalAuth && (Utils.isNullOrEmpty(username) || Utils.isNullOrEmpty(tenantId) || Utils.isNullOrEmpty(personalAccessToken))) {
            errors.add("Personal access token override requires all 3 be provided: username, personalAccessToken, tenantId");
        }

        return errors;
    }

    private void saveWebSiteScanSettings(DastScanSharedBuildStep dastScanSharedBuildStep) throws Exception {

        if (loginMacroFilePath != null && !loginMacroFilePath.isEmpty() && loginMacroFilePath.length() > 1) {
            Path path = Paths.get(loginMacroFilePath);
            //add file validation here.
            PatchDastFileUploadResponse patchUploadResponse = dastScanSharedBuildStep.PatchSetupManifestFile(Files.readAllBytes(path), "LoginMacro");

            if (patchUploadResponse == null || !patchUploadResponse.isSuccess) {
                throw new FileUploadException(String.format("Failed to upload login macro file %s for release Id:%s",
                        loginMacroFilePath, releaseId));
            } else {
                this.loginMacroFileId = String.valueOf(patchUploadResponse.fileId);
            }
        }

        dastScanSharedBuildStep.saveReleaseSettingsForWebSiteScan(releaseId, assessmentTypeId,
                entitlementId
                , entitlementFrequency, String.valueOf(loginMacroFileId),
                selectedDynamicTimeZone,
                scanPolicy,
                webSiteUrl
                , scanScope, enableRedundantPageDetection, envFacing
                , networkAuthType != null && !networkAuthType.isEmpty(), requireLoginMacro,
                networkAuthUserName, networkAuthPassword
                , networkAuthType, scanTimeBox);
    }

    private void saveWorkflowSiteScanSettings(DastScanSharedBuildStep dastScanSharedBuildStep) throws Exception {

        if (!this.workflowMacroFilePath.isEmpty() && this.workflowMacroFilePath.length() > 2) {

            int workflowMacroFileId = 0;

            Path path = Paths.get(this.workflowMacroFilePath);

            PatchDastFileUploadResponse patchDastFileUploadResponse = dastScanSharedBuildStep.PatchSetupManifestFile(Files.readAllBytes(path), "WorkflowDrivenMacro");
            if (patchDastFileUploadResponse == null || !patchDastFileUploadResponse.isSuccess) {
                throw new FileUploadException(String.format("Failed to upload workflow macro file %s for release Id:%s",
                        this.workflowMacroFilePath, releaseId));
            } else {
                this.workflowMacroId = String.valueOf(patchDastFileUploadResponse.fileId);
            }

            if (this.workflowMacroHosts == null || this.workflowMacroHosts.isEmpty()) {
                this.workflowMacroHosts = String.join(",", patchDastFileUploadResponse.hosts);
            }
        }
        dastScanSharedBuildStep.saveReleaseSettingsForWorkflowDrivenScan(releaseId, assessmentTypeId, entitlementId, entitlementFrequency, workflowMacroId, this.workflowMacroHosts,
                selectedDynamicTimeZone, scanPolicy, enableRedundantPageDetection, envFacing, getNetworkAuthType().isEmpty(), networkAuthUserName, networkAuthPassword, networkAuthType);
    }

    private void saveApiScanSettings(DastScanSharedBuildStep dastScanSharedBuildStep) throws Exception {
        try {
            if (FodEnums.DastApiType.OpenApi.toString().equalsIgnoreCase(selectedApiType)) {
                Path openApiPath = Paths.get(openApiFilePath);
                PatchDastFileUploadResponse response = dastScanSharedBuildStep.PatchSetupManifestFile(Files.readAllBytes(openApiPath), "OpenAPIDefinition");
                    openApiFileId = String.valueOf(response.fileId);
                String sourceUrn = openApiRadioSource.equals("Url") ? openApiUrl : openApiFileId;
                dastScanSharedBuildStep.saveReleaseSettingsForOpenApiScan(releaseId, assessmentTypeId, entitlementId,
                        entitlementFrequency, selectedDynamicTimeZone,
                        enableRedundantPageDetection, envFacing, !networkAuthType.isEmpty(),
                        networkAuthUserName, networkAuthPassword, networkAuthType,
                        openApiRadioSource, sourceUrn, openApiKey);

            }
            else if (FodEnums.DastApiType.GraphQL.toString().equalsIgnoreCase(selectedApiType)) {
                Path graphQlPath = Paths.get(graphQLFilePath);
                PatchDastFileUploadResponse response = dastScanSharedBuildStep.PatchSetupManifestFile(Files.readAllBytes(graphQlPath), "GraphQLDefinition");
                graphQLFileId = String.valueOf(response.fileId);
                String sourceUrn = graphQlRadioSource.equals("Url") ? graphQLUrl : graphQLFileId;
                dastScanSharedBuildStep.saveReleaseSettingsForGraphQlScan(releaseId, assessmentTypeId, entitlementId,
                        entitlementFrequency, selectedDynamicTimeZone,
                        enableRedundantPageDetection, envFacing, !networkAuthType.isEmpty(),
                        networkAuthUserName, networkAuthPassword, networkAuthType,
                        sourceUrn, graphQlRadioSource, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath);

            }
            else if (FodEnums.DastApiType.Grpc.toString().equalsIgnoreCase(selectedApiType)) {
                Path grpcPath = Paths.get(grpcFilePath);
                PatchDastFileUploadResponse response = dastScanSharedBuildStep.PatchSetupManifestFile(Files.readAllBytes(grpcPath), "GRPCDefinition");
                grpcFileId = String.valueOf(response.fileId);
                dastScanSharedBuildStep.saveReleaseSettingsForGrpcScan(releaseId, assessmentTypeId, entitlementId,
                        entitlementFrequency, selectedDynamicTimeZone,
                        enableRedundantPageDetection, envFacing, !networkAuthType.isEmpty(),
                        networkAuthUserName, networkAuthPassword, networkAuthType,
                        grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath);

            }
            else if (FodEnums.DastApiType.Postman.toString().equalsIgnoreCase(selectedApiType)) {
                Path postmanPath = Paths.get(postmanFilePath);
                PatchDastFileUploadResponse response = dastScanSharedBuildStep.PatchSetupManifestFile(Files.readAllBytes(postmanPath), "PostmanCollection");
                postmanFileId = String.valueOf(response.fileId);
                dastScanSharedBuildStep.saveReleaseSettingsForPostmanScan(releaseId, assessmentTypeId, entitlementId,
                        entitlementFrequency, selectedDynamicTimeZone,
                        enableRedundantPageDetection, envFacing, !networkAuthType.isEmpty(),
                        networkAuthUserName, networkAuthPassword, networkAuthType,
                        postmanFileId);
            } else {
                throw new IllegalArgumentException("Not Valid Dast API Scan Type set for releaseId: " + releaseId);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, IllegalArgumentException {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Upload Running...");
        build.addAction(new CrossBuildAction());

        DastScanSharedBuildStep dastScanSharedBuildStep = null;

        if (Objects.equals(scanType, FodEnums.DastScanType.Standard.toString()) || Objects.equals(scanType, FodEnums.DastScanType.Workflow.toString())) {

            int loginFileId = 0;
            if (!loginMacroFileId.isEmpty()) {
                loginFileId = Integer.parseInt(loginMacroFileId);
            }

            dastScanSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig,
                    username,
                    tenantId,
                    personalAccessToken,
                    releaseId,
                    webSiteUrl,
                    envFacing,
                    scanTimeBox,
                    null,
                    scanPolicy,
                    scanScope,
                    scanType,
                    selectedDynamicTimeZone,
                    enableRedundantPageDetection,
                    loginMacroFilePath,
                    loginFileId,
                    workflowMacroId,
                    workflowMacroHosts,
                    networkAuthUserName,
                    networkAuthPassword,
                    applicationId,
                    assessmentTypeId,
                    entitlementId,
                    entitlementFrequency,
                    networkAuthType,
                    timeBoxChecked);
        } else if (Objects.equals(scanType, FodEnums.DastScanType.API.toString())) {

            dastScanSharedBuildStep = new DastScanSharedBuildStep(overrideGlobalConfig, username, personalAccessToken, tenantId,
                    releaseId, envFacing, scanTimeBox, scanPolicy, scanScope, scanType,
                    selectedDynamicTimeZone,
                    networkAuthUserName,
                    networkAuthPassword, applicationId,
                    assessmentTypeId, entitlementId,
                    entitlementFrequency, entitlementId, timeBoxChecked,
                    selectedApiType, openApiRadioSource, openApiFileId, openApiUrl, openApiKey,
                    postmanFileId,
                    graphQlRadioSource, graphQLFileId, graphQLUrl, graphQLSchemeType, graphQlApiHost, graphQlApiServicePath,
                    grpcFileId, grpcSchemeType, grpcApiHost, grpcApiServicePath, openApiFilePath, postmanFilePath, graphQLFilePath, grpcFilePath);

        }
        boolean overrideGlobalAuthConfig = !Utils.isNullOrEmpty(username);
        List<String> errors = null;

        try {
            errors = ValidateAuthModel(overrideGlobalAuthConfig);

            if (errors.isEmpty()) {
                AuthenticationModel authModel = new AuthenticationModel(overrideGlobalAuthConfig,
                        username,
                        personalAccessToken,
                        tenantId);

            }
            if (dastScanSharedBuildStep == null) {
                throw new RuntimeException("Failed to initialize DAST Job model");
            }
            errors = dastScanSharedBuildStep.ValidateModel();
        } catch (FormValidation e) {
            throw new RuntimeException(e);
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments:\n\t" + String.join("\n\t", errors));
        }

        try {
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex.toString());
        }
        dastScanSharedBuildStep.perform(build, workspace, launcher, listener, correlationId);
        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
        crossBuildAction.setPreviousStepBuildResult(build.getResult());

        if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
            crossBuildAction.setScanId(dastScanSharedBuildStep.getScanId());
            crossBuildAction.setCorrelationId(correlationId);
        }
        try {
            build.save();
        } catch (IOException ex) {
            log.println("Fortify On Demand Dynamic Scan Error saving settings. Error message: " + ex.getMessage());
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getDisplayName() {
            return "Run Fortify on Demand Upload";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "fodDynamicAssessment";
        }

        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter("usernameStaplerOnly") final String username,
                                                                  @QueryParameter("personalAccessTokenSelect") final String personalAccessToken,
                                                                  @QueryParameter("tenantIdStaplerOnly") final String tenantId,
                                                                  @AncestorInPath Job job) throws FormValidation {
            job.checkPermission(Item.CONFIGURE);
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);

        }

        public static ListBoxModel doFillEnvFacingItems() {
            return DastScanSharedBuildStep.doFillDastEnvItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            return DastScanSharedBuildStep.doFillEntitlementPreferenceItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillUsernameItems(@AncestorInPath Job job) {
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPersonalAccessTokenSelectItems(@AncestorInPath Job job) {
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTenantIdItems(@AncestorInPath Job job) {
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressScanActionTypeItems() {
            return DastScanSharedBuildStep.doFillInProgressScanActionTypeItems();
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanPolicyItems() {
            return DastScanSharedBuildStep.doFillScanPolicyItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressBuildResultTypeItems() {
            return DastScanSharedBuildStep.doFillInProgressBuildResultTypeItems();
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

        private static <T extends Enum<T>> ListBoxModel doFillFromEnum(Class<T> enumClass) {
            ListBoxModel items = new ListBoxModel();
            for (T selected : EnumSet.allOf(enumClass)) {
                items.add(new ListBoxModel.Option(selected.toString(), selected.name()));
            }
            return items;
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlements(Integer releaseId, JSONObject authModelObject) {
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
        public String retrieveAssessmentTypeEntitlementsForAutoProv(String appName, String relName, Boolean isMicroservice, String microserviceName, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                AssessmentTypesController assessments = new AssessmentTypesController(apiConnection, null, Utils.createCorrelationId());
                AssessmentTypeEntitlementsForAutoProv result = null;
                DastScanController dastScanController = new DastScanController(apiConnection, null, Utils.createCorrelationId());
                return Utils.createResponseViewModel(assessments.getDynamicAssessmentTypeEntitlements(false));

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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveLookupItems(String type, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                LookupItemsController lookupItemsController = new LookupItemsController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(lookupItemsController.getLookupItems(APILookupItemTypes.valueOf(type)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }


    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
        private transient FortifyDastPipelineAssessment upload;

        protected Execution(FortifyDastPipelineAssessment upload, StepContext context) {
            super(context);
            this.upload = upload;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running fodDynamicAssessment step");
            upload.perform(getContext().get(Run.class), getContext().get(FilePath.class),
                    getContext().get(Launcher.class), getContext().get(TaskListener.class));

            return null;
        }
    }
}
