package org.jenkinsci.plugins.fodupload.models;

public class BsiToken {
    private int tenantId;
    private String tenantCode;
    private int releaseId;
    private int assessmentTypeId;
    
    private String payloadType;

    private int scanPreferenceId;
    private String scanPreference;

    private int auditPreferenceId;
    private String auditPreference;

    private boolean includeThirdParty;
    private boolean includeOpenSourceAnalysis;

    private String portalUri;
    private String apiUri;

    private int technologyTypeId;
    private String technologyType;

    private int technologyVersionId;
    private String technologyVersion;

    public int getTenantId() { return tenantId; }
    public void setTenantId(int tenantId) { this.tenantId = tenantId; }
    public String getTenantCode() { return tenantCode; }
    public void setTenantCode(String tenantCode) { this.tenantCode = tenantCode; }
    public int getReleaseId() { return releaseId; }
    public void setReleaseId(int releaseId) { this.releaseId = releaseId; }
    public int getAssessmentTypeId() { return assessmentTypeId; }
    public void setAssessmentTypeId(int assessmentTypeId) { this.assessmentTypeId = assessmentTypeId; }

    public String getPayloadType() { return payloadType; }
    public void setPayloadType(String payloadType) { this.payloadType = payloadType; }

    public int getScanPreferenceId() { return scanPreferenceId; }
    public void setScanPreferenceId(int scanPreferenceId) { this.scanPreferenceId = scanPreferenceId; }
    public String getScanPreference() { return scanPreference; }
    public void setScanPreference(String scanPreference) { this.scanPreference = scanPreference; }

    public int getAuditPreferenceId() { return auditPreferenceId; }
    public void setAuditPreferenceId(int auditPreferenceId) { this.auditPreferenceId = auditPreferenceId; }
    public String getAuditPreference() { return auditPreference; }
    public void setAuditPreference(String auditPreference) { this.auditPreference = auditPreference; }

    public boolean getIncludeThirdParty() {return includeThirdParty; }
    public void setIncludeThirdParty(boolean includeThirdParty) { this.includeThirdParty = includeThirdParty; }
    public boolean getIncludeOpenSourceAnalysis() { return includeOpenSourceAnalysis; }
    public void setIncludeOpenSourceAnalysis(boolean includeOpenSourceAnalysis) { this.includeOpenSourceAnalysis = includeOpenSourceAnalysis; }

    public String getPortalUri() { return portalUri; }
    public void setPortalUri(String portalUri) { this.portalUri = portalUri; }
    public String getApiUri() { return apiUri; }
    public void setApiUri(String apiUri) { this.apiUri = apiUri; }

    public int getTechnologyTypeId() { return technologyTypeId; }
    public void setTechnologyTypeId(int technologyTypeId) { this.technologyTypeId = technologyTypeId; }
    public String getTechnologyType() { return technologyType; }
    public void setTechnologyType(String technologyType) { this.technologyType = technologyType; }

    public String getTechnologyStack() { return technologyType; }
    public void setTechnologyStack(String technologyType) { this.technologyType = technologyType; }
    public String getLanguageLevel() { return technologyVersion; }
    public void setLanguageLevel(String technologyVersion) { this.technologyVersion = technologyVersion; }

    public int getTechnologyVersionId() { return technologyVersionId; }
    public void setTechnologyVersionId(int technologyVersionId) { this.technologyVersionId = technologyVersionId; }
    public String getTechnologyVersion() { return technologyVersion; }
    public void setTechnologyVersion(String technologyVersion) { this.technologyVersion = technologyVersion; }
}
