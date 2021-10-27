package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class CreateApplicationResponse {

    private Integer applicationId;
    private Integer releaseId;
    private Integer microserviceId;
    private Boolean success;
    private List<String> errors;


    public CreateApplicationResponse(Integer applicationId, Integer releaseId, Integer microserviceId, Boolean success, List<String> errors) {
        this.applicationId = applicationId;
        this.releaseId = releaseId;
        this.microserviceId = microserviceId;
        this.success = success;
        this.errors = errors;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public Integer getMicroserviceId() {
        return microserviceId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors;
    }
}
