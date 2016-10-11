package org.jenkinsci.plugins.fodupload.models;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

import java.io.File;

public class JobConfigModel {
    private int applicationId;
    private int releaseId;
    private int assessmentTypeId;
    private String technologyStack;
    private String languageLevel;
    private boolean runOpenSourceAnalysis;
    private boolean isExpressScan;
    private boolean isExpressAudit;
    private boolean doPollFortify;
    private boolean doPrettyLogOutput;
    private boolean includeAllFiles;
    private boolean includeThirdParty;
    private boolean isRemediationScan;
    private File uploadFile;

    public int getApplicationId() { return applicationId; }
    public int getReleaseId() { return releaseId; }

    public int getAssessmentTypeId() { return assessmentTypeId; }
    public boolean hasAssessmentTypeId() {
        return assessmentTypeId != 0;
    }

    public String getTechnologyStack() { return technologyStack; }
    public boolean hasTechnologyStack() {
        return !technologyStack.isEmpty();
    }
    public String getLanguageLevel() { return languageLevel; }
    public boolean hasLanguageLevel() {
        return !languageLevel.isEmpty();
    }

    public boolean getRunOpenSourceAnalysis() { return runOpenSourceAnalysis; }
    public boolean getIsExpressScan() { return isExpressScan; }
    public boolean getIsExpressAudit() { return isExpressAudit; }
    public boolean getDoPollFortify() { return doPollFortify; }
    public boolean getDoPrettyLogOutput() { return doPrettyLogOutput; }
    public boolean getIncludeAllFiles() { return includeAllFiles; }
    public boolean getIncludeThirdParty() { return includeThirdParty; }
    public boolean getIsRemediationScan() { return isRemediationScan; }


    public File getUploadFile() { return uploadFile; }
    public void setUploadFile(File file) { uploadFile = file; }

    public JobConfigModel(String applicationId, String releaseId, String assessmentTypeId, String technologyStack,
              String languageLevel, boolean runOpenSourceAnalysis, boolean isExpressScan, boolean isExpressAudit,
              boolean doPollFortify, boolean doPrettyLogOutput, boolean includeAllFiles, boolean includeThirdParty,
              boolean isRemediationScan) {
        this.applicationId = Integer.parseInt(applicationId);
        this.releaseId = Integer.parseInt(releaseId);
        this.assessmentTypeId = Integer.parseInt(assessmentTypeId);
        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
        this.runOpenSourceAnalysis = runOpenSourceAnalysis;
        this.isExpressScan = isExpressScan;
        this.isExpressAudit = isExpressAudit;
        this.doPollFortify = doPollFortify;
        this.doPrettyLogOutput = doPrettyLogOutput;
        this.includeAllFiles = includeAllFiles;
        this.includeThirdParty = includeThirdParty;
        this.isRemediationScan = isRemediationScan;
    }
}
