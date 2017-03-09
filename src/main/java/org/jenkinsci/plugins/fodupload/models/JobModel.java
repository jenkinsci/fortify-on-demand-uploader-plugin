package org.jenkinsci.plugins.fodupload.models;

import java.io.File;

public class JobModel {
    private BsiUrl bsiUrl;
    private boolean runOpenSourceAnalysis;
    private boolean isExpressScan;
    private boolean isExpressAudit;
    private int pollingInterval;
    private File payload;

    public File getPayload() {
        return payload;
    }
    public void setPayload(File payload) {
        this.payload = payload;
    }

    public BsiUrl getBsiUrl() {
        return bsiUrl;
    }

    public boolean isRunOpenSourceAnalysis() {
        return runOpenSourceAnalysis;
    }

    public boolean isExpressScan() {
        return isExpressScan;
    }

    public boolean isExpressAudit() {
        return isExpressAudit;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public boolean isDoPrettyLogOutput() {
        return doPrettyLogOutput;
    }

    public boolean isIncludeAllFiles() {
        return includeAllFiles;
    }

    public boolean isExcludeThirdParty() {
        return excludeThirdParty;
    }

    public boolean isRemediationScan() {
        return isRemediationScan;
    }

    private boolean doPrettyLogOutput;
    private boolean includeAllFiles;
    private boolean excludeThirdParty;
    private boolean isRemediationScan;

    public JobModel(String bsiUrl, boolean runOpenSourceAnalysis, boolean isExpressAudit, boolean isExpressScan,
                    int pollingInterval, boolean includeAllFiles, boolean excludeThirdParty, boolean isRemediationScan,
                    boolean doPrettyLogOutput) {
        this.bsiUrl = new BsiUrl(bsiUrl);
        this.runOpenSourceAnalysis = runOpenSourceAnalysis;
        this.isExpressAudit = isExpressAudit;
        this.isExpressScan = isExpressScan;
        this.pollingInterval = pollingInterval;
        this.includeAllFiles = includeAllFiles;
        this.excludeThirdParty = excludeThirdParty;
        this.isRemediationScan = isRemediationScan;
        this.doPrettyLogOutput = doPrettyLogOutput;
    }

    @Override
    public String toString() {
        String text = String.format(
                        "Release Id:                        %s%n" +
                        "Assessment Type Id:                %s%n" +
                        "Technology Stack:                  %s%n" +
                        "Language Level:                    %s%n" +
                        "Run Open Source Analysis:          %s%n" +
                        "Express Scan:                      %s%n" +
                        "Express Audit:                     %s%n" +
                        "Exclude All Files:                 %s%n" +
                        "Exclude Third Party:               %s%n" +
                        "Remediation Scan:                  %s%n" +
                        "Polling Interval:                  %s%n" +
                        "Pretty Log Output:                 %s%n",
                bsiUrl.getProjectVersionId(),
                bsiUrl.getAssessmentTypeId(),
                bsiUrl.getTechnologyStack(),
                bsiUrl.getLanguageLevel(),
                runOpenSourceAnalysis,
                isExpressScan,
                isExpressAudit,
                includeAllFiles,
                excludeThirdParty,
                isRemediationScan,
                pollingInterval,
                doPrettyLogOutput);
        return text;
    }
}
