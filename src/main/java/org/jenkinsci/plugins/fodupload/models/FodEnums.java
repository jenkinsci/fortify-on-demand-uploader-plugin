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
        DynamicScanWebServiceTypes
    }

    public enum GrantType {CLIENT_CREDENTIALS, PASSWORD}

    ;

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
        DoNotStartScan("DoNotStartScan"),
        CancelInProgressScan("CancelInProgressScan");

        private final String _val;

        InProgressScanActionType(String val) {
            this._val = val;
        }

        public static InProgressScanActionType fromInt(int val) {
            switch (val) {
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


}
