package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
@SuppressFBWarnings("UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD")
public class GetDastScanSettingResponse {

    public int assessmentTypeId;
    public int geoLocationId;

    public int entitlementId;
    public String timeZone;
    public DynamicScanEnvironmentFacingTypes dynamicScanEnvironmentFacingType;

    public enum DynamicScanEnvironmentFacingTypes {
        Internal,
        External,
    }
    public enum EntitlementFrequencyTypes {
        SingleScan,
        Subscription
    }
    public EntitlementFrequencyTypes entitlementFrequencyType;
    public String scanType;

    public enum Policy {
        Standard,
        Api,
        CriticalsAndHighs,
        PassiveScan
    }

    public enum ApiSource {
        FileId,
        Url
    }
    public int timeBoxInHours;

    public Policy policy;
    public Website websiteAssessment;
    public API apiAssessment;
    public WorkflowdrivenAssessment workflowdrivenAssessment;
    public int loginMacroFileId;
    public boolean requiresSiteAuthentication;
    public boolean enableRedundantPageDetection;
    public boolean allowFormSubmissions;
    public boolean allowSameHostRedirects;
    public boolean restrictToDirectoryAndSubdirectories;
    public boolean requiresNetworkAuthentication;
    public NetworkAuthenticationSettings networkAuthenticationSettings;
    public FileDetails[] fileDetails;
    public boolean hasUtilizedAdditionalServices;
    public boolean requestFalsePositiveRemoval;

    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC")
    public class  FileDetails{
        public int fileId;
        public String fileName;
        public String fileType;
    }

}
