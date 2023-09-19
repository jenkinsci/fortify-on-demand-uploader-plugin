package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class FodDastApiResponse {
    private final boolean success;
    private final List<Error> errors;
    private final List<String> messages;

    static class Error
    {
        int errorCode;
        String message;
    }

    public FodDastApiResponse(boolean success, List<Error> errors, List<String> messages) {
        this.success = success;
        this.errors = errors;
        this.messages = messages;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<Error> getErrors() {
        return this.errors;
    }

    public List<String> getMessages() {
        return messages;
    }
}
