package org.jenkinsci.plugins.fodupload.models;

public class CreateMicroserviceModel {

    private Integer applicationId;
    private String microserviceName;

    public CreateMicroserviceModel(Integer applicationId, String microserviceName) {
        this.applicationId = applicationId;
        this.microserviceName = microserviceName;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }
}