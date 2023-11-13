package org.jenkinsci.plugins.fodupload.models;

import java.util.List;

public class DastScanJobModel {

    private boolean overrideGlobalConfig;
    String username;

    String personalAccessToken;
    String tenantId;
    String _releaseId;
    String selectedReleaseType;
    String webSiteUrl;
    String dastEnv;
    String scanPolicyType;
    boolean scanHost;
    boolean allowHttp;
    boolean allowFormSubmissionCrawl;
    String selectedScanType;
    String selectedDynamicTimeZone;
    boolean enableRedundantPageDetection;
    String webSiteNetworkAuthUserName;
    String loginFileMacro;
    String webSiteNetworkAuthPassword;
    String userSelectedApplication;
    String userSelectedRelease;
    String assessmentTypeId;
    String entitlementId;
    String entitlementFrequencyId;
    String entitlementFrequencyType;
    String userSelectedEntitlement;
    String networkAuthType;
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
    String selectedGraphQlApiHost;
    String selectedGraphQlApiServicePath;
    String selectedGrpcupload;
    String selectedGrpcSchemeType;
    String selectedGrpcApiHost;
    String selectedGrpcApiServicePath;

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

    public String getSelectedScanType() {
        return selectedScanType;
    }

    public String getSelectedDynamicTimeZone() {
        return selectedDynamicTimeZone;
    }

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    public String getWebSiteNetworkAuthUserName() {
        return webSiteNetworkAuthUserName;
    }

    public String getLoginFileMacro() {
        return loginFileMacro;
    }

    public String getWebSiteNetworkAuthPassword() {
        return webSiteNetworkAuthPassword;
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
    public String getSelectedOpenApiSource(){return selectedOpenApiSource;}
    public String getSelectedApi(){return selectedApi;}
    public String getSelectedOpenApiurl(){return selectedOpenApiurl;}
    public String getSelectedApiKey(){return selectedApiKey;}
    public String getSelectedOpenApiFileSource(){return selectedOpenApiFileSource;}
    public String getSelectedPostmanFile(){return selectedPostmanFile;}
    public String getSelectedGraphQlSource(){return selectedGraphQlSource;}
    public String getSelectedGraphQlUpload(){return selectedGraphQlUpload;}
    public String getSelectedGraphQlUrl(){return selectedGraphQlUrl;}
    public String getSelectedGraphQlApiHost(){return selectedGraphQlApiHost;}
    public String getSelectedGraphQLSchemeType(){return selectedGraphQLSchemeType;}
    public String getSelectedGraphQlApiServicePath(){return selectedGraphQlApiServicePath;}
    public String getSelectedGrpcupload(){return  selectedGrpcupload;}
    public String getSelectedGrpcApiHost(){return selectedGrpcApiHost;}
    public String getSelectedGrpcSchemeType(){return selectedGrpcSchemeType;}
    public String getSelectedGrpcApiServicePath(){return selectedGrpcApiServicePath;}

    String selectedDynamicGeoLocation;
    boolean webSiteNetworkAuthEnabled;
    boolean webSiteLoginMacroEnabled;

    public DastScanJobModel(boolean overrideGlobalConfig, String username,
                            String personalAccessToken, String tenantId,
                            String releaseId, String selectedReleaseType,
                            String webSiteUrl, String dastEnv,
                            String scanTimebox,
                            List<String> standardScanTypeExcludeUrls,
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
                               String selectedApiType,
                               String openApiSource, String openApiFileSource, String openApiurl, String apiKey,
                               String postmanFile,
                               String graphQlSource,String graphQlUpload, String graphQlUrl, String graphQLSchemeType, String graphQlApiHost, String graphQlApiServicePath,
                               String grpcupload, String grpcSchemeType, String grpcApiHost, String grpcApiServicePath) {
        this._releaseId = userSelectedRelease;
        this.tenantId = tenantId;
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.selectedReleaseType = selectedReleaseType;
        this.userSelectedApplication = userSelectedApplication;
        this.userSelectedRelease = userSelectedRelease;
        this.webSiteNetworkAuthEnabled = webSiteNetworkAuthSettingEnabled;
        this.dastEnv = dastEnv;
        this.assessmentTypeId = assessmentTypeId;
        this.allowFormSubmissionCrawl = allowFormSubmissionCrawl;
        this.allowHttp = allowHttp;
        this.loginFileMacro = loginMacroId;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.selectedScanType = selectedScanType;
        this.scanHost = scanScope;
        this.webSiteUrl = webSiteUrl;
        this.userSelectedEntitlement = userSelectedEntitlement;
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
        this.selectedDynamicGeoLocation = selectedDynamicGeoLocation;
        this.entitlementId = entitlementId;
        this.entitlementFrequencyType = entitlementFrequencyType;
        this.enableRedundantPageDetection = enableRedundantPageDetection;
        this.webSiteLoginMacroEnabled = webSiteLoginMacroEnabled;
        this.scanPolicyType = scanPolicyType;
        this.webSiteNetworkAuthUserName = webSiteNetworkAuthUserName;
        this.webSiteNetworkAuthPassword = webSiteNetworkAuthPassword;
        this.networkAuthType = selectedNetworkAuthType;
        this.selectedApi = selectedApiType;
        this.selectedOpenApiFileSource = openApiFileSource;
        this.selectedOpenApiSource = openApiSource;
        this.selectedApiKey = apiKey;
        this.selectedPostmanFile = postmanFile;
        this.selectedGraphQlSource = graphQlSource;
        this.selectedGraphQlUrl = graphQlUrl;
        this.selectedGraphQlUpload = graphQlUpload;
        this.selectedGraphQlApiHost = graphQlApiHost;
        this.selectedGraphQLSchemeType = graphQLSchemeType;
        this.selectedGraphQlApiServicePath = graphQlApiServicePath;
        this.selectedGrpcupload = grpcupload;
        this.selectedGrpcApiHost = grpcApiHost;
        this.selectedGrpcSchemeType = grpcSchemeType;
        this.selectedGrpcApiServicePath = grpcApiServicePath;
    }

    public String get_releaseId() {
        System.out.println(_releaseId + " selected release id");
        return _releaseId;
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

    public String getUseSelectedApiType(){return selectedApi;}

}
