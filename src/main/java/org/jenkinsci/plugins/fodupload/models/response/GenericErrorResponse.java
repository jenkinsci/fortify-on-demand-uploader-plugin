package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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

        int index = 0;
        for (ErrorResponse error : errors) {

            sb.append(index);
            sb.append(") ");
            sb.append(error.getMessage());
            if (index < errors.size())
                sb.append("\n");
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

    @SuppressFBWarnings("UWF_UNWRITTEN_FIELD")
    public String getMessage() {
        return message;
    }
}