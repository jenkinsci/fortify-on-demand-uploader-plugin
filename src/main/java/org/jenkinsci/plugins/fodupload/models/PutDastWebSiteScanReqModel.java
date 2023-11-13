package org.jenkinsci.plugins.fodupload.models;

/* Request Model For saving website Scan setting
{
  "dynamicSiteUrl": "string",
  "enableRedundantPageDetection": true,
  "loginMacroFileId": 0,
  "requiresSiteAuthentication": true,
  "exclusionsList": [
    {
      "value": "string"
    }
  ],
  "restrictToDirectoryAndSubdirectories": true,
  "policy": "Standard",
  "assessmentTypeId": 0,
  "entitlementId": 0,
  "entitlementFrequencyType": "SingleScan",
  "dynamicScanEnvironmentFacingType": "Internal",
  "timeZone": "string",
  "timeBoxInHours": 0,
  "requiresNetworkAuthentication": true,
  "networkAuthenticationSettings": {
    "networkAuthenticationType": "Basic",
    "userName": "string",
    "password": "string"
  }
}
 */
public class PutDastWebSiteScanReqModel extends PutDastScanSetupReqModel {

    public void setDynamicSiteUrl(String dynamicSiteUrl) {
        this.dynamicSiteUrl = dynamicSiteUrl;
    }
    private String dynamicSiteUrl;
    private boolean enableRedundantPageDetection;

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }
    public void setRequiresSiteAuthentication(boolean requiresSiteAuthentication) {
        this.requiresSiteAuthentication = requiresSiteAuthentication;
    }
    private  boolean requiresSiteAuthentication;

    public void setRestrictToDirectoryAndSubdirectories(boolean restrictToDirectoryAndSubdirectories) {
        this.restrictToDirectoryAndSubdirectories = restrictToDirectoryAndSubdirectories;
    }
    private  boolean restrictToDirectoryAndSubdirectories;

    public void setTimeBoxInHours(int timeBoxInHours) {
        this.timeBoxInHours = timeBoxInHours;
    }
    int timeBoxInHours;
    private ExclusionsList exclusionsList;
    public class ExclusionsList {
        public String  value;
    }
    public void setLoginMacroFileId(int loginMacroFileId) {
        this.loginMacroFileId = loginMacroFileId;
    }

    int loginMacroFileId;
}
