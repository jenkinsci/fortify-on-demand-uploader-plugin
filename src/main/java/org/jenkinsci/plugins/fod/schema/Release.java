package org.jenkinsci.plugins.fod.schema;

import java.util.Date;

public class Release {
	
	private Long applicationId;
	private Long releaseId;
	private String applicationName;
	private String releaseName;
	private Long status;
	private Integer rating;
	private Integer critical;
	private Integer high;
	private Integer medium;
	private Integer low;
	private Integer scanStatus;
	private Long currentStaticScanId;
	private Long currentDynamicScanId;
	private Long currentMobileScanId;
	private Long dynamicScanStatus;
	private Long staticScanStatus;
	private Long mobileScanStatus;
	private Long staticScanStatusId;
	private Long dynamicScanStatusId;
	private Long mobileScanStatusId;
	private Date staticScanDate;
	private Date dynamicScanDate;
	private Date mobileScanDate;
	private Integer issueCount;
	private Boolean isPassed;
	private String passFailReasonId;
	
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
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getReleaseName() {
		return releaseName;
	}
	public void setReleaseName(String releaseName) {
		this.releaseName = releaseName;
	}
	public Long getStatus() {
		return status;
	}
	public void setStatus(Long status) {
		this.status = status;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
	public Integer getCritical() {
		return critical;
	}
	public void setCritical(Integer critical) {
		this.critical = critical;
	}
	public Integer getHigh() {
		return high;
	}
	public void setHigh(Integer high) {
		this.high = high;
	}
	public Integer getMedium() {
		return medium;
	}
	public void setMedium(Integer medium) {
		this.medium = medium;
	}
	public Integer getLow() {
		return low;
	}
	public void setLow(Integer low) {
		this.low = low;
	}
	public Integer getScanStatus() {
		return scanStatus;
	}
	public void setScanStatus(Integer scanStatus) {
		this.scanStatus = scanStatus;
	}
	public Long getCurrentStaticScanId() {
		return currentStaticScanId;
	}
	public void setCurrentStaticScanId(Long currentStaticScanId) {
		this.currentStaticScanId = currentStaticScanId;
	}
	public Long getCurrentDynamicScanId() {
		return currentDynamicScanId;
	}
	public void setCurrentDynamicScanId(Long currentDynamicScanId) {
		this.currentDynamicScanId = currentDynamicScanId;
	}
	public Long getCurrentMobileScanId() {
		return currentMobileScanId;
	}
	public void setCurrentMobileScanId(Long currentMobileScanId) {
		this.currentMobileScanId = currentMobileScanId;
	}
	public Long getDynamicScanStatus() {
		return dynamicScanStatus;
	}
	public void setDynamicScanStatus(Long dynamicScanStatus) {
		this.dynamicScanStatus = dynamicScanStatus;
	}
	public Long getStaticScanStatus() {
		return staticScanStatus;
	}
	public void setStaticScanStatus(Long staticScanStatus) {
		this.staticScanStatus = staticScanStatus;
	}
	public Long getMobileScanStatus() {
		return mobileScanStatus;
	}
	public void setMobileScanStatus(Long mobileScanStatus) {
		this.mobileScanStatus = mobileScanStatus;
	}
	public Long getStaticScanStatusId() {
		return staticScanStatusId;
	}
	public void setStaticScanStatusId(Long staticScanStatusId) {
		this.staticScanStatusId = staticScanStatusId;
	}
	public Long getDynamicScanStatusId() {
		return dynamicScanStatusId;
	}
	public void setDynamicScanStatusId(Long dynamicScanStatusId) {
		this.dynamicScanStatusId = dynamicScanStatusId;
	}
	public Long getMobileScanStatusId() {
		return mobileScanStatusId;
	}
	public void setMobileScanStatusId(Long mobileScanStatusId) {
		this.mobileScanStatusId = mobileScanStatusId;
	}
	public Date getStaticScanDate() {
		return staticScanDate;
	}
	public void setStaticScanDate(Date staticScanDate) {
		this.staticScanDate = staticScanDate;
	}
	public Date getDynamicScanDate() {
		return dynamicScanDate;
	}
	public void setDynamicScanDate(Date dynamicScanDate) {
		this.dynamicScanDate = dynamicScanDate;
	}
	public Date getMobileScanDate() {
		return mobileScanDate;
	}
	public void setMobileScanDate(Date mobileScanDate) {
		this.mobileScanDate = mobileScanDate;
	}
	public Integer getIssueCount() {
		return issueCount;
	}
	public void setIssueCount(Integer issueCount) {
		this.issueCount = issueCount;
	}
	public Boolean getIsPassed() {
		return isPassed;
	}
	public void setIsPassed(Boolean isPassed) {
		this.isPassed = isPassed;
	}
	public String getPassFailReasonId() {
		return passFailReasonId;
	}
	public void setPassFailReasonId(String passFailReasonId) {
		this.passFailReasonId = passFailReasonId;
	}
	
}
