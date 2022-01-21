package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class CreateMicroserviceResponse {

    private Integer microserviceId;
    private Boolean success;
    private List<String> errors;


    public CreateMicroserviceResponse(Integer microserviceId, Boolean success, List<String> errors) {
        this.microserviceId = microserviceId;
        this.success = success;
        this.errors = errors;
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
