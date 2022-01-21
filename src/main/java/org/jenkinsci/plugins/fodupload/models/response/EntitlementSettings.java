package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

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

    public List<LookupItemsModel> getAssessmentTypes() {
        return assessmentTypes;
    }

    public Integer getEntitlement() {
        return entitlement;
    }

    public List<LookupItemsModel> getEntitlements() {
        return entitlements;
    }

    public Integer getAuditPreference() {
        return auditPreference;
    }

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
