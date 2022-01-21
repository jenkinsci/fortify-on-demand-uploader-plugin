package org.jenkinsci.plugins.fodupload.models;

public class CreateReleaseModel {
    private Integer applicationId;
    private String releaseName;
    private Integer microserviceId;
    private SDLCStatusType sdlcStatusType;

    public CreateReleaseModel(Integer applicationId, String releaseName, Integer microserviceId, SDLCStatusType sdlcStatusType) {
        this.applicationId = applicationId;
        this.releaseName = releaseName;
        this.microserviceId = microserviceId;
        this.sdlcStatusType = sdlcStatusType;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public Integer getMicroserviceId() {
        return microserviceId;
    }

    public SDLCStatusType getSdlcStatusType() {
        return sdlcStatusType;
    }
}
