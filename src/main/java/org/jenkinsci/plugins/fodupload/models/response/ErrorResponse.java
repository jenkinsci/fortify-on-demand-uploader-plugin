package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ErrorResponse {
    private int errorCode;
    private String message;

    public int getErrorCode() {
        return errorCode;
    }

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getMessage() {
        return message;
    }
}
