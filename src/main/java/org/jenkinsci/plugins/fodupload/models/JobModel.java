package org.jenkinsci.plugins.fodupload.models;

import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class JobModel {

    private static final BsiTokenParser tokenParser = new BsiTokenParser();

    private String bsiTokenOriginal;
    private BsiToken bsiToken;
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

    public BsiToken getBsiToken() {
        return bsiToken;
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
     * @param bsiToken              BSI Token
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
    public JobModel(String bsiToken,
                    boolean runOpenSourceAnalysis,
                    boolean isExpressAudit,
                    boolean isExpressScan,
                    boolean includeAllFiles,
                    boolean excludeThirdParty,
                    boolean isRemediationScan,
                    boolean isBundledAssessment,
                    boolean purchaseEntitlements,
                    int entitlementPreference) throws URISyntaxException, UnsupportedEncodingException {

        this.bsiTokenOriginal = bsiToken;
        this.bsiToken = tokenParser.parse(bsiToken);
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
                bsiToken.getProjectVersionId(),
                bsiToken.getAssessmentTypeId(),
                bsiToken.getTechnologyStack(),
                bsiToken.getLanguageLevel(),
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

    // TODO: More validation, though this should never happen with the new format
    public boolean validate(PrintStream logger) {
        List<String> errors = new ArrayList<>();

        if (bsiToken.getAssessmentTypeId() != 0)
            errors.add("Assessment Type");

        if (bsiToken.getTechnologyVersion() != null)
            errors.add("Technology Stack");

        if (bsiToken.getProjectVersionId() != 0)
            errors.add("Release Id");

        if (errors.size() > 0) {
            logger.println("Missing the following fields from BSI Token: ");
            for (String error : errors) {
                logger.println("    " + error);
            }
            return false;
        }
        return true;
    }

    public String getBsiTokenOriginal() {
        return bsiTokenOriginal;
    }
}
