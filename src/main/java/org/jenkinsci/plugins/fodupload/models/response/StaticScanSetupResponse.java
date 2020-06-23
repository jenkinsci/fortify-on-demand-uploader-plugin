package org.jenkinsci.plugins.fodupload.models.response;

public class StaticScanSetupResponse {
    private int assessmentTypeId;
    private int entitlementId;
    private int entitlementFrequencyTypeId;
    private int releaseId;
    private String technologyStack;
    private int technologyStackId;
    private int languageLevelId;
    private String languageLevel;
    private boolean performOpenSourceAnalysis;
    private int auditPreferenceTypeId;
    private boolean includeThirdPartyLibraries;
    private boolean useSourceControl;
    private String bsiToken;

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public int getEntitlementFrequencyTypeId() {
        return entitlementFrequencyTypeId;
    }

    public int getReleaseId() {
        return releaseId;
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

    public int getAuditPreferenceTypeId() {
        return auditPreferenceTypeId;
    }

    public boolean isIncludeThirdPartyLibraries() {
        return includeThirdPartyLibraries;
    }

    public boolean isUseSourceControl() {
        return useSourceControl;
    }

    public String getBsiToken() {
        return bsiToken;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }

    public String getLanguageLevel() {
        return languageLevel;
    }
}
