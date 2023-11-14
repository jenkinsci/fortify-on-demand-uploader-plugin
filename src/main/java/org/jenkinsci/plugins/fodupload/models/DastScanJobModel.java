package org.jenkinsci.plugins.fodupload.models;

import java.util.List;

public class DastScanJobModel {

    private boolean overrideGlobalConfig;
    String username;
    String timeBoxScan;
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

    public DastScanJobModel(Boolean overrideGlobalConfig, String username, String personalAccessToken, String tenantId, String releaseId,
                            String webSiteUrl, String dastEnv, String scanTimebox,
                            boolean scanScope, String selectedScanType, String scanPolicy,
                            String selectedDynamicTimeZone,
                            boolean enableRedundantPageDetection,
                            String webSiteNetworkAuthUserName, int loginMacroId,
                            String workflowMacroId, String allowedHost,
                            String webSiteNetworkAuthPassword, String assessmentTypeId,
                            String entitlementId, String entitlementFrequencyType,
                            String selectedNetworkAuthType) {

        this._releaseId = releaseId;
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.tenantId = tenantId;
        this.webSiteUrl = webSiteUrl;
        this.dastEnv = dastEnv;
        this.timeBoxScan = scanTimebox;
        this.scanPolicyType = scanPolicy;
        this.enableRedundantPageDetection = enableRedundantPageDetection;
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
        this.webSiteNetworkAuthPassword = webSiteNetworkAuthPassword;
        this.webSiteNetworkAuthUserName = webSiteNetworkAuthUserName;
        this.selectedScanType = selectedScanType;
        this.assessmentTypeId = assessmentTypeId;
        this.entitlementId = entitlementId;
        this.entitlementFrequencyType = entitlementFrequencyType;
        this.networkAuthType = selectedNetworkAuthType;

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
                            String selectedDynamicGeoLocation, String selectedNetworkAuthType) {
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

}
