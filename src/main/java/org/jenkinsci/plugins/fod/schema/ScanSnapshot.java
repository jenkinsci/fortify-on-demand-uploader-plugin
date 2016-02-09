package org.jenkinsci.plugins.fod.schema;

import java.util.List;

public class ScanSnapshot {
	private Long projectVersionId;
	private Long releaseId;
	private Long dynamicScanId;
	private Long staticScanId;
	private Long mobileScanId;
	private List<CategoryRollup> categoryRollups;
	private Long historyRollupId;
	
	public Long getProjectVersionId() {
		return projectVersionId;
	}
	public void setProjectVersionId(Long projectVersionId) {
		this.projectVersionId = projectVersionId;
	}
	public Long getDynamicScanId() {
		return dynamicScanId;
	}
	public void setDynamicScanId(Long dynamicScanId) {
		this.dynamicScanId = dynamicScanId;
	}
	public Long getStaticScanId() {
		return staticScanId;
	}
	public void setStaticScanId(Long staticScanId) {
		this.staticScanId = staticScanId;
	}
	public Long getMobileScanId() {
		return mobileScanId;
	}
	public void setMobileScanId(Long mobileScanId) {
		this.mobileScanId = mobileScanId;
	}
	public List<CategoryRollup> getCategoryRollups() {
		return categoryRollups;
	}
	public void setCategoryRollups(List<CategoryRollup> categoryRollups) {
		this.categoryRollups = categoryRollups;
	}
	public Long getHistoryRollupId() {
		return historyRollupId;
	}
	public void setHistoryRollupId(Long historyRollupId) {
		this.historyRollupId = historyRollupId;
	}
	public Long getReleaseId() {
		return releaseId;
	}
	public void setReleaseId(Long releaseId) {
		this.releaseId = releaseId;
	}
}
