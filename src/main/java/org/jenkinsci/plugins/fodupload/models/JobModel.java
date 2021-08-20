package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.BsiTokenParser;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobModel {

    private static final BsiTokenParser tokenParser = new BsiTokenParser();

    private String releaseId;
    private String bsiTokenOriginal;
    private transient BsiToken bsiTokenCache;
    private boolean purchaseEntitlements;
    private String entitlementPreference;
    private String srcLocation;
    private String remediationScanPreferenceType;
    private String inProgressScanActionType;
    private String inProgressBuildResultType;
    private String selectedReleaseType;
    private String userSelectedApplication;
    private String userSelectedMicroservice;
    private String userSelectedRelease;
    private String selectedScanCentralBuildType;
    private boolean scanCentralIncludeTests;
    private boolean scanCentralSkipBuild;
    private String scanCentralBuildCommand;
    private String scanCentralBuildFile;
    private String scanCentralBuildToolVersion;
    private String scanCentralVirtualEnv;
    private String scanCentralRequirementFile;

    private File payload;

    /**
     * Build model used to pass values around
     *
     * @param releaseId                     Release ID
     * @param bsiToken                      BSI Token
     * @param purchaseEntitlements          purchaseEntitlements
     * @param entitlementPreference         entitlementPreference
     * @param srcLocation                   srcLocation
     * @param remediationScanPreferenceType remediationScanPreferenceType
     * @param inProgressScanActionType      inProgressScanActionType
     * @param inProgressBuildResultType     inProgressBuildResultType
     * @param selectedReleaseType           selectedReleaseType
     * @param selectedScanCentralBuildType  selectedScanCentralBuildType
     * @param scanCentralIncludeTests       Scan Central IncludeTests option for Maven and Gradle
     * @param scanCentralSkipBuild			Desc
     * @param scanCentralBuildCommand		Desc
     * @param scanCentralBuildFile			Desc
     * @param scanCentralBuildToolVersion	Desc
     * @param scanCentralVirtualEnv			Desc
     * @param scanCentralRequirementFile	Desc
     */
    public JobModel(String releaseId,
                    String bsiToken,
                    boolean purchaseEntitlements,
                    String entitlementPreference,
                    String srcLocation,
                    String remediationScanPreferenceType,
                    String inProgressScanActionType,
                    String inProgressBuildResultType,
                    String selectedReleaseType,
                    String userSelectedApplication,
                    String userSelectedMicroservice,
                    String userSelectedRelease,
                    String selectedScanCentralBuildType,
                    boolean scanCentralIncludeTests,
                    boolean scanCentralSkipBuild,
                    String scanCentralBuildCommand,
                    String scanCentralBuildFile,
                    String scanCentralBuildToolVersion,
                    String scanCentralVirtualEnv,
                    String scanCentralRequirementFile) {

        this.releaseId = releaseId;
        this.bsiTokenOriginal = bsiToken;
        this.entitlementPreference = entitlementPreference;
        this.purchaseEntitlements = purchaseEntitlements;
        this.srcLocation = srcLocation;
        this.remediationScanPreferenceType = remediationScanPreferenceType;
        this.inProgressScanActionType = inProgressScanActionType;
        this.inProgressBuildResultType = inProgressBuildResultType;
        this.selectedReleaseType = selectedReleaseType;
        this.userSelectedApplication = userSelectedApplication;
        this.userSelectedMicroservice = userSelectedMicroservice;
        this.userSelectedRelease = userSelectedRelease;
        this.selectedScanCentralBuildType = selectedScanCentralBuildType;
        this.scanCentralIncludeTests = scanCentralIncludeTests;
        this.scanCentralSkipBuild = scanCentralSkipBuild;
        this.scanCentralBuildCommand = scanCentralBuildCommand;
        this.scanCentralBuildFile = scanCentralBuildFile;
        this.scanCentralBuildToolVersion = scanCentralBuildToolVersion;
        this.scanCentralVirtualEnv = scanCentralVirtualEnv;
        this.scanCentralRequirementFile = scanCentralRequirementFile;
    }

    public File getPayload() {
        return payload;
    }

    public void setPayload(File payload) {
        this.payload = payload;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public BsiToken getBsiToken() {
        return bsiTokenCache;
    }

    public boolean isPurchaseEntitlements() {
        return purchaseEntitlements;
    }

    public String getEntitlementPreference() {
        return entitlementPreference;
    }

    public String getBsiTokenOriginal() {
        return bsiTokenOriginal;
    }

    public String getSrcLocation() {
        return srcLocation;
    }

    public String getRemediationScanPreferenceType() {
        return remediationScanPreferenceType;
    }

    public String getInProgressScanActionType() {
        return inProgressScanActionType;
    }

    public String getInProgressBuildResultType() {
        return inProgressBuildResultType;
    }

    public String getSelectedReleaseType() {
        return selectedReleaseType;
    }

    public String getUserSelectedApplication() {
        return userSelectedApplication;
    }

    public String getUserSelectedMicroservice() {
        return userSelectedMicroservice;
    }

    public String getUserSelectedRelease() {
        return userSelectedRelease;
    }

    public String getSelectedScanCentralBuildType() { return selectedScanCentralBuildType; }

    public boolean getScanCentralIncludeTests() { return scanCentralIncludeTests; }

    public boolean getScanCentralSkipBuild() { return scanCentralSkipBuild; }

    public String getScanCentralBuildCommand() { return scanCentralBuildCommand; }

    public String getScanCentralBuildFile() { return scanCentralBuildFile; }

    public String getScanCentralBuildToolVersion() { return scanCentralBuildToolVersion; }

    public String getScanCentralVirtualEnv() { return scanCentralVirtualEnv; }

    public String getScanCentralRequirementFile() { return scanCentralRequirementFile; }

    // ToDo: This is outdated
    @Override
    public String toString() {
        if (bsiTokenCache != null) {
            return String.format(
                    "Release Id:                        %s%n" +
                            "Assessment Type Id:                %s%n" +
                            "Technology Stack:                  %s%n" +
                            "Language Level:                    %s%n" +
                            "Purchase Entitlements:             %s%n" +
                            "Entitlement Preference:            %s%n" +
                            "In Progress Scan Action:           %s%n" +
                            "In Progress Build Action:          %s%n" +
                            "Selected Release Type:             %s%n",
                    bsiTokenCache.getProjectVersionId(),
                    bsiTokenCache.getAssessmentTypeId(),
                    bsiTokenCache.getTechnologyStack(),
                    bsiTokenCache.getLanguageLevel(),
                    purchaseEntitlements,
                    entitlementPreference,
                    inProgressScanActionType,
                    inProgressBuildResultType,
                    selectedReleaseType);
        } else {
            return String.format("Release Id: %s", releaseId);
        }
    }

    public boolean loadBsiToken() {
        if (this.bsiTokenCache != null) {
            return true;
        }

        try {
            this.bsiTokenCache = tokenParser.parseBsiToken(bsiTokenOriginal);
        } catch (Exception ex) {
            return false;
        }
        return (this.bsiTokenCache != null);
    }

    // TODO: More validation, though this should never happen with the new format
    public boolean validate(PrintStream logger) {

        List<String> errors = new ArrayList<>();

        Integer releaseIdNum = 0;
        if (releaseId != null && !releaseId.isEmpty()) {
            try {
                releaseIdNum = Integer.parseInt(releaseId);
            } catch (NumberFormatException ex) {
                errors.add("Release Id");
                logger.println(errors.toString());
            }
        }

        if (releaseIdNum == 0) {
            if (bsiTokenCache.getAssessmentTypeId() == 0)
                errors.add("Assessment Type");

            if (bsiTokenCache.getTechnologyType() == null)
                errors.add("Technology Stack");

            if (bsiTokenCache.getProjectVersionId() == 0)
                errors.add("BSI Token Release Id");
        }

        if (errors.size() > 0) {
            logger.println("Missing the following fields from BSI Token: ");
            for (String error : errors) {
                logger.println("    " + error);
            }
            return false;
        }
        return true;
    }
}
