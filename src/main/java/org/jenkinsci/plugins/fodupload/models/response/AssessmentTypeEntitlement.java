package org.jenkinsci.plugins.fodupload.models.response;

public class AssessmentTypeEntitlement {

    private Integer assessmentTypeId;
    private String name;
    private Integer entitlementId;
    private String frequencyType;
    private Integer frequencyTypeId;
    private Integer units;
    private Integer unitsAvailable;
    private String subscriptionEndDate;
    private Boolean isRemediation;
    private Integer remediationScansAvailable;
    private Boolean isBundledAssessment;
    private Integer parentAssessmentTypeId;
    private String entitlementDescription;

    public String getAssessmentCategory() {return assessmentCategory;}

    private String assessmentCategory;

    public AssessmentTypeEntitlement(Integer assessmentTypeId, String name, Integer entitlementId, String frequencyType, Integer frequencyTypeId, Integer units,
                                     Integer unitsAvailable, String subscriptionEndDate, Boolean isRemediation, Integer remediationScansAvailable,
                                     Boolean isBundledAssessment, Integer parentAssessmentTypeId, String entitlementDescription, String assessmentCategory) {
        this.assessmentTypeId = assessmentTypeId;
        this.name = name;
        this.entitlementId = entitlementId;
        this.frequencyType = frequencyType;
        this.frequencyTypeId = frequencyTypeId;
        this.units = units;
        this.unitsAvailable = unitsAvailable;
        this.subscriptionEndDate = subscriptionEndDate;
        this.isRemediation = isRemediation;
        this.remediationScansAvailable = remediationScansAvailable;
        this.isBundledAssessment = isBundledAssessment;
        this.parentAssessmentTypeId = parentAssessmentTypeId;
        this.entitlementDescription = entitlementDescription;
        this.assessmentCategory = assessmentCategory;
    }

    public Integer getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public String getName() {
        return name;
    }

    public Integer getEntitlementId() {
        return entitlementId;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public Integer getUnits() {
        return units;
    }

    public Integer getUnitsAvailable() {
        return unitsAvailable;
    }

    public String getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public Boolean getRemediation() {
        return isRemediation;
    }

    public Integer getRemediationScansAvailable() {
        return remediationScansAvailable;
    }

    public Boolean getBundledAssessment() {
        return isBundledAssessment;
    }

    public Integer getParentAssessmentTypeId() {
        return parentAssessmentTypeId;
    }

    public String getEntitlementDescription() {
        return entitlementDescription;
    }

    public Integer getFrequencyTypeId() { return frequencyTypeId; }
}
