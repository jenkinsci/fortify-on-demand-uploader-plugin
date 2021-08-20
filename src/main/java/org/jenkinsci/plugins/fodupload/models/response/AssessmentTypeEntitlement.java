package org.jenkinsci.plugins.fodupload.models.response;

import java.util.Calendar;

public class AssessmentTypeEntitlement {

    private Integer assessmentTypeId;
    private String name;
    private Integer entitlementId;
    private String frequencyType;
    private Integer units;
    private Integer unitsAvailable;
    private Calendar subscriptionEndDate;
    private Boolean isRemediation;
    private Integer remediationScansAvailable;
    private Boolean isBundledAssessment;
    private Integer parentAssessmentTypeId;
    private String entitlementDescription;

    public AssessmentTypeEntitlement(Integer assessmentTypeId, String name, Integer entitlementId, String frequencyType, Integer units, Integer unitsAvailable, Calendar subscriptionEndDate, Boolean isRemediation, Integer remediationScansAvailable, Boolean isBundledAssessment, Integer parentAssessmentTypeId, String entitlementDescription) {
        this.assessmentTypeId = assessmentTypeId;
        this.name = name;
        this.entitlementId = entitlementId;
        this.frequencyType = frequencyType;
        this.units = units;
        this.unitsAvailable = unitsAvailable;
        this.subscriptionEndDate = subscriptionEndDate;
        this.isRemediation = isRemediation;
        this.remediationScansAvailable = remediationScansAvailable;
        this.isBundledAssessment = isBundledAssessment;
        this.parentAssessmentTypeId = parentAssessmentTypeId;
        this.entitlementDescription = entitlementDescription;
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

    public Calendar getSubscriptionEndDate() {
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
}
