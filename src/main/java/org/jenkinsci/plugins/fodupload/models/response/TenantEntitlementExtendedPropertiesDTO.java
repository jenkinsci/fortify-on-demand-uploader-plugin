package org.jenkinsci.plugins.fodupload.models.response;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.*;

public class TenantEntitlementExtendedPropertiesDTO {
    private int assessmentTypeId;
    private EntitlementFrequencyType frequencyTypeId;
    private String frequencyType;
    public String subscriptionLength;

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public EntitlementFrequencyType getFrequencyTypeId() {
        return frequencyTypeId;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public String getSubscriptionLength() {
        return subscriptionLength;
    }
}
