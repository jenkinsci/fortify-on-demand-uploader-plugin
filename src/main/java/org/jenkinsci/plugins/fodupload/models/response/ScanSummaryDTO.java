package org.jenkinsci.plugins.fodupload.models.response;

public class ScanSummaryDTO {
    private int applicationId;
    private String applicationName;
    private int releaseId;
    private String releaseName;
    private int scanId;
    private int scanTypeId;
    private String scanType;
    private int assessmentTypeId;
    private String assessmentTypeName;
    private int analysisStatusTypeId;
    private String analysisStatusType;
    private String startedDatetime;
    private String completedDateTime;
    private int totalIssues;
    private int issueCountCritical;
    private int issueCountHigh;
    private int issueCountMedium;
    private int issueCountLow;
    private int starRating;
    private String notes;
    private boolean isFalsePositiveChallenge;
    private boolean isRemediationScan;
    private int entitlementId;
    private int entitlementUnitsConsumed;
    private boolean isSubscriptionEntitlement;
    private ScanPauseDetail[] pauseDetails;
    private String cancelReason;
    private String analysisStatusReasonNotes;
    private int scanMethodTypeId;
    private String scanMethodTypeName;
    private String scanTool;
    private String scanToolVersion;

    public int getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getReleaseId() {
        return releaseId;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public int getScanId() {
        return scanId;
    }

    public int getScanTypeId() {
        return scanTypeId;
    }

    public String getScanType() {
        return scanType;
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public String getAssessmentTypeName() {
        return assessmentTypeName;
    }

    public int getAnalysisStatusTypeId() {
        return analysisStatusTypeId;
    }

    public String getAnalysisStatusType() {
        return analysisStatusType;
    }

    public String getStartedDatetime() {
        return startedDatetime;
    }

    public String getCompletedDateTime() {
        return completedDateTime;
    }

    public int getTotalIssues() {
        return totalIssues;
    }

    public int getIssueCountCritical() {
        return issueCountCritical;
    }

    public int getIssueCountHigh() {
        return issueCountHigh;
    }

    public int getIssueCountMedium() {
        return issueCountMedium;
    }

    public int getIssueCountLow() {
        return issueCountLow;
    }

    public int getStarRating() {
        return starRating;
    }

    public String getNotes() {
        return notes;
    }

    public boolean getIsFalsePositiveChallenge() {
        return isFalsePositiveChallenge;
    }

    public boolean getIsRemediationScan() {
        return isRemediationScan;
    }

    public int getEntitlementId() {
        return entitlementId;
    }

    public int getEntitlementUnitsConsumed() {
        return entitlementUnitsConsumed;
    }

    public boolean getIsSubscriptionEntitlement() {
        return isSubscriptionEntitlement;
    }

    public ScanPauseDetail[] getPauseDetails() {
        ScanPauseDetail[] returnDetails = pauseDetails;
        return returnDetails;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public String getAnalysisStatusReasonNotes() {
        return analysisStatusReasonNotes;
    }

    public int getScanMethodTypeId() {
        return scanMethodTypeId;
    }

    public String getScanMethodTypeName() {
        return scanMethodTypeName;
    }

    public String getScanTool() {
        return scanTool;
    }

    public String getScanToolVersion() {
        return scanToolVersion;
    }
}

