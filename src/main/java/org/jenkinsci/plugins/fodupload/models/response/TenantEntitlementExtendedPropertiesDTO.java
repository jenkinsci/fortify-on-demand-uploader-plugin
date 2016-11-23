package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.*;

public class TenantEntitlementExtendedPropertiesDTO {
    private int assessmentTypeId;
    private EntitlementFrequencyType frequencyTypeId;
    private String frequencyType;
    private String subscriptionLength;

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public EntitlementFrequencyType getFrequencyTypeId() {
        return frequencyTypeId;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getFrequencyType() {
        return frequencyType;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getSubscriptionLength() {
        return subscriptionLength;
    }
}
