package org.jenkinsci.plugins.fodupload.models.request;

import java.io.File;

public class UploadRequest {
    private int assessmentTypeId;
    private String languageLevel;
    private String technologyStack;
    private int projectVersionId;
    private boolean isRemediationScan;
    private int scanPreferenceId;
    private int auditPreferenceId;
    private boolean runSonatypeScan;
    private File uploadFile;
    private boolean excludeThirdPartyLibs;


    public int getAssessmentTypeId() { return assessmentTypeId; }
    public boolean hasAssessmentTypeId() {
        return assessmentTypeId != 0;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }
    public boolean hasTechnologyStack() {
        return !technologyStack.isEmpty();
    }

    public String getLanguageLevel() {
        return languageLevel;
    }
    public boolean hasLanguageLevel() {
        return !languageLevel.isEmpty();
    }

    public int getProjectVersionId() {
        return projectVersionId;
    }
    public boolean hasProjectVersionId() {
        return projectVersionId != 0;
    }

    public boolean isRemediationScan() {
        return isRemediationScan;
    }
    public boolean hasExcludeThirdPartyLibs() {
        return excludeThirdPartyLibs;
    }

    public int getScanPreferenceId() {
        return scanPreferenceId;
    }
    public boolean hasScanPreferenceId() {
        return scanPreferenceId != 0;
    }

    public boolean hasRunSonatypeScan() {
        return runSonatypeScan;
    }

    public int getAuditPreferenceId() {
        return auditPreferenceId;
    }
    public boolean hasAuditPreferencesId() {
        return auditPreferenceId != 0;
    }

    public File getUploadFile() { return uploadFile; }

    public void setAssessmentTypeId(int assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    public void setLanguageLevel(String languageLevel) {
        this.languageLevel = languageLevel;
    }

    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }

    public void setProjectVersionId(int projectVersionId) {
        this.projectVersionId = projectVersionId;
    }

    public void setRemediationScan(boolean remediationScan) {
        isRemediationScan = remediationScan;
    }

    public void setScanPreferenceId(int scanPreferenceId) {
        this.scanPreferenceId = scanPreferenceId;
    }

    public void setAuditPreferenceId(int auditPreferenceId) {
        this.auditPreferenceId = auditPreferenceId;
    }

    public void setRunSonatypeScan(boolean runSonatypeScan) {
        this.runSonatypeScan = runSonatypeScan;
    }

    public void setUploadFile(File uploadZip) {
        this.uploadFile = uploadZip;
    }

    public void setExcludeThirdPartyLibs(boolean excludeThirdPartyLibs) {
        this.excludeThirdPartyLibs = excludeThirdPartyLibs;
    }
}
