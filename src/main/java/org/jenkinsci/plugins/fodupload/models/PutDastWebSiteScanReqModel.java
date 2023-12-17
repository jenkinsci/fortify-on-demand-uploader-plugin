package org.jenkinsci.plugins.fodupload.models;

public class PutDastWebSiteScanReqModel extends PutDastScanSetupReqModel {

    public void setDynamicSiteUrl(String dynamicSiteUrl) {
        this.dynamicSiteUrl = dynamicSiteUrl;
    }
    String dynamicSiteUrl;
    boolean enableRedundantPageDetection;
    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }
    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }
    public void setRequiresSiteAuthentication(boolean requiresSiteAuthentication) {
        this.requiresSiteAuthentication = requiresSiteAuthentication;
    }
    boolean requiresSiteAuthentication;
    public void setRestrictToDirectoryAndSubdirectories(boolean restrictToDirectoryAndSubdirectories) {
        this.restrictToDirectoryAndSubdirectories = restrictToDirectoryAndSubdirectories;
    }
    boolean restrictToDirectoryAndSubdirectories;
    public void setTimeBoxInHours(Integer timeBoxInHours) {
        this.timeBoxInHours = timeBoxInHours;
    }
    Integer timeBoxInHours;
    ExclusionsList exclusionsList;
    public class ExclusionsList {
        public String  value;
    }
    public void setLoginMacroFileId(int loginMacroFileId) {
        this.loginMacroFileId = loginMacroFileId;
    }
    public void setPolicy(String policy) {
        this.policy = policy;
    }
    String policy;
    int loginMacroFileId;
}