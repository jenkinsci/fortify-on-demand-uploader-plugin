package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

public class MicroserviceApiResponse {
    private int microserviceId;
    private String microserviceName;
    private int releaseId;
    
    public int getMicroserviceId() {
        return microserviceId;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getMicroserviceName() {
        return microserviceName;
    }

    public int getReleaseId() {
        return releaseId;
    }
}