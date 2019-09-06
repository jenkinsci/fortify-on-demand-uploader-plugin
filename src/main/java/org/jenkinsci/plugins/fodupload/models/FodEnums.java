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
        SubscriptionFirstThenSingleScan(4),
        SingleScanFirstThenSubscription(3),
        SubscriptionOnly(2),
        SingleScanOnly(1);

        private final int _val;

        EntitlementPreferenceType(int val) {
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

        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 4:
                    return "SubscriptionFirstThenSingleScan";
                case 3:
                    return "SingleScanFirstThenSubscription";
                case 2:
                    return "SubscriptionOnly";
                case 1:
                default:
                    return "SingleScanOnly";
            }
        }
    }
}
