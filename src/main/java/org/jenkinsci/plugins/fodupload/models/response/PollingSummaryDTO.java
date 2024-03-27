package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class PollingSummaryDTO {
    private int ScanId;
    private int OpenSourceScanId;
    private int TenantId;
    private int AnalysisStatusId;
    private int OpenSourceStatusId;
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
    private PollingSummaryPauseDetail[] PauseDetails;
    private int ScanType;

    public int getScanType(){return  ScanType;};
    public int getScanId() {
        return ScanId;
    };

    public int getOpenSourceScanId() {
        return OpenSourceScanId;
    };

    public int getTenantId() {
        return TenantId;
    };

    public int getAnalysisStatusId() {
        return AnalysisStatusId;
    };

    public int getOpenSourceStatusId() {
        return OpenSourceStatusId;
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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public PollingSummaryPauseDetail[] getPauseDetails() {
        PollingSummaryPauseDetail[] returnDetails = PauseDetails;
        return returnDetails;
    };
}
