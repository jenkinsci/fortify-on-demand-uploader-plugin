package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class PollingSummaryPauseDetail {
    private String Reason = "";
    private String Notes = "";

    public String getReason() {
        return Reason != null ? Reason : "";
    }

    public String getNotes() {
        return Notes != null ? Notes : "";
    }
    
}