package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class GenericErrorResponse {
    private List<ErrorResponse> errors;

    public List<ErrorResponse> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (ErrorResponse error : errors) {
            int errorNumber = errors.indexOf(error) + 1;
            sb.append(errorNumber);
            sb.append(") ");
            sb.append(error.getMessage());
            sb.append(errorNumber > 1 ? "\n" : "");
        }
        return sb.toString();
    }
}

class ErrorResponse {
    private int errorCode;
    private String message;

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}