package org.jenkinsci.plugins.fodupload.models;

import java.util.ArrayList;
import java.util.List;

/*
{
  "urls": [
    "string"
  ],
  "enableRedundantPageDetection": true,
  "loginMacroFileId": 0,
  "requiresSiteAuthentication": true,
  "exclusionsList": [
    {
      "value": "string"
    }
  ],
  "restrictToDirectoryAndSubdirectories": true,
  "assessmentTypeId": 0,
  "entitlementFrequencyType": "SingleScan",
  "geoLocationId": 0,
  "dynamicScanEnvironmentFacingType": "Internal",
  "policy": "Standard",
  "timeZone": "string",
  "timeBoxInHours": 0,
  "blockout": [
    {
      "day": "Sunday",
      "hourBlocks": [
        {
          "hour": 0,
          "checked": true
        }
      ]
    }
  ],
  "requiresNetworkAuthentication": true,
  "networkAuthenticationSettings": {
    "networkAuthenticationType": "Basic",
    "userName": "string",
    "password": "string"
  }
}
 */
public class PutDastScanSetupReqModel {
    private int geoLocationId;
    private int assessmentTypeId;
    private int entitlementId;
    private String entitlementFrequencyType;
    private String dynamicScanEnvironmentFacingType;
    private String dynamicScanAuthenticationType;
    private String timeZone;
    private String scanType;
    private String policy;
    private int loginMacroFileId;
    private boolean requiresSiteAuthentication;
    private NetworkAuthentication networkAuthenticationSettings;
    private boolean allowFormSubmissions;
    private boolean allowSameHostRedirects;

    public boolean requiresNetworkAuthentication;
    private boolean restrictToDirectoryAndSubdirectories;
    private boolean enableRedundantPageDetection;

    private ExcludeSiteUrl excludeSiteUrl;

    public class ExcludeSiteUrl {
        public List<String> urls;

        ExcludeSiteUrl() {
            urls = new ArrayList<>();
        }

    }

    public String[] Urls;

    public String[] getUrls() {
        return Urls;
    }

    public void setUrls(String[] urls) {
        Urls = urls;
    }


    public boolean isAllowFormSubmissions() {
        return allowFormSubmissions;
    }

    public void setAllowFormSubmissions(boolean allowFormSubmissions) {
        this.allowFormSubmissions = allowFormSubmissions;
    }

    public boolean isAllowSameHostRedirects() {
        return allowSameHostRedirects;
    }

    public void setAllowSameHostRedirects(boolean allowSameHostRedirects) {
        this.allowSameHostRedirects = allowSameHostRedirects;
    }

    public boolean isRestrictToDirectoryAndSubdirectories() {
        return restrictToDirectoryAndSubdirectories;
    }

    public void setRestrictToDirectoryAndSubdirectories(boolean restrictToDirectoryAndSubdirectories) {
        this.restrictToDirectoryAndSubdirectories = restrictToDirectoryAndSubdirectories;
    }

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }

    public NetworkAuthentication getNetworkAuthenticationSettings() {
        return new NetworkAuthentication();
    }

    public void setNetworkAuthenticationSettings(NetworkAuthentication networkAuthenticationSettings) {
        this.networkAuthenticationSettings = networkAuthenticationSettings;
    }

    public ExcludeSiteUrl setWebSites() {
        if (excludeSiteUrl == null)
            excludeSiteUrl = new ExcludeSiteUrl();

        return excludeSiteUrl;
    }

    public String getEntitlementFrequencyType() {
        return entitlementFrequencyType;
    }

    public void setEntitlementFrequencyType(String entitlementFrequencyType) {
        this.entitlementFrequencyType = entitlementFrequencyType;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getDynamicScanAuthenticationType() {
        return dynamicScanAuthenticationType;
    }

    public void setDynamicScanAuthenticationType(String dynamicScanAuthenticationType) {
        this.dynamicScanAuthenticationType = dynamicScanAuthenticationType;
    }

    public String getDynamicScanEnvironmentFacingType() {
        return dynamicScanEnvironmentFacingType;
    }

    public void setDynamicScanEnvironmentFacingType(String dynamicScanEnvironmentFacingType) {
        this.dynamicScanEnvironmentFacingType = dynamicScanEnvironmentFacingType;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public void setEntitlementId(int entitlementId) {
        this.entitlementId = entitlementId;
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public void setAssessmentTypeId(int assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    public int getGeoLocationId() {
        return geoLocationId;
    }

    public void setGeoLocationId(int geoLocationId) {
        this.geoLocationId = geoLocationId;
    }

    //Todo:-Take care the serialization here
    public class NetworkAuthentication {
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String userName;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String password;


        public String getNetworkAuthenticationType() {
            return networkAuthenticationType;
        }

        public void setNetworkAuthenticationType(String networkAuthenticationType) {
            this.networkAuthenticationType = networkAuthenticationType;
        }

        public String networkAuthenticationType;

        public void setNetworkAuthenticationRequired(boolean isRequire) {
            requiresNetworkAuthentication = isRequire;

        }

    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public int getLoginMacroFileId() {
        return loginMacroFileId;
    }

    public void setLoginMacroFileId(int loginMacroFileId) {
        this.loginMacroFileId = loginMacroFileId;
    }

    public boolean isRequiresSiteAuthentication() {
        return requiresSiteAuthentication;
    }

    public void setRequiresSiteAuthentication(boolean requiresSiteAuthentication) {
        this.requiresSiteAuthentication = requiresSiteAuthentication;
    }

    //  public PutDynamicScanSetupModel(int geoLocationId, int assessmentTypeId, int entitlementId,String entitlementFrequencyType, String timeZone)
    public PutDastScanSetupReqModel() {
    }
}
