package org.jenkinsci.plugins.fodupload.models.response;

public class ReleaseIdLookupResult {
    private Integer releaseId;
    private String applicationName;
    private String releaseName;
    private String microserviceName;

    public ReleaseIdLookupResult(Integer releaseId, String applicationName, String releaseName, String microserviceName) {
        this.releaseId = releaseId;
        this.applicationName = applicationName;
        this.releaseName = releaseName;
        this.microserviceName = microserviceName;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }
}
