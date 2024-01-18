package org.jenkinsci.plugins.fodupload.models.response.Dast;

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

    public int timeBoxInHours;

    public Policy policy;
    public Website websiteAssessment;
    public API apiAssessment;

    public ExclusionDTO[] exclusionsList;
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





    public class  FileDetails{
        public int fileId;
        public String fileName;
        public String fileType;
    }
    public enum DayOfWeekTypes {
        Sunday,
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday
    }


}
