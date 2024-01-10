package org.jenkinsci.plugins.fodupload.models;

public class DastScanJobModel {
    private boolean overrideGlobalConfig;
    String username;
    String timeBoxScan;

    public String getAllowedHost() {
        return allowedHost;
    }

    String allowedHost;
    String personalAccessToken;
    String tenantId;
    String _releaseId;
    String selectedReleaseType;
    String webSiteUrl;

    public int getWorkflowMacroFileId() {
        return workflowMacroFileId;
    }

    public  String getWorkflowMacroFilePath()
    {
        return this.workflowMacroFilePath;
    }

    int workflowMacroFileId;
    String dastEnv;
    String scanPolicyType;
    boolean scanHost;
    boolean allowHttp;
    boolean allowFormSubmissionCrawl;
    String selectedScanType;
    String selectedDynamicTimeZone;
    boolean enableRedundantPageDetection;
    String networkAuthUserName;
    Integer loginFileMacro;
    String networkAuthPassword;
    String networkAuthType;
    String userSelectedApplication;
    String userSelectedRelease;
    String assessmentTypeId;
    String entitlementId;
    String entitlementFrequencyId;
    String entitlementFrequencyType;
    String userSelectedEntitlement;
    String selectedApi;
    String selectedOpenApiSource;
    String selectedOpenApiFileSource;
    String selectedOpenApiurl;
    String selectedApiKey;
    String selectedPostmanFile;
    String selectedGraphQlSource;
    String selectedGraphQlUpload;
    String selectedGraphQlUrl;
    String selectedGraphQLSchemeType;
    String loginMacroFilePath;
    String workflowMacroFilePath;
    String selectedGraphQlApiHost;
    String selectedGraphQlApiServicePath;
    String selectedGrpcUpload;
    String selectedGrpcSchemeType;
    String selectedGrpcApiHost;
    String selectedGrpcApiServicePath;
    boolean timeBoxChecked;
    boolean requestLoginMacroFileCreation;
    String loginMacroPrimaryUserName;
    String loginMacroPrimaryPassword;
    String loginMacroSecondaryUsername;
    String loginMacroSecondaryPassword;

    Boolean requestFalsePositiveRemoval;


    public DastScanJobModel(Boolean overrideGlobalConfig, String username, String personalAccessToken, String tenantId, String releaseId,
                            String webSiteUrl, String dastEnv, String scanTimebox,
                            boolean scanScope, String selectedScanType, String scanPolicy,
                            String selectedDynamicTimeZone,
                            boolean enableRedundantPageDetection,
                            String networkAuthUserName, String loginMacroFilePath,
                            String workflowMacroFilePath,int loginMacroId,
                            String workflowMacroId, String allowedHost,
                            String networkAuthPassword, String assessmentTypeId,
                            String entitlementId, String entitlementFrequencyType,
                            String selectedNetworkAuthType,boolean timeBoxChecked,
                            boolean requestLoginMacroFileCreation, String loginMacroPrimaryUserName, String loginMacroPrimaryPassword,
                            String loginMacroSecondaryUsername, String loginMacroSecondaryPassword, boolean requestFalsePositiveRemoval) {

        this._releaseId = releaseId;
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.tenantId = tenantId;
        this.webSiteUrl = webSiteUrl;
        this.allowedHost = allowedHost;
        if (workflowMacroId != null && !workflowMacroId.isEmpty()) {
            this.workflowMacroFileId = Integer.parseInt(workflowMacroId);
        }
        this.loginMacroFilePath = loginMacroFilePath;
        this.workflowMacroFilePath = workflowMacroFilePath;
        this.loginFileMacro = loginMacroId;
        this.dastEnv = dastEnv;
        this.timeBoxScan = scanTimebox;
        this.scanPolicyType = scanPolicy;
        this.enableRedundantPageDetection = enableRedundantPageDetection;
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
        this.networkAuthPassword = networkAuthPassword;
        this.networkAuthUserName = networkAuthUserName;
        this.selectedScanType = selectedScanType;
        this.assessmentTypeId = assessmentTypeId;
        this.entitlementId = entitlementId;
        this.entitlementFrequencyType = entitlementFrequencyType;
        this.networkAuthType = selectedNetworkAuthType;
        this.timeBoxChecked = timeBoxChecked;
        this.requestLoginMacroFileCreation = requestLoginMacroFileCreation;
        this.loginMacroPrimaryUserName = loginMacroPrimaryUserName;
        this.loginMacroPrimaryPassword = loginMacroPrimaryPassword;
        this.loginMacroSecondaryUsername = loginMacroSecondaryUsername;
        this.loginMacroSecondaryPassword = loginMacroSecondaryPassword;
        this.requestFalsePositiveRemoval = requestFalsePositiveRemoval;
    }

    public boolean isOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    public String getUsername() {
        return username;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getWebSiteUrl() {
        return webSiteUrl;
    }

    public String getDastEnv() {
        return dastEnv;
    }

    public String getScanPolicyType() {
        return scanPolicyType;
    }

    public boolean isScanHost() {
        return scanHost;
    }

    public boolean isAllowHttp() {
        return allowHttp;
    }

    public boolean isAllowFormSubmissionCrawl() {
        return allowFormSubmissionCrawl;
    }

    public String getNetworkAuthType() {
        return networkAuthType;
    }

    public String getSelectedScanType() {
        return selectedScanType;
    }

    public String getSelectedDynamicTimeZone() {
        return selectedDynamicTimeZone;
    }

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    public String getNetworkAuthUserName() {
        return networkAuthUserName;
    }

    public Integer getLoginFileMacro() {
        return loginFileMacro;
    }

    public String getNetworkAuthPassword() {
        return networkAuthPassword;
    }

    public String getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public String getEntitlementId() {
        return entitlementId;
    }

    public String getEntitlementFrequencyId() {
        return entitlementFrequencyId;
    }

    public String getEntitlementFrequencyType() {
        return entitlementFrequencyType;
    }

    public String getUserSelectedEntitlement() {
        return userSelectedEntitlement;
    }

    public String getSelectedDynamicGeoLocation() {
        return selectedDynamicGeoLocation;
    }

    public boolean isWebSiteNetworkAuthEnabled() {
        return webSiteNetworkAuthEnabled;
    }

    public boolean isWebSiteLoginMacroEnabled() {
        return webSiteLoginMacroEnabled;
    }

    public boolean isTimeBoxChecked() {
        return timeBoxChecked;
    }

    public boolean isRequestLoginMacroFileCreation() {
        return requestLoginMacroFileCreation;
    }

    public String getLoginMacroPrimaryUserName() {
        return loginMacroPrimaryUserName;
    }

    public String getLoginMacroPrimaryPassword() {
        return loginMacroPrimaryPassword;
    }

    public String getLoginMacroSecondaryUsername() {
        return loginMacroSecondaryUsername;
    }

    public String getLoginMacroSecondaryPassword() {
        return loginMacroSecondaryPassword;
    }

    public boolean getRequestFalsePositiveRemoval() {
        return requestFalsePositiveRemoval;
    }
    public String getSelectedOpenApiSource() {
        return selectedOpenApiSource;
    }

    public String getSelectedApi() {
        return selectedApi;
    }

    public String getSelectedOpenApiurl() {
        return selectedOpenApiurl;
    }

    public String getSelectedApiKey() {
        return selectedApiKey;
    }

    public String getSelectedOpenApiFileSource() {
        return selectedOpenApiFileSource;
    }

    public String getSelectedPostmanFile() {
        return selectedPostmanFile;
    }

    public String getSelectedGraphQlSource() {
        return selectedGraphQlSource;
    }

    public String getSelectedGraphQlUpload() {
        return selectedGraphQlUpload;
    }

    public String getSelectedGraphQlUrl() {
        return selectedGraphQlUrl;
    }

    public String getSelectedGraphQlApiHost() {
        return selectedGraphQlApiHost;
    }

    public String getSelectedGraphQLSchemeType() {
        return selectedGraphQLSchemeType;
    }

    public String getSelectedGraphQlApiServicePath() {
        return selectedGraphQlApiServicePath;
    }

    public String getSelectedGrpcUpload() {
        return selectedGrpcUpload;
    }

    public String getSelectedGrpcApiHost() {
        return selectedGrpcApiHost;
    }

    public String getSelectedGrpcSchemeType() {
        return selectedGrpcSchemeType;
    }

    public String getSelectedGrpcApiServicePath() {
        return selectedGrpcApiServicePath;
    }

    String openApiFilePath;

    public String getOpenApiFilePath() {
        return openApiFilePath;
    }

    public void setOpenApiFilePath(String openApiFilePath) {
        this.openApiFilePath = openApiFilePath;
    }

    public String getPostmanFilePath() {
        return postmanFilePath;
    }

    public void setPostmanFilePath(String postmanFilePath) {
        this.postmanFilePath = postmanFilePath;
    }

    public String getGrpcFilePath() {
        return grpcFilePath;
    }

    public void setGrpcFilePath(String grpcFilePath) {
        this.grpcFilePath = grpcFilePath;
    }

    public String getGraphQlFilePath() {
        return graphQlFilePath;
    }

    public void setGraphQlFilePath(String graphQlFilePath) {
        this.graphQlFilePath = graphQlFilePath;
    }

    String postmanFilePath;
    String grpcFilePath;
    String graphQlFilePath;
    String selectedDynamicGeoLocation;
    boolean webSiteNetworkAuthEnabled;
    boolean webSiteLoginMacroEnabled;


    public DastScanJobModel(boolean overrideGlobalConfig, String username,
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
                            String selectedApiType,
                            String openApiSource, String openApiFileSource, String openApiurl, String apiKey,
                            String postmanFile,
                            String graphQlSource, String graphQlUpload, String graphQlUrl, String graphQLSchemeType, String graphQlApiHost, String graphQlApiServicePath,
                            String grpcUpload, String grpcSchemeType, String grpcApiHost, String grpcApiServicePath, String openApiFilePath, String postmanFilePath, String graphQlFilePath, String grpcFilePath) {

        this._releaseId = releaseId;
        this.tenantId = tenantId;
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.userSelectedApplication = userSelectedApplication;
        this.dastEnv = dastEnv;
        this.assessmentTypeId = assessmentTypeId;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.selectedScanType = selectedScanType;
        this.scanHost = scanScope;
        this.userSelectedEntitlement = userSelectedEntitlement;
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
        this.entitlementId = entitlementId;
        this.entitlementFrequencyType = entitlementFrequencyType;
        this.scanPolicyType = scanPolicyType;
        this.networkAuthUserName = networkAuthUserName;
        this.networkAuthPassword = networkAuthPassword;
        this.selectedApi = selectedApiType;
        this.selectedOpenApiFileSource = openApiFileSource;
        this.selectedOpenApiurl = openApiurl;
        this.selectedOpenApiSource = openApiSource;
        this.selectedApiKey = apiKey;
        this.selectedPostmanFile = postmanFile;
        this.selectedGraphQlSource = graphQlSource;
        this.selectedGraphQlUrl = graphQlUrl;
        this.selectedGraphQlUpload = graphQlUpload;
        this.selectedGraphQlApiHost = graphQlApiHost;
        this.selectedGraphQLSchemeType = graphQLSchemeType;
        this.selectedGraphQlApiServicePath = graphQlApiServicePath;
        this.selectedGrpcUpload = grpcUpload;
        this.selectedGrpcApiHost = grpcApiHost;
        this.selectedGrpcSchemeType = grpcSchemeType;
        this.selectedGrpcApiServicePath = grpcApiServicePath;
        this.openApiFilePath = openApiFilePath;
        this.postmanFilePath = postmanFilePath;
        this.graphQlFilePath = graphQlFilePath;
        this.grpcFilePath = grpcFilePath;
    }

    public String get_releaseId() {
        return _releaseId;
    }
    public String set_releaseIdFromAutoProv(String releaseId)
    {
        this._releaseId =releaseId;
        return this._releaseId;
    }

    public String getSelectedReleaseType() {
        System.out.println(selectedReleaseType + " selectedReleaseType");
        return selectedReleaseType;
    }

    public String getUserSelectedApplication() {
        System.out.println(userSelectedApplication);
        return userSelectedApplication;
    }

    public String getUserSelectedRelease() {
        return userSelectedRelease;
    }

    public String getUseSelectedApiType() {
        return selectedApi;
    }

}
