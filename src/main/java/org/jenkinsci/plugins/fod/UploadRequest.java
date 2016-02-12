package org.jenkinsci.plugins.fod;

import java.io.File;

public class UploadRequest {
	private File uploadZip;
	private String applicationName;
	private String releaseName;
	private Long releaseId;
	private Long assessmentTypeId;
	private String technologyStack;
	private String languageLevel;
	private Boolean runSonatypeScan;
	private Boolean isExpressScan;
	private Boolean isExpressAudit;
	
	
	public File getUploadZip() {
		return uploadZip;
	}
	public void setUploadZip(File uploadZip) {
		this.uploadZip = uploadZip;
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
	public Long getReleaseId() {
		return releaseId;
	}
	public void setReleaseId(Long releaseId2) {
		this.releaseId = releaseId2;
	}
	public Long getAssessmentTypeId() {
		return assessmentTypeId;
	}
	public void setAssessmentTypeId(Long assessmentTypeId) {
		this.assessmentTypeId = assessmentTypeId;
	}
	public String getTechnologyStack() {
		return technologyStack;
	}
	public void setTechnologyStack(String technologyStack) {
		this.technologyStack = technologyStack;
	}
	public String getLanguageLevel() {
		return languageLevel;
	}
	public void setLanguageLevel(String languageLevel) {
		this.languageLevel = languageLevel;
	}
	public Boolean getRunSonatypeScan() {
		return runSonatypeScan;
	}
	public void setRunSonatypeScan(Boolean runSonatypeScan) {
		this.runSonatypeScan = runSonatypeScan;
	}
	public Boolean getIsExpressScan() {
		return isExpressScan;
	}
	public void setIsExpressScan(Boolean isExpressScan) {
		this.isExpressScan = isExpressScan;
	}
	public Boolean getIsExpressAudit() {
		return isExpressAudit;
	}
	public void setIsExpressAudit(Boolean isExpressAudit) {
		this.isExpressAudit = isExpressAudit;
	}

}
