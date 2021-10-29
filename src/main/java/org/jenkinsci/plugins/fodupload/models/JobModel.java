package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.BsiTokenParser;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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
    private boolean scanCentralSkipBuild;
    private String scanCentralBuildCommand;
    private String scanCentralBuildFile;
    private String scanCentralBuildToolVersion;
    private String scanCentralVirtualEnv;
    private String scanCentralRequirementFile;

    private Boolean isPipeline;
    private String assessmentType;
    private String entitlementId;
    private String frequencyId;
    private String auditPreference;
    private String technologyStack;
    private String languageLevel;
    private String openSourceScan;

    private Boolean autoProvision;
    private String applicationName;
    private String applicationType;
    private String releaseName;
    private Integer owner;
    private String attributes;
    private String businessCriticality;
    private String sdlcStatus;
    private String microserviceName;
    private Boolean isMicroservice;

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
     * @param scanCentralSkipBuild          Desc
     * @param scanCentralBuildCommand       Desc
     * @param scanCentralBuildFile          Desc
     * @param scanCentralBuildToolVersion   Desc
     * @param scanCentralVirtualEnv         Desc
     * @param scanCentralRequirementFile    Desc
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
                    boolean scanCentralSkipBuild,
                    String scanCentralBuildCommand,
                    String scanCentralBuildFile,
                    String scanCentralBuildToolVersion,
                    String scanCentralVirtualEnv,
                    String scanCentralRequirementFile,
                    Boolean isPipeline,
                    String assessmentType,
                    String entitlementId,
                    String frequencyId,
                    String auditPreference,
                    String technologyStack,
                    String languageLevel,
                    String openSourceScan,
                    Boolean autoProvision,
                    String applicationName,
                    String applicationType,
                    String releaseName,
                    Integer owner,
                    String attributes,
                    String businessCriticality,
                    String sdlcStatus,
                    String microserviceName,
                    Boolean isMicroservice) {

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
        this.scanCentralSkipBuild = scanCentralSkipBuild;
        this.scanCentralBuildCommand = scanCentralBuildCommand;
        this.scanCentralBuildFile = scanCentralBuildFile;
        this.scanCentralBuildToolVersion = scanCentralBuildToolVersion;
        this.scanCentralVirtualEnv = scanCentralVirtualEnv;
        this.scanCentralRequirementFile = scanCentralRequirementFile;
        this.isPipeline = isPipeline;
        this.assessmentType = assessmentType;
        this.entitlementId = entitlementId;
        this.frequencyId = frequencyId;
        this.auditPreference = auditPreference;
        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
        this.openSourceScan = openSourceScan;
        this.autoProvision = autoProvision;
        this.applicationName = applicationName;
        this.applicationType = applicationType;
        this.releaseName = releaseName;
        this.owner = owner;
        this.attributes = attributes;
        this.businessCriticality = businessCriticality;
        this.sdlcStatus = sdlcStatus;
        this.microserviceName = microserviceName;
        this.isMicroservice = isMicroservice;
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

    public String getSelectedScanCentralBuildType() {
        return selectedScanCentralBuildType == null ? FodEnums.SelectedScanCentralBuildType.None.toString() : selectedScanCentralBuildType;
    }

    public boolean getScanCentralSkipBuild() {
        return scanCentralSkipBuild;
    }

    public String getScanCentralBuildCommand() {
        return scanCentralBuildCommand;
    }

    public String getScanCentralBuildFile() {
        return scanCentralBuildFile;
    }

    public String getScanCentralBuildToolVersion() {
        return scanCentralBuildToolVersion;
    }

    public String getScanCentralVirtualEnv() {
        return scanCentralVirtualEnv;
    }

    public String getScanCentralRequirementFile() {
        return scanCentralRequirementFile;
    }
    public Boolean getIsPipeline() {
        return isPipeline;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public String getEntitlementId() {
        return entitlementId;
    }

    public String getFrequencyId() {
        return frequencyId;
    }

    public String getAuditPreference() {
        return auditPreference;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }

    public String getLanguageLevel() {
        return languageLevel;
    }

    public String getOpenSourceScan() {
        return openSourceScan;
    }

    public Boolean getAutoProvision() { return autoProvision; }

    public String getApplicationName() { return applicationName; }

    public String getApplicationType() { return applicationType; }

    public String getReleaseName() { return releaseName; }

    public Integer getOwner() { return owner; }

    public String getAttributes() { return attributes; }

    public String getBusinessCriticality() { return businessCriticality; }

    public String getSdlcStatus() { return sdlcStatus; }

    public String getMicroserviceName() { return microserviceName; }

    public Boolean getIsMicroservice() { return isMicroservice; }


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
                    bsiTokenCache.getReleaseId(),
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

        this.bsiTokenCache = tokenParser.tryParseBsiToken(bsiTokenOriginal);

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

            if (bsiTokenCache.getReleaseId() == 0)
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
