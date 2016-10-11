package org.jenkinsci.plugins.fodupload.models;

import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;

public class JobConfigModel {
    private String applicationId;
    private String releaseId;
    private String assessmentTypeId;
    private String technologyStack;
    private String languageLevel;
    private boolean runOpenSourceAnalysis;
    private boolean isExpressScan;
    private boolean isExpressAudit;
    private boolean doPollFortify;
    private boolean doPrettyLogOutput;
    private boolean includeAllFiles;
    private boolean includeThirdParty;

    public String getApplicationId() { return applicationId; }
    public String getReleaseId() { return releaseId; }
    public String getAssessmentTypeId() { return assessmentTypeId; }
    public String getTechnologyStack() { return technologyStack; }
    public String getLanguageLevel() { return languageLevel; }
    public boolean getRunOpenSourceAnalysis() { return runOpenSourceAnalysis; }
    public boolean getIsExpressScan() { return isExpressScan; }
    public boolean getIsExpressAudit() { return isExpressAudit; }
    public boolean getDoPollFortify() { return doPollFortify; }
    public boolean getDoPrettyLogOutput() { return doPrettyLogOutput; }
    public boolean getIncludeAllFiles() { return includeAllFiles; }
    public boolean getIncludeThirdParty() { return includeThirdParty; }

    public JobConfigModel(String applicationId, String releaseId, String assessmentTypeId, String technologyStack,
              String languageLevel, boolean runOpenSourceAnalysis, boolean isExpressScan, boolean isExpressAudit,
              boolean doPollFortify, boolean doPrettyLogOutput, boolean includeAllFiles, boolean includeThirdParty) {
        this.applicationId = applicationId;
        this.releaseId = releaseId;
        this.assessmentTypeId = assessmentTypeId;
        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
        this.runOpenSourceAnalysis = runOpenSourceAnalysis;
        this.isExpressScan = isExpressScan;
        this.isExpressAudit = isExpressAudit;
        this.doPollFortify = doPollFortify;
        this.doPrettyLogOutput = doPrettyLogOutput;
        this.includeAllFiles = includeAllFiles;
        this.includeThirdParty = includeThirdParty;
    }
}
