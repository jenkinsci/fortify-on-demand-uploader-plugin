package org.jenkinsci.plugins.fodupload.models;

public class PutStaticScanSetupModel {
    private int assessmentTypeId;
    private int entitlementId;
    private int entitlementFrequencyType;
    private int technologyStackId;
    private int languageLevelId;
    private boolean performOpenSourceAnalysis;
    private int auditPreferenceType;

    public PutStaticScanSetupModel(int assessmentTypeId, int entitlementId, int entitlementFrequencyType, int technologyStackId, int languageLevelId, boolean performOpenSourceAnalysis, int auditPreferenceType) {
        this.assessmentTypeId = assessmentTypeId;
        this.entitlementId = entitlementId;
        this.entitlementFrequencyType = entitlementFrequencyType;
        this.technologyStackId = technologyStackId;
        this.languageLevelId = languageLevelId;
        this.performOpenSourceAnalysis = performOpenSourceAnalysis;
        this.auditPreferenceType = auditPreferenceType;
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public int getEntitlementFrequencyType() {
        return entitlementFrequencyType;
    }

    public int getTechnologyStackId() {
        return technologyStackId;
    }

    public int getLanguageLevelId() {
        return languageLevelId;
    }

    public boolean isPerformOpenSourceAnalysis() {
        return performOpenSourceAnalysis;
    }

    public int getAuditPreferenceType() {
        return auditPreferenceType;
    }
}
