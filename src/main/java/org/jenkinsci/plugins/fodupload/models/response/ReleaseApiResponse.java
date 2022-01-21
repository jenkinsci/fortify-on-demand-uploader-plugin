package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ReleaseApiResponse {
    private int releaseId;
    private String releaseName;
    private String releaseDescription;
    private Boolean suspend;
    private String releaseCreatedDate;
    private String microserviceName;
    private int microserviceId;
    private int applicationId;
    private String applicationName;
    private int currentAnalysisStatusTypeId;
    private String currentAnalysisStatusType;
    private int rating;
    private int critical;
    private int high;
    private int medium;
    private int low;
    private int currentStaticScanId;
    private int currentDynamicScanId;
    private int currentMobileScanId;
    private String staticAnalysisStatusType;
    private String dynamicAnalysisStatusType;
    private String mobileAnalysisStatusType;
    private int staticAnalysisStatusTypeId;
    private int dynamicAnalysisStatusTypeId;
    private int mobileAnalysisStatusTypeId;
    private String staticScanDate;
    private String dynamicScanDate;
    private String mobileScanDate;
    private int issueCount;
    private Boolean isPassed;
    private int passFailReasonTypeId;
    private String passFailReasonType;
    private int sldcStatusTypeId;
    private String sldcStatusType;
    private int ownerId;

    public int getReleaseId() {
        return releaseId;
    };
    public String getReleaseName() {
        return releaseName;
    };
    public String getReleaseDescription() {
        return releaseDescription;
    };
    public Boolean getSuspend() {
        return suspend;
    };
    public String getReleaseCreatedDate() {
        return releaseCreatedDate;
    };
    public String getMicroserviceName() {
        return microserviceName;
    };
    public int getMicroserviceId() {
        return microserviceId;
    };
    public int getApplicationId() {
        return applicationId;
    };
    public String getApplicationName() {
        return applicationName;
    };
    public int getCurrentAnalysisStatusTypeId() {
        return currentAnalysisStatusTypeId;
    };
    public String getCurrentAnalysisStatusType() {
        return currentAnalysisStatusType;
    };
    public int getRating() {
        return rating;
    };
    public int getCritical() {
        return critical;
    };
    public int getHigh() {
        return high;
    };
    public int getMedium() {
        return medium;
    };
    public int getLow() {
        return low;
    };
    public int getCurrentStaticScanId() {
        return currentStaticScanId;
    };
    public int getCurrentDynamicScanId() {
        return currentDynamicScanId;
    };
    public int getCurrentMobileScanId() {
        return currentMobileScanId;
    };
    public String getStaticAnalysisStatusType() {
        return staticAnalysisStatusType;
    };
    public String getDynamicAnalysisStatusType() {
        return dynamicAnalysisStatusType;
    };
    public String getMobileAnalysisStatusType() {
        return mobileAnalysisStatusType;
    };
    public int getStaticAnalysisStatusTypeId() {
        return staticAnalysisStatusTypeId;
    };
    public int getDynamicAnalysisStatusTypeId() {
        return dynamicAnalysisStatusTypeId;
    };
    public int getMobileAnalysisStatusTypeId() {
        return mobileAnalysisStatusTypeId;
    };
    public String getStaticScanDate() {
        return staticScanDate;
    };
    public String getDynamicScanDate() {
        return dynamicScanDate;
    };
    public String getMobileScanDate() {
        return mobileScanDate;
    };
    public int getIssueCount() {
        return issueCount;
    };
    public Boolean getIsPassed() {
        return isPassed;
    };
    public int getPassFailReasonTypeId() {
        return passFailReasonTypeId;
    };
    public String getPassFailReasonType() {
        return passFailReasonType;
    };
    public int getSldcStatusTypeId() {
        return sldcStatusTypeId;
    };
    public String getSldcStatusType() {
        return sldcStatusType;
    };
    public int getOwnerId() {
        return ownerId;
    };
}
