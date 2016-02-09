package org.jenkinsci.plugins.fod.schema;

public class Scan {
    private Long applicationId;
    private Long releaseId;
    private Long scanId;
    private Long scanTypeId; //1=static?
    private Long scanStatusId; //1=COMPLETED
    private Long totalIssues;
    private Integer starRating;
    private String notes;
    private Boolean isFalsePositiveChallenge;
    private Boolean isRemediationScan;
	public Long getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	public Long getReleaseId() {
		return releaseId;
	}
	public void setReleaseId(Long releaseId) {
		this.releaseId = releaseId;
	}
	public Long getScanId() {
		return scanId;
	}
	public void setScanId(Long scanId) {
		this.scanId = scanId;
	}
	public Long getScanTypeId() {
		return scanTypeId;
	}
	public void setScanTypeId(Long scanTypeId) {
		this.scanTypeId = scanTypeId;
	}
	public Long getScanStatusId() {
		return scanStatusId;
	}
	public void setScanStatusId(Long scanStatusId) {
		this.scanStatusId = scanStatusId;
	}
	public Long getTotalIssues() {
		return totalIssues;
	}
	public void setTotalIssues(Long totalIssues) {
		this.totalIssues = totalIssues;
	}
	public Integer getStarRating() {
		return starRating;
	}
	public void setStarRating(Integer starRating) {
		this.starRating = starRating;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public Boolean getIsFalsePositiveChallenge() {
		return isFalsePositiveChallenge;
	}
	public void setIsFalsePositiveChallenge(Boolean isFalsePositiveChallenge) {
		this.isFalsePositiveChallenge = isFalsePositiveChallenge;
	}
	public Boolean getIsRemediationScan() {
		return isRemediationScan;
	}
	public void setIsRemediationScan(Boolean isRemediationScan) {
		this.isRemediationScan = isRemediationScan;
	}
}
