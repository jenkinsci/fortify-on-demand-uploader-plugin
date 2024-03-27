package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class PutStaticScanSetupResponse {

    private boolean success;
    private String bsiToken;
    private List<String> errors;
    private List<String> messages;


    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PutStaticScanSetupResponse(boolean success, String bsiToken, List<String> errors, List<String> messages) {
        this.success = success;
        this.bsiToken = bsiToken;
        this.errors = errors;
        this.messages = messages;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getBsiToken() {
        return bsiToken;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getErrors() {
        return errors;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<String> getMessages() {
        return messages;
    }
}
