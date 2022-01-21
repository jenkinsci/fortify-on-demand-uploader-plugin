package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class CreateReleaseResponse {

    private Integer releaseId;
    private Boolean success;
    private List<String> errors;


    public CreateReleaseResponse(Integer releaseId, Boolean success, List<String> errors) {
        this.releaseId = releaseId;
        this.success = success;
        this.errors = errors;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors;
    }
}
