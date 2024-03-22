package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class EntitlementSettings {
    private Integer assessmentType;
    private List<LookupItemsModel> assessmentTypes;
    private Integer entitlement;
    private List<LookupItemsModel> entitlements;
    private Integer auditPreference;
    private List<LookupItemsModel> auditPreferences;
    private Integer technologyStack; // real enum
    private Integer languageLevel; // real enum
    private boolean sonatypeScan;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public EntitlementSettings(Integer assessmentType, List<LookupItemsModel> assessmentTypes,
                               Integer entitlement, List<LookupItemsModel> entitlements,
                               Integer auditPreference, List<LookupItemsModel> auditPreferences,
                               Integer technologyStack, Integer languageLevel, boolean sonatypeScan) {
        this.assessmentType = assessmentType;
        this.assessmentTypes = assessmentTypes;
        this.entitlement = entitlement;
        this.entitlements = entitlements;
        this.auditPreference = auditPreference;
        this.auditPreferences = auditPreferences;
        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
        this.sonatypeScan = sonatypeScan;
    }

    public Integer getAssessmentType() {
        return assessmentType;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<LookupItemsModel> getAssessmentTypes() {
        return assessmentTypes;
    }

    public Integer getEntitlement() {
        return entitlement;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<LookupItemsModel> getEntitlements() {
        return entitlements;
    }

    public Integer getAuditPreference() {
        return auditPreference;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<LookupItemsModel> getAuditPreferences() {
        return auditPreferences;
    }

    public Integer getTechnologyStack() {
        return technologyStack;
    }

    public Integer getLanguageLevel() {
        return languageLevel;
    }

    public boolean getSonatypeScan() {
        return sonatypeScan;
    }

}
