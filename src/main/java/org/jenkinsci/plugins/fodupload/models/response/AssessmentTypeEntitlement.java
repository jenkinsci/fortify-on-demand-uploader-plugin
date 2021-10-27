package org.jenkinsci.plugins.fodupload.models.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    public AssessmentTypeEntitlement(Integer assessmentTypeId, String name, Integer entitlementId, String frequencyType, Integer frequencyTypeId, Integer units,
                                     Integer unitsAvailable, String subscriptionEndDate, Boolean isRemediation, Integer remediationScansAvailable,
                                     Boolean isBundledAssessment, Integer parentAssessmentTypeId, String entitlementDescription)
    {
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
}
