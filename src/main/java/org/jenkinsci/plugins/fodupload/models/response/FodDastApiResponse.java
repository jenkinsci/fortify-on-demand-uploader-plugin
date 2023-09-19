package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class FodDastApiResponse {
    private final List<String> errors;
    private final String messages;

    public boolean isSuccess() {
        return isSuccess;
    }

    private final boolean isSuccess;
    private String ScanId;

    public String getScanId() {
        return ScanId;
    }

    public void setScanId(String scanId) {
        ScanId = scanId;
    }

    static class Error {
        int errorCode;
        String message;
    }

    public FodDastApiResponse(boolean success, List<String> errors, String messages) {
        this.errors = errors;
        this.isSuccess = success;
        this.messages = messages;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public String getMessages() {
        return messages;
    }
}
