package org.jenkinsci.plugins.fodupload.models.response.Dast;

import java.util.List;

public class FodDastApiResponse {
    private List<String> errors;
    private String messages;
    public boolean setSuccessStatus(boolean status) {
        isSuccess =status;
        return isSuccess;
    }

    private boolean isSuccess;

    public List<String> getErrors() {
        return this.errors;
    }
    public String getMessages() {
        return messages;
    }
}
