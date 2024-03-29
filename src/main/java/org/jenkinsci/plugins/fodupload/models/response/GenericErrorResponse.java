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

        int index = 1;
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
