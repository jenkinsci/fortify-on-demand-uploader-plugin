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
    private String[] microservices;
    private String releaseMicroserviceName;

    public CreateApplicationModel(String applicationName, ApplicationType applicationType, String releaseName, Integer ownerId, ApplicationAttribute[] attributes, BusinessCriticalityType businessCriticalityType, SDLCStatusType sdlcStatusType, Boolean hasMicroservices, String[] microservices, String releaseMicroserviceName) {
        this.applicationName = applicationName;
        this.applicationType = applicationType;
        this.releaseName = releaseName;
        this.ownerId = ownerId;
        this.attributes = attributes.clone();
        this.businessCriticalityType = businessCriticalityType;
        this.sdlcStatusType = sdlcStatusType;
        this.hasMicroservices = hasMicroservices;
        this.microservices = microservices.clone();
        this.ownerId = ownerId;
        this.releaseMicroserviceName = releaseMicroserviceName;
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
        return attributes.clone();
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

    public String[] getMicroservices() {
        return microservices.clone();
    }

    public String getReleaseMicroserviceName() {
        return releaseMicroserviceName;
    }
}
