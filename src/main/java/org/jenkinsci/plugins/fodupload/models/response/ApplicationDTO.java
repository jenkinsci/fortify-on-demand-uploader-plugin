package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ApplicationDTO {

    private int applicationId;
    private String applicationName;
    private String applicationDescription;

    public int getApplicationId() {
        return applicationId;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getApplicationName() {
        return applicationName;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getApplicationDescription() {
        return applicationDescription;
    }
}
