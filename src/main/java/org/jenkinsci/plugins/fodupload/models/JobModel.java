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
    private boolean includeAllFiles;
    private boolean purchaseEntitlements;
    private int entitlementPreference;
    private boolean isBundledAssessment;
    private boolean isRemediationPreferred;

    // These override options are for supporting the legacy BSI Urls
    // TODO: Remove these in the future when users can no longer generate BSI URLs in FoD
    private boolean runOpenSourceAnalysisOverride;
    private boolean isExpressScanOverride;
    private boolean isExpressAuditOverride;
    private boolean includeThirdPartyOverride;

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

    public boolean isIncludeAllFiles() {
        return includeAllFiles;
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

    public String getBsiTokenOriginal() {
        return bsiTokenOriginal;
    }

    public boolean isRemediationPreferred() {
        return isRemediationPreferred;
    }

    /**
     * Build model used to pass values around
     *
     * @param bsiToken              BSI Token
     * @param includeAllFiles       includeAllFiles
     * @param isBundledAssessment   isBundledAssessment
     * @param purchaseEntitlements  purchaseEntitlements
     * @param entitlementPreference entitlementPreference
     */
    public JobModel(String bsiToken,
                    boolean includeAllFiles,
                    boolean isBundledAssessment,
                    boolean purchaseEntitlements,
                    int entitlementPreference,
                    boolean isRemediationPreferred,
                    boolean runOpenSourceAnalysisOverride,
                    boolean isExpressScanOverride,
                    boolean isExpressAuditOverride,
                    boolean includeThirdPartyOverride) throws URISyntaxException, UnsupportedEncodingException {

        this.bsiTokenOriginal = bsiToken;
        this.bsiToken = tokenParser.parse(bsiToken);
        this.includeAllFiles = includeAllFiles;
        this.entitlementPreference = entitlementPreference;
        this.isBundledAssessment = isBundledAssessment;
        this.purchaseEntitlements = purchaseEntitlements;
        this.isRemediationPreferred = isRemediationPreferred;

        this.runOpenSourceAnalysisOverride = runOpenSourceAnalysisOverride;
        this.isExpressScanOverride = isExpressScanOverride;
        this.isExpressAuditOverride = isExpressAuditOverride;
        this.includeThirdPartyOverride = includeThirdPartyOverride;
    }

    @Override
    public String toString() {
        return String.format(
                "Release Id:                        %s%n" +
                        "Assessment Type Id:                %s%n" +
                        "Technology Stack:                  %s%n" +
                        "Language Level:                    %s%n" +
                        "Include All Files:                 %s%n" +
                        "Purchase Entitlements:             %s%n" +
                        "Entitlement Preference             %s%n" +
                        "Bundled Assessment:                %s%n",
                bsiToken.getProjectVersionId(),
                bsiToken.getAssessmentTypeId(),
                bsiToken.getTechnologyStack(),
                bsiToken.getLanguageLevel(),
                includeAllFiles,
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

    public boolean isIncludeThirdPartyOverride() {
        return includeThirdPartyOverride;
    }

    public boolean isExpressAuditOverride() {
        return isExpressAuditOverride;
    }

    public boolean isExpressScanOverride() {
        return isExpressScanOverride;
    }

    public boolean isRunOpenSourceAnalysisOverride() {
        return runOpenSourceAnalysisOverride;
    }
}
