package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class CreateMicroserviceResponse {

    private Integer microserviceId;
    private Boolean success;
    private List<String> errors;


    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public CreateMicroserviceResponse(Integer microserviceId, Boolean success, List<String> errors) {
        this.microserviceId = microserviceId;
        this.success = success;
        this.errors = errors;
    }

    public Integer getMicroserviceId() {
        return microserviceId;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Boolean getSuccess() {
        return success;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getErrors() {
        return errors;
    }
}
