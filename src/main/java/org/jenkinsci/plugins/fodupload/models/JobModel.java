package org.jenkinsci.plugins.fodupload.models;

import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class JobModel {
    private BsiUrl bsiUrl;
    private boolean runOpenSourceAnalysis;
    private boolean isExpressScan;
    private boolean isExpressAudit;
    private boolean includeAllFiles;
    private boolean excludeThirdParty;
    private boolean isRemediationScan;
    private boolean purchaseEntitlements;
    private int entitlementPreference;
    private boolean isBundledAssessment;
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

    public boolean isIncludeAllFiles() {
        return includeAllFiles;
    }

    public boolean isExcludeThirdParty() {
        return excludeThirdParty;
    }

    public boolean isRemediationScan() {
        return isRemediationScan;
    }

    public boolean isPurchaseEntitlements() {
        return purchaseEntitlements;
    }

    public int getEntitlementPreference() {
        return entitlementPreference;
    }

    public boolean isBundledAssessment() {
        return isBundledAssessment;
    }

    /**
     * Build model used to pass values around
     *
     * @param bsiUrl                BSI URL
     * @param runOpenSourceAnalysis runOpenSourceAnalysis
     * @param isExpressAudit        isExpressAudit
     * @param isExpressScan         isExpressScan
     * @param includeAllFiles       includeAllFiles
     * @param excludeThirdParty     excludeThirdParty
     * @param isRemediationScan     isRemediationScan
     * @param isBundledAssessment   isBundledAssessment
     * @param purchaseEntitlements  purchaseEntitlements
     * @param entitlementPreference entitlementPreference
     */
    public JobModel(String bsiUrl,
                    boolean runOpenSourceAnalysis,
                    boolean isExpressAudit,
                    boolean isExpressScan,
                    boolean includeAllFiles,
                    boolean excludeThirdParty,
                    boolean isRemediationScan,
                    boolean isBundledAssessment,
                    boolean purchaseEntitlements,
                    int entitlementPreference) throws URISyntaxException {
        this.bsiUrl = new BsiUrl(bsiUrl);
        this.runOpenSourceAnalysis = runOpenSourceAnalysis;
        this.isExpressAudit = isExpressAudit;
        this.isExpressScan = isExpressScan;
        this.includeAllFiles = includeAllFiles;
        this.excludeThirdParty = excludeThirdParty;
        this.isRemediationScan = isRemediationScan;
        this.entitlementPreference = entitlementPreference;
        this.isBundledAssessment = isBundledAssessment;
        this.purchaseEntitlements = purchaseEntitlements;
    }

    @Override
    public String toString() {
        return String.format(
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
                        "Purchase Entitlements:             %s%n" +
                        "Entitlement Preference             %s%n" +
                        "Bundled Assessment:                %s%n",
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
                purchaseEntitlements,
                entitlementPreference,
                isBundledAssessment);
    }

    public boolean validate(PrintStream logger) {
        List<String> errors = new ArrayList<>();

        if (!bsiUrl.hasAssessmentTypeId())
            errors.add("Assessment Type");

        if (!bsiUrl.hasTechnologyStack())
            errors.add("Technology Stack");

        if (!bsiUrl.hasProjectVersionId())
            errors.add("Release Id");

        if (errors.size() > 0) {
            logger.println("Missing the following fields from BSI URL: ");
            for (String error : errors) {
                logger.println("    " + error);
            }
            return false;
        }
        return true;
    }
}
