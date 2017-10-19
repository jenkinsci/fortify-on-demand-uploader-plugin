package org.jenkinsci.plugins.fodupload.models.response;

@SuppressWarnings("unused")
public class ReleaseAssessmentTypeDTO {

    private int assessmentTypeId;
    private String name;
    private String scanType;
    private int scanTypeId;
    private int entitlementId;
    private String frequencyType;
    private int frequencyTypeId;
    private int units;
    private int unitsAvailable;
    private int parentAssessmentTypeId;
    private boolean isBundledAssessment;
    private boolean isRemediation;
    private int remediationScansAvailable;

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public String getName() {
        return name;
    }

    public String getScanType() {
        return scanType;
    }

    public int getScanTypeId() {
        return scanTypeId;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public int getFrequencyTypeId() {
        return frequencyTypeId;
    }

    public int getUnits() {
        return units;
    }

    public int getUnitsAvailable() {
        return unitsAvailable;
    }

    public int getParentAssessmentTypeId() {
        return parentAssessmentTypeId;
    }

    public boolean isBundledAssessment() {
        return isBundledAssessment;
    }

    public boolean isRemediation() {
        return isRemediation;
    }

    public int getRemediationScansAvailable() {
        return remediationScansAvailable;
    }
}
