package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class CreateReleaseResponse {

    private Integer releaseId;
    private Boolean success;
    private List<String> errors;


    @SuppressFBWarnings("EI_EXPOSE_REP2")
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

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getErrors() {
        return errors;
    }
}
