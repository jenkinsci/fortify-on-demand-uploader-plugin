package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
@SuppressFBWarnings("EI_EXPOSE_REP")
public class PostReleaseWithUpsertApplicationResponseModel {
    private Integer applicationId;
    private Integer releaseId;
    private Boolean success;
    private List<String> errors;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PostReleaseWithUpsertApplicationResponseModel(Integer applicationId, Integer releaseId, Boolean success, List<String> errors) {
        this.applicationId = applicationId;
        this.releaseId = releaseId;
        this.success = success;
        this.errors = errors;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public Boolean getSuccess() {
        return success;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getErrors() {
        if(errors == null) errors = new ArrayList<>();

        return errors;
    }
}
