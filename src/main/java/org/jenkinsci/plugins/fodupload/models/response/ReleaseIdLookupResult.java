package org.jenkinsci.plugins.fodupload.models.response;

public class ReleaseIdLookupResult {
    private Integer releaseId;
    private String applicationName;
    private String releaseName;

    public ReleaseIdLookupResult(Integer releaseId, String applicationName, String releaseName) {
        this.releaseId = releaseId;
        this.applicationName = applicationName;
        this.releaseName = releaseName;
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
}
