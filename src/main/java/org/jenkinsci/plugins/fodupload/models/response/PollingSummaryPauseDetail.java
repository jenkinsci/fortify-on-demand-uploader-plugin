package org.jenkinsci.plugins.fodupload.models.response;

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