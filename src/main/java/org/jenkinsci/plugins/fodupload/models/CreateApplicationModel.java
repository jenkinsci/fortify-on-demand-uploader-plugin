package org.jenkinsci.plugins.fodupload.models;

public class CreateApplicationModel {
    private String applicationName;
    private ApplicationType applicationType;
    private String releaseName;
    private Integer ownerId;
    private ApplicationAttribute[] attributes;
    private BusinessCriticalityType businessCriticalityType;
    private SDLCStatusType sdlcStatusType;
    private Boolean hasMicroservices;
    private String microserviceName;

    public CreateApplicationModel(String applicationName, ApplicationType applicationType, String releaseName, Integer ownerId, ApplicationAttribute[] attributes, BusinessCriticalityType businessCriticalityType, SDLCStatusType sdlcStatusType, Boolean hasMicroservices, String microserviceName) {
        this.applicationName = applicationName;
        this.applicationType = applicationType;
        this.releaseName = releaseName;
        this.ownerId = ownerId;
        this.attributes = attributes;
        this.businessCriticalityType = businessCriticalityType;
        this.sdlcStatusType = sdlcStatusType;
        this.hasMicroservices = hasMicroservices;
        this.microserviceName = microserviceName;
    }


    public String getApplicationName() {
        return applicationName;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public ApplicationAttribute[] getAttributes() {
        return attributes;
    }

    public BusinessCriticalityType getBusinessCriticalityType() {
        return businessCriticalityType;
    }

    public SDLCStatusType getSdlcStatusType() {
        return sdlcStatusType;
    }

    public Boolean getHasMicroservices() {
        return hasMicroservices;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }
}
