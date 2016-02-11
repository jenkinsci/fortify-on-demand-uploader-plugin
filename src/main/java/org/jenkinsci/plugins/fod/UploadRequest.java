package org.jenkinsci.plugins.fod;

import java.io.File;

public class UploadRequest {
    private String clientId;
    private String clientSecret;
    private String tenantCode;
    private String username;
    private String password;
    private File uploadZip;
	private String applicationName;
	private String releaseName;
	private Long releaseId;
	private Long assessmentTypeId;
	private String technologyStack;
	private String languageLevel;
	private String proxyHost;
	private String proxyPort;
	private String proxyUser;
	private String proxyPassword;
	private String ntWorkstation;
	private String ntDomain;
	private String pollingInterval;
	private Boolean runSonatypeScan;
	
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public String getTenantCode() {
		return tenantCode;
	}
	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
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
	public String getProxyHost() {
		return proxyHost;
	}
	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}
	public String getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
	}
	public String getProxyUser() {
		return proxyUser;
	}
	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}
	public String getProxyPassword() {
		return proxyPassword;
	}
	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}
	public String getNtWorkstation() {
		return ntWorkstation;
	}
	public void setNtWorkstation(String ntWorkstation) {
		this.ntWorkstation = ntWorkstation;
	}
	public String getNtDomain() {
		return ntDomain;
	}
	public void setNtDomain(String ntDomain) {
		this.ntDomain = ntDomain;
	}
	public String getPollingInterval() {
		return pollingInterval;
	}
	public void setPollingInterval(String pollingInterval) {
		this.pollingInterval = pollingInterval;
	}
	public Boolean getRunSonatypeScan() {
		return runSonatypeScan;
	}
	public void setRunSonatypeScan(Boolean runSonatypeScan) {
		this.runSonatypeScan = runSonatypeScan;
	}

}
