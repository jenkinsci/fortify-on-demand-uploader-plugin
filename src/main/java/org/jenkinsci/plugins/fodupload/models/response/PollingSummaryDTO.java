package org.jenkinsci.plugins.fodupload.models.response;

public class PollingSummaryDTO {
    private int ScanId;
    private int TenantId;
    private int AnalysisStatusId;
    private String AnalysisStatusTypeValue;
    private int AnalysisStatusReasonId;
    private String AnalysisStatusReason;
    private String AnalysisStatusReasonNotes;
    private int IssueCountCritical;
    private int IssueCountHigh;
    private int IssueCountMedium;
    private int IssueCountLow;
    private Boolean PassFailStatus;
    private String PassFailReasonType;
    private ScanPauseDetail[] PauseDetails;

    
    public int getScanId() {
        return ScanId;
    };

    public int getTenantId() {
        return TenantId;
    };

    public int getAnalysisStatusId() {
        return AnalysisStatusId;
    };

    public String getAnalysisStatusTypeValue() {
        return AnalysisStatusTypeValue;
    };

    public int getAnalysisStatusReasonId() {
        return AnalysisStatusReasonId;
    };

    public String getAnalysisStatusReason() {
        return AnalysisStatusReason != null ? AnalysisStatusReason : "";
    };

    public String getAnalysisStatusReasonNotes() {
        return AnalysisStatusReasonNotes != null ? AnalysisStatusReasonNotes : "";
    };

    public int getIssueCountCritical() {
        return IssueCountCritical;
    };

    public int getIssueCountHigh() {
        return IssueCountHigh;
    };

    public int getIssueCountMedium() {
        return IssueCountMedium;
    };

    public int getIssueCountLow() {
        return IssueCountLow;
    };

    public Boolean getPassFailStatus() {
        return PassFailStatus;
    };

    public String getPassFailReasonType() {
        return PassFailReasonType;
    };

    public ScanPauseDetail[] getPauseDetails() {
        ScanPauseDetail[] returnDetails = PauseDetails;
        return returnDetails;
    };
}