package org.jenkinsci.plugins.fodupload.models.response.Dast;
public class GetDastAutomatedScanSetupResponse {

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

    public enum ScanTypes {
        Static,
        Dynamic,
        Mobile,
        Monitoring,
        Network,
        OpenSource,
    }

    public ScanTypes scanType;

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

    public Policy policy;
    public Website websiteAssessment;
    public API apiAssessment;
    public int timeBoxInHours;
    public ExclusionDTO[] exclusionsList;
    public  WorkflowdrivenAssessment workflowdrivenAssessment;
    public int loginMacroFileId;
    public boolean requiresSiteAuthentication;
    public boolean enableRedundantPageDetection;
    public boolean allowFormSubmissions;
    public boolean allowSameHostRedirects;
    public boolean restrictToDirectoryAndSubdirectories;
    public boolean requiresNetworkAuthentication;
    public NetworkAuthenticationSettings networkAuthenticationSettings;
    public enum DayOfWeekTypes
    {
        Sunday,
        Monday ,
        Tuesday,
        Wednesday ,
        Thursday ,
        Friday ,
        Saturday
    }


}
