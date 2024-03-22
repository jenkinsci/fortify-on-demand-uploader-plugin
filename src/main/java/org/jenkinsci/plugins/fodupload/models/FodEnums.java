package org.jenkinsci.plugins.fodupload.models;

public class FodEnums {

    public enum APILookupItemTypes {
        All,
        MobileScanPlatformTypes,
        MobileScanFrameworkTypes,
        MobileScanEnvironmentTypes,
        MobileScanRoleTypes,
        MobileScanExternalDeviceTypes,
        DynamicScanEnvironmentFacingTypes,
        DynamicScanAuthenticationTypes,
        TimeZones,
        RepeatScheduleTypes,
        GeoLocations,
        SDLCStatusTypes,
        DayOfWeekTypes,
        BusinessCriticalityTypes,
        ReportTemplateTypes,
        AnalysisStatusTypes,
        ScanStatusTypes,
        ReportFormats,
        Roles,
        ScanPreferenceTypes,
        AuditPreferenceTypes,
        EntitlementFrequencyTypes,
        ApplicationTypes,
        ScanTypes,
        AttributeTypes,
        AttributeDataTypes,
        MultiFactorAuthorizationTypes,
        ReportTypes,
        ReportStatusTypes,
        PassFailReasonTypes,
        DynamicScanWebServiceTypes,
        TechnologyTypes,
        LanguageLevels,

        NetworkAuthenticationType
    }

    public enum GrantType {CLIENT_CREDENTIALS, PASSWORD}

    public enum EntitlementPreferenceType {
        SubscriptionFirstThenSingleScan("SubscriptionFirstThenSingleScan"),
        SingleScanFirstThenSubscription("SingleScanFirstThenSubscription"),
        SubscriptionOnly("SubscriptionOnly"),
        SingleScanOnly("SingleScanOnly");

        private final String _val;

        EntitlementPreferenceType(String val) {
            this._val = val;
        }

        public static EntitlementPreferenceType fromInt(int val) {
            switch (val) {
                case 4:
                    return SubscriptionFirstThenSingleScan;
                case 3:
                    return SingleScanFirstThenSubscription;
                case 2:
                    return SubscriptionOnly;
                case 1:
                    return SingleScanOnly;
                default:
                    return null;
            }
        }

        public String getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case "SubscriptionFirstThenSingleScan":
                    return "Subscription First Then Single Scan";
                case "SingleScanFirstThenSubscription":
                    return "Single Scan First Then Subscription";
                case "SubscriptionOnly":
                    return "Subscription Only";
                case "SingleScanOnly":
                default:
                    return "Single Scan Only";
            }
        }
    }

    public enum RemediationScanPreferenceType {
        NonRemediationScanOnly("NonRemediationScanOnly"),
        RemediationScanOnly("RemediationScanOnly"),
        RemediationScanIfAvailable("RemediationScanIfAvailable");

        private final String _val;

        RemediationScanPreferenceType(String val) {
            this._val = val;
        }

        public static RemediationScanPreferenceType fromInt(int val) {
            switch (val) {
                case 3:
                    return NonRemediationScanOnly;
                case 2:
                    return RemediationScanOnly;
                case 1:
                default:
                    return RemediationScanIfAvailable;
            }
        }

        public String getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case "NonRemediationScanOnly":
                    return "Non-Remediation Scan Only";
                case "RemediationScanOnly":
                    return "Remediation Scan Only";
                case "RemediationScanIfAvailable":
                default:
                    return "Remediation Scan If Available";
            }
        }
    }

    public enum InProgressScanActionType {
        Queue("Queue"),
        DoNotStartScan("DoNotStartScan"),
        CancelInProgressScan("CancelInProgressScan");

        private final String _val;

        InProgressScanActionType(String val) {
            this._val = val;
        }

        public static InProgressScanActionType fromInt(int val) {
            switch (val) {
                case 3:
                    return Queue;
                case 2:
                    return CancelInProgressScan;
                case 1:
                default:
                    return DoNotStartScan;
            }
        }

        public String getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case "Queue":
                    return "Queue";
                case "CancelInProgressScan":
                    return "Cancel In-Progress Scan";
                case "DoNotStartScan":
                default:
                    return "Do Not Start Scan";
            }
        }
    }

    public enum InProgressBuildResultType {
        FailBuild("FailBuild"),
        WarnBuild("WarnBuild");

        private final String _val;

        InProgressBuildResultType(String val) {
            this._val = val;
        }

        public static InProgressBuildResultType fromInt(int val) {
            switch (val) {
                case 2:
                    return WarnBuild;
                case 1:
                default:
                    return FailBuild;
            }
        }

        public String getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case "WarnBuild":
                    return "Provide Warning";
                case "FailBuild":
                default:
                    return "Fail Build";
            }
        }
    }


    public enum SelectedReleaseType {
        UseBsiToken("UseBsiToken"),
        UseReleaseId("UseReleaseId"),
        UseAppAndReleaseName("UseAppAndReleaseName");

        private final String _val;

        SelectedReleaseType(String val) {
            this._val = val;
        }

        public static SelectedReleaseType fromInt(int val) {
            switch (val) {
                case 3:
                    return UseAppAndReleaseName;
                case 2:
                    return UseReleaseId;
                case 1:
                default:
                    return UseBsiToken;
            }
        }

        public String getValue() {
            return this._val;
        }

        public int getInteger() {
            switch (this._val) {
                case "UseAppAndReleaseName":
                    return 3;
                case "UseReleaseId":
                    return 2;
                case "UseBsiToken":
                default:
                    return 1;
            }
        }

        public String toString() {
            switch (this._val) {
                case "UseAppAndReleaseName":
                    return "Application and Release Options";
                case "UseReleaseId":
                    return "Release ID";
                case "UseBsiToken":
                default:
                    return "BSI Token";
            }
        }
    }

    public enum SelectedScanCentralBuildType {

        None,
        Gradle,
        Maven,
        MSBuild,
        DotNet,
        PHP,
        Go,
        Python;

        public static SelectedScanCentralBuildType fromInt(int val) {
            switch (val) {
                case 1:
                    return Gradle;
                case 2:
                    return Maven;
                case 3:
                    return MSBuild;
                case 4:
                    return PHP;
                case 5:
                    return Python;
                case 6:
                    return Go;
                case 0:
                default:
                    return None;
            }
        }

        public int getInteger() {
            switch (this.name()) {
                case "Gradle":
                    return 1;
                case "Maven":
                    return 2;
                case "MSBuild":
                    return 3;
                case "PHP":
                    return 4;
                case "Python":
                    return 5;
                case "Go":
                    return 6;
                default:
                    return 0;
            }
        }

    }

    public enum DastScanFileTypes {
        OpenAPIDefinition("OpenAPIDefinition"),
        GraphQLDefinition("GraphQLDefinition"),
        GRPCDefinition("GRPCDefinition"),
        WorkflowDrivenMacro("WorkflowDrivenMacro"),
        LoginMacro("LoginMacro"),
        PostmanCollection("PostmanCollection");
        private final String _val;

        public static DastScanFileTypes fromInt(int val) {
            switch (val) {
                case 7:
                    return PostmanCollection;
                case 6:
                    return LoginMacro;
                case 5:
                    return WorkflowDrivenMacro;
                case 4:
                    return GRPCDefinition;

                case 3:
                    return GraphQLDefinition;

                case 2:
                    return OpenAPIDefinition;
                case 1:
                    return OpenAPIDefinition;

            }
            return null;
        }

        DastScanFileTypes(String val) {
            this._val = val;
        }

        public String getValue() {
            return this._val;
        }

    }

    public enum DastEnvironmentType {
        External,
        Internal

    }

    public enum DastScanType {
        Website("Website"),
        Workflow("Workflow-driven"),
        API("API");
        private final String _val;

        DastScanType(String val) {
            this._val = val;
        }

        public String toString() {
            switch (this._val) {
                case "Workflow-driven":
                    return "Workflow-driven";
                case "Website":
                    return "Website";
                case "API":
                    return "API";
                default:
                    return "none";

            }

        }

    }

    public enum DastPolicy {

        Standard("Standard"),
        Critical_and_high("CriticalsAndHighs"),
        Passive("PassiveScan");

        private final String _val;

        DastPolicy(String val) {
            this._val = val;
        }

        public int getInteger() {
            switch (this._val) {
                case "PassiveScan":
                    return 3;
                case "CriticalsAndHighs":
                    return 2;
                case "Standard":
                    return 1;
            }
            return 0;
        }

        public static DastPolicy fromInt(int val) {
            switch (val) {

                case 3:
                    return Passive;

                case 2:
                    return Critical_and_high;
                case 1:
                default:
                    return Standard;
            }
        }

        public String getValue() {
            return this._val;
        }

        @SuppressWarnings("NP_TOSTRING_COULD_RETURN_NULL")
        public String toString() {
            switch (this._val) {
                case "Standard":
                    return "Standard";
                case "CriticalsAndHighs":
                    return "Critical and high";
                case "Passive":
                    return "Passive";

            }

            return null;
        }
    }

    public enum DastReleaseType {
        UseReleaseId("UseReleaseId"),
        UseAppAndReleaseName("UseAppAndReleaseName");
        private final String _val;

        DastReleaseType(String val) {
            this._val = val;
        }

        public static DastReleaseType fromInt(int val) {
            switch (val) {
                case 2:
                    return UseAppAndReleaseName;
                case 1:
                default:
                    return UseReleaseId;

            }
        }

        public String getValue() {
            return this._val;
        }

        public int getInteger() {
            switch (this._val) {
                case "UseAppAndReleaseName":
                    return 2;
                case "UseReleaseId":
                default:
                    return 1;
            }
        }

        public String toString() {
            switch (this._val) {

                case "UseAppAndReleaseName":
                    return "Application and Release Options";
                case "UseReleaseId":
                default:
                    return "Release ID";

            }
        }
    }

    public enum DastApiType {
        OpenApi("openApi"),
        Grpc("grpc"),
        GraphQL("graphQl"),
        Postman("postman");
        private final String _val;

        DastApiType(String val) {
            this._val = val;
        }

        public String toString() {
            switch (this._val) {
                case "openApi":
                    return "openApi";
                case "graphQl":
                    return "graphQl";
                case "grpc":
                    return "grpc";
                case "postman":
                    return "postman";
                default:
                    return "none";

            }

        }

        public String getValue() {
            return this._val;
        }

    }

    public enum ApiSourceType {
        FileId("FileId"),
        Url("Url");

        private final String _val;

        ApiSourceType(String val) {
            this._val = val;
        }

        public String toString() {
            switch (this._val) {
                case "FileId":
                    return "FileId";
                case "Url":
                    return "Url";
                default:
                    return "none";

            }

        }

    }

}
