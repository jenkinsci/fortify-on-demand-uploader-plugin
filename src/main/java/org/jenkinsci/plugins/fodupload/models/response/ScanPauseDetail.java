package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class ScanPauseDetail {
    private String pausedOn = "";
    private String reason = "";
    private String notes = "";

    public String getPausedOn() {
        return pausedOn;
    }

    public String getReason() {
        return reason != null ? reason : "";
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }
}
