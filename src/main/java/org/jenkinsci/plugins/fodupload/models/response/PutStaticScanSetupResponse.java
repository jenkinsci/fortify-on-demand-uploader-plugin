package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class PutStaticScanSetupResponse {

    private boolean success;
    private String bsiToken;
    private List<String> errors;
    private List<String> messages;


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

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getMessages() {
        return messages;
    }
}
