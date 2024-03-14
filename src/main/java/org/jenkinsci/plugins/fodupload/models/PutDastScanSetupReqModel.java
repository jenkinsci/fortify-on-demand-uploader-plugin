package org.jenkinsci.plugins.fodupload.models;

import java.io.Serializable;

public class PutDastScanSetupReqModel implements Serializable {

    public NetworkAuthentication getNetworkAuthenticationSettings() {
        return new NetworkAuthentication();
    }

    public void setNetworkAuthenticationSettings(NetworkAuthentication networkAuthenticationSettings) {
        this.networkAuthenticationSettings = networkAuthenticationSettings;
    }

    NetworkAuthentication networkAuthenticationSettings;

    public void setEntitlementFrequencyType(String entitlementFrequencyType) {
        this.entitlementFrequencyType = entitlementFrequencyType;
    }

    String entitlementFrequencyType;

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    String timeZone;

    public void setDynamicScanEnvironmentFacingType(String dynamicScanEnvironmentFacingType) {
        this.dynamicScanEnvironmentFacingType = dynamicScanEnvironmentFacingType;
    }

    String dynamicScanEnvironmentFacingType;

    public void setEntitlementId(int entitlementId) {
        this.entitlementId = entitlementId;
    }

    int entitlementId;

    public void setAssessmentTypeId(int assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    int assessmentTypeId;

    public void setRequiresNetworkAuthentication(boolean requiresNetworkAuthentication) {
        this.requiresNetworkAuthentication = requiresNetworkAuthentication;
    }

    public boolean requiresNetworkAuthentication;

    public class NetworkAuthentication implements Serializable {

        public void setUserName(String userName) {
            this.userName = userName;
        }

        String userName;

        public void setPassword(String password) {
            this.password = password;
        }

        String password;

        public void setNetworkAuthenticationType(String networkAuthenticationType) {
            this.networkAuthenticationType = networkAuthenticationType;
        }

        public String networkAuthenticationType;

    }

    public void setRequestFalsePositiveRemoval(boolean requestFalsePositiveRemoval) {
        this.requestFalsePositiveRemoval = requestFalsePositiveRemoval;
    }

    public boolean requestFalsePositiveRemoval;


}
