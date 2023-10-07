package org.jenkinsci.plugins.fodupload.models;

import java.util.ArrayList;
import java.util.List;

public class PutDynamicWebSiteScanSetupModel {
    private int geoLocationId;
    private int assessmentTypeId;
    private int entitlementId;
    private String entitlementFrequencyType;
    private String dynamicScanEnvironmentFacingType;
    private String dynamicScanAuthenticationType;
    private String timeZone;
    private boolean requiresNetworkAuthentication;
    private String scanType;
    private String policy;
    private int loginMacroFileId;
    private boolean requiresSiteAuthentication;
    private NetworkAuthentication networkAuthenticationSettings;
    private boolean allowFormSubmissions;
    private boolean allowSameHostRedirects;
    private boolean restrictToDirectoryAndSubdirectories;
    private boolean enableRedundantPageDetection;

    public String[] getUrls() {
        return Urls;
    }

    public void setUrls(String[] urls) {
        Urls = urls;
    }

    private String[] Urls;

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
        return networkAuthenticationSettings;
    }

    public void setNetworkAuthenticationSettings(NetworkAuthentication networkAuthenticationSettings) {
        this.networkAuthenticationSettings = networkAuthenticationSettings;
    }


    //Todo:-Take care the serialization here
    public class NetworkAuthentication {
        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        String userName;

        public boolean isRequireNetworkAuthentication() {
            return isRequireNetworkAuthentication;
        }

        public void setRequireNetworkAuthentication(boolean requireNetworkAuthentication) {
            isRequireNetworkAuthentication = requireNetworkAuthentication;
        }

        boolean isRequireNetworkAuthentication;

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

        String networkAuthenticationType;
    }

//    private BlackoutEntry[] blockout;

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

    public boolean isRequiresNetworkAuthentication() {
        return requiresNetworkAuthentication;
    }

    public void setRequiresNetworkAuthentication(boolean requiresNetworkAuthentication) {
        this.requiresNetworkAuthentication = requiresNetworkAuthentication;

        if (requiresNetworkAuthentication) {
            this.setNetworkAuthenticationSettings(new NetworkAuthentication());
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

    public class WebSite {
        public String[] Urls;
    }

    //  public PutDynamicScanSetupModel(int geoLocationId, int assessmentTypeId, int entitlementId,String entitlementFrequencyType, String timeZone)
    public PutDynamicWebSiteScanSetupModel() {

        String jsonArrayString = "[{\"day\":\"Sunday\",\"hourBlocks\":[{\"hour\":3,\"checked\":true}]},{\"day\":\"Monday\",\"hourBlocks\":[{\"hour\":3,\"checked\":true}]},{\"day\":\"Tuesday\",\"hourBlocks\":[{\"hour\":3,\"checked\":true}]},{\"day\":\"Wednesday\",\"hourBlocks\":[{\"hour\":3,\"checked\":true}]},{\"day\":\"Thursday\",\"hourBlocks\":[{\"hour\":3,\"checked\":true}]}]";

//        this.geoLocationId = geoLocationId;
//        this.assessmentTypeId = assessmentTypeId;
//        this.entitlementId = entitlementId;
//        this.entitlementFrequencyType = entitlementFrequencyType;
//        this.dynamicSiteURL = "http://zero.webappsecurity.com";
//        this.timeZone = timeZone;
//        this.dynamicScanAuthenticationType = "NoAuthentication";
//        this.dynamicScanEnvironmentFacingType = "Internal";
//        WebSite.Urls.
//        Gson gson = new Gson();
//        BlackoutEntry[] blackoutEntries = gson.fromJson(jsonArrayString, BlackoutEntry[].class);
//        this.blockout = blackoutEntries;
////        System.out.println(blackoutEntries);
    }

}
