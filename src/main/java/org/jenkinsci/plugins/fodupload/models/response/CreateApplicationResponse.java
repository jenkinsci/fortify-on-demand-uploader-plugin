package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class CreateApplicationResponse {

    private Integer applicationId;
    private Boolean success;
    private List<String> errors;


    public CreateApplicationResponse(Integer applicationId, Boolean success, List<String> errors) {
        this.applicationId = applicationId;
        this.success = success;
        this.errors = errors;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors;
    }
}
