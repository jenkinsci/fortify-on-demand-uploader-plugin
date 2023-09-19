package org.jenkinsci.plugins.fodupload.models;

import java.util.List;

public class DynamicScanJobModel {

    private boolean overrideGlobalConfig;
    String username;
    String personalAccessToken;
    String tenantId;
    String _releaseId;
    String selectedReleaseType;
    List<String> webSiteUrl;
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

    public List<String> getWebSiteUrl() {
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

    public boolean isWebSiteNetworkAuthSetting() {
        return webSiteNetworkAuthSetting;
    }

    public boolean isWebSiteLoginMacroSetting() {
        return webSiteLoginMacroSetting;
    }

    String selectedDynamicGeoLocation;
    boolean webSiteNetworkAuthSetting;
    boolean webSiteLoginMacroSetting;

    public DynamicScanJobModel(boolean overrideGlobalConfig, String username, String personalAccessToken, String tenantId, String releaseId, String selectedReleaseType, List<String> webSiteUrl, String dastEnv, String scanPolicyType, boolean scanHost, boolean allowHttp, boolean allowFormSubmissionCrawl, String selectedScanType, String selectedDynamicTimeZone, boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName, String loginFileMacro, String webSiteNetworkAuthPassword, String userSelectedApplication, String userSelectedRelease, String assessmentTypeId, String entitlementId, String entitlementFrequencyId, String entitlementFrequencyType, String userSelectedEntitlement, String selectedDynamicGeoLocation, boolean webSiteNetworkAuthSetting, boolean webSiteLoginMacroSetting) {
        this._releaseId = userSelectedRelease;
        this.tenantId = tenantId;
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.selectedReleaseType = selectedReleaseType;
        this.userSelectedApplication = userSelectedApplication;
        this.userSelectedRelease = userSelectedRelease;
        this.webSiteNetworkAuthSetting = webSiteNetworkAuthSetting;
        this.dastEnv = dastEnv;
        this.entitlementFrequencyId = entitlementFrequencyId;
        this.assessmentTypeId = assessmentTypeId;
        this.allowFormSubmissionCrawl = allowFormSubmissionCrawl;
        this.allowHttp = allowHttp;
        this.loginFileMacro = loginFileMacro;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.selectedScanType = selectedScanType;
        this.scanHost = scanHost;
        this.webSiteUrl = webSiteUrl;
        this.userSelectedEntitlement = userSelectedEntitlement;
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
        this.selectedDynamicGeoLocation = selectedDynamicGeoLocation;
        this.entitlementId = entitlementId;
        this.entitlementFrequencyId = entitlementFrequencyId;
        this.entitlementFrequencyType = entitlementFrequencyType;
        this.enableRedundantPageDetection = enableRedundantPageDetection;
        this.webSiteLoginMacroSetting = webSiteLoginMacroSetting;
        this.scanPolicyType = scanPolicyType;
        this.webSiteNetworkAuthUserName = webSiteNetworkAuthUserName;
        this.webSiteNetworkAuthPassword = webSiteNetworkAuthPassword;

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
