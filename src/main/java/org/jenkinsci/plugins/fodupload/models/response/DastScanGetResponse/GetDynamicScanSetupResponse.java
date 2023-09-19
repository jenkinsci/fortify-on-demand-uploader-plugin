package org.jenkinsci.plugins.fodupload.models.response.DastScanGetResponse;

import org.jenkinsci.plugins.fodupload.models.response.DastScanGetResponse.BlackoutEntry;
import org.jenkinsci.plugins.fodupload.models.response.DastScanGetResponse.ExclusionsList;
import org.jenkinsci.plugins.fodupload.models.response.DastScanGetResponse.IncludeUrl;
import org.jenkinsci.plugins.fodupload.models.response.FodDastApiResponse;

import java.util.ArrayList;
import java.util.List;

public class GetDynamicScanSetupResponse  extends FodDastApiResponse {

    private int geoLocationId;
    private String dynamicScanEnvironmentFacingType;
    private String exclusions;
    private String dynamicScanAuthenticationType;
    private boolean hasFormsAuthentication;
    private String primaryUserName;
    private String primaryUserPassword;
    private String secondaryUserName;
    private String secondaryUserPassword;
    private String otherUserName;
    private String otherUserPassword;
    private boolean vpnRequired;
    private String vpnUserName;
    private String vpnPassword;
    private boolean requiresNetworkAuthentication;
    private String networkUserName;
    private String networkPassword;
    private boolean multiFactorAuth;
    private String multiFactorAuthText;
    private String notes;
    private boolean requestCall;
    private boolean whitelistRequired;
    private String whitelistText;
    private String dynamicSiteURL;
    private String timeZone;
    private String repeatScheduleType;
    private int assessmentTypeId;
    private int entitlementId;
    private boolean allowFormSubmissions;
    private boolean allowSameHostRedirects;
    private boolean restrictToDirectoryAndSubdirectories;
    private boolean generateWAFVirtualPatch;
    private boolean isWebService;
    private String webServiceType;
    private String webServiceDescriptorURL;
    private String webServiceUserName;
    private String webServicePassword;
    private String webServiceAPIKey;
    private String webServiceAPIPassword;
    private String entitlementFrequencyType;
    private String userAgentType;
    private String concurrentRequestThreadsType;
    private String postmanCollectionURL;
    private String openApiURL;
    private String remoteManifestAuthorizationHeaderName;
    private String remoteManifestAuthorizationHeaderValue;
    private String networkName;

    public ArrayList<IncludeUrl> includeUrls;
    public ArrayList<ExclusionsList> exclusionsList;
    private BlackoutEntry[] blockout;

    public GetDynamicScanSetupResponse(boolean success, List<String> errors, String messages) {
        super(success, errors, messages);
    }

    public int getGeoLocationId() {
        return geoLocationId;
    }

    public String getDynamicScanEnvironmentFacingType() {
        return dynamicScanEnvironmentFacingType;
    }
    public String getDynamicScanAuthenticationType() {
        return dynamicScanAuthenticationType;
    }

    public boolean getHasFormsAuthentication() {
        return hasFormsAuthentication;
    }

    public String getPrimaryUserName() {
        return primaryUserName;
    }

    public String getPrimaryUserPassword() {
        return primaryUserPassword;
    }

    public String getSecondaryUserName() {
        return secondaryUserName;
    }

    public String getSecondaryUserPassword() {
        return secondaryUserPassword;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getOtherUserPassword() {
        return otherUserPassword;
    }

    public boolean getVpnRequired() {
        return vpnRequired;
    }
    public String getVpnUserName() {
        return vpnUserName;
    }

    public String getVpnPassword() {
        return vpnPassword;
    }

    public boolean getRequiresNetworkAuthentication() {
        return requiresNetworkAuthentication;
    }

    public String getNetworkUserName() {
        return networkUserName;
    }

    public String getNetworkPassword() {
        return networkPassword;
    }

    public boolean getMultiFactorAuth() {
        return multiFactorAuth;
    }

    public String getMultiFactorAuthText() {
        return multiFactorAuthText;
    }

    public String getNotes() {
        return notes;
    }
    public boolean getRequestCall() {
        return requestCall;
    }

    public boolean getWhitelistRequired() {
        return whitelistRequired;
    }

    public String getWhitelistText() {
        return whitelistText;
    }

    public String getDynamicSiteURL() {
        return dynamicSiteURL;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public String getRepeatScheduleType() {
        return repeatScheduleType;
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public boolean getAllowFormSubmissions() {
        return allowFormSubmissions;
    }
    public boolean getAllowSameHostRedirects() {
        return allowSameHostRedirects;
    }

    public boolean getRestrictToDirectoryAndSubdirectories() {
        return restrictToDirectoryAndSubdirectories;
    }

    public boolean getGenerateWAFVirtualPatch() {
        return generateWAFVirtualPatch;
    }
    public boolean getIsWebService() {
        return isWebService;
    }

    public String getWebServiceType() {
        return webServiceType;
    }

    public String getWebServiceDescriptorURL() {
        return webServiceDescriptorURL;
    }

    public String getWebServiceUserName() {
        return webServiceUserName;
    }

    public String getWebServicePassword() {
        return webServicePassword;
    }

    public String getWebServiceAPIKey() {
        return webServiceAPIKey;
    }

    public String getWebServiceAPIPassword() {
        return webServiceAPIPassword;
    }

    public String getEntitlementFrequencyType() {
        return entitlementFrequencyType;
    }

    public String getUserAgentType() {
        return userAgentType;
    }
    public String getConcurrentRequestThreadsType() {
        return concurrentRequestThreadsType;
    }

    public String getPostmanCollectionURL() {
        return postmanCollectionURL;
    }

    public String getOpenApiURL() {
        return openApiURL;
    }

    public String getRemoteManifestAuthorizationHeaderName() {
        return remoteManifestAuthorizationHeaderName;
    }

    public String getRemoteManifestAuthorizationHeaderValue() {
        return remoteManifestAuthorizationHeaderValue;
    }

    public String getNetworkName() {
        return networkName;
    }
//    private int geoLocationId;
//    private int assessmentTypeId;
//    private int entitlementId;
//    private String entitlementFrequencyType;
//    private String dynamicSiteURL;
//    private String timeZone;
//    public int getAssessmentTypeId() {
//        return assessmentTypeId;
//    }
//
//    public int getEntitlementId() {
//        return entitlementId;
//    }
//
//    public String getEntitlementFrequencyType() {
//        return entitlementFrequencyType;
//    }
//
//    public String getDynamicSiteURL() {
//        return dynamicSiteURL;
//    }
//
//    public String getTimeZone() {
//        return timeZone;
//    }
}
