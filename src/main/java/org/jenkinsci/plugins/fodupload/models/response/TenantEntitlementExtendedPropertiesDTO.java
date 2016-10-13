package org.jenkinsci.plugins.fodupload.models.response;

public class TenantEntitlementExtendedPropertiesDTO {
    private int assessmentTypeId;
    private byte frequencyTypeId;
    private String frequencyType;
    public String subscriptionLength;

    public int getAssessmentTypeId() { return assessmentTypeId; }
    public byte getFrequencyTypeId() { return frequencyTypeId; }
    public String getFrequencyType() { return frequencyType; }
    public String getSubscriptionLength() { return subscriptionLength; }
}
