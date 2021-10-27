package org.jenkinsci.plugins.fodupload.models.response;

public class GetStaticScanSetupResponse {
    private int assessmentTypeId;
    private int entitlementId;
    private int entitlementFrequencyType;
    private int releaseId;
    private String technologyStack;
    private int technologyStackId;
    private int languageLevelId;
    private String languageLevel;
    private boolean performOpenSourceAnalysis;
    private int auditPreferenceType;
    private boolean includeThirdPartyLibraries;
    private boolean useSourceControl;
    private String bsiToken;

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public int getEntitlementFrequencyType() {
        return entitlementFrequencyType;
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

    public int getAuditPreferenceType() {
        return auditPreferenceType;
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
