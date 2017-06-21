package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.response.TenantEntitlementExtendedPropertiesDTO;

import java.io.File;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.*;

public class JobConfigModel {
    private int applicationId;
    private int releaseId;
    private int assessmentTypeId;
    private String technologyStack;
    private String languageLevel;
    private boolean runOpenSourceAnalysis;
    private boolean isExpressScan;
    private boolean isExpressAudit;
    private int pollingInterval;
    private boolean doPrettyLogOutput;
    private boolean includeAllFiles;
    private boolean excludeThirdParty;
    private boolean isRemediationScan;
    private File uploadFile;
    private int entitlementId;
    private int entitlementFrequencyTypeId;

    public int getApplicationId() {
        return applicationId;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

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
        return languageLevel != null && !languageLevel.isEmpty();
    }

    public boolean getRunOpenSourceAnalysis() {
        return runOpenSourceAnalysis;
    }

    public boolean getIsExpressScan() {
        return isExpressScan;
    }

    public boolean getIsExpressAudit() {
        return isExpressAudit;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public boolean getDoPrettyLogOutput() {
        return doPrettyLogOutput;
    }

    public boolean getIncludeAllFiles() {
        return includeAllFiles;
    }

    public boolean getExcludeThirdParty() {
        return excludeThirdParty;
    }

    public boolean getIsRemediationScan() {
        return isRemediationScan;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public int getEntitlementFrequencyTypeId() {
        return entitlementFrequencyTypeId;
    }


    public File getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(File file) {
        uploadFile = file;
    }

    public JobConfigModel(String applicationId, String releaseId, String assessmentTypeId, String technologyStack,
                          String languageLevel, boolean runOpenSourceAnalysis, boolean isExpressScan, boolean isExpressAudit,
                          int pollingInterval, boolean doPrettyLogOutput, boolean includeAllFiles, boolean excludeThirdParty,
                          boolean isRemediationScan, int entitlementId, int frequencyType) {
        this.applicationId = Utils.tryParseInt(applicationId);
        this.releaseId = Utils.tryParseInt(releaseId);
        this.assessmentTypeId = Utils.tryParseInt(assessmentTypeId);

        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
        this.runOpenSourceAnalysis = runOpenSourceAnalysis;
        this.isExpressScan = isExpressScan;
        this.isExpressAudit = isExpressAudit;
        this.pollingInterval = pollingInterval;
        this.doPrettyLogOutput = doPrettyLogOutput;
        this.includeAllFiles = includeAllFiles;
        this.excludeThirdParty = excludeThirdParty;
        this.isRemediationScan = isRemediationScan;
        this.entitlementId = entitlementId;

        this.entitlementFrequencyTypeId = (frequencyType != 0) ?
                frequencyType :
                EntitlementFrequencyType.SingleScan.getValue();
    }

    @Override
    public String toString() {
        String text = String.format(
                "Application Id:                    %s%n" +
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
                        "Entitlement Id:                    %s%n" +
                        "Entitlement Frequency Type:        %s%n" +
                        "Polling Interval:                  %s%n" +
                        "Pretty Log Output:                 %s%n",
                applicationId, releaseId, assessmentTypeId, technologyStack, languageLevel, runOpenSourceAnalysis,
                isExpressScan, isExpressAudit, includeAllFiles, excludeThirdParty, isRemediationScan, entitlementId,
                entitlementFrequencyTypeId == EntitlementFrequencyType.SingleScan.getValue() ? "Single Scan" : "Subscription", pollingInterval, doPrettyLogOutput);
        return text;
    }
}
