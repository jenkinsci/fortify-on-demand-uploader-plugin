package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings({"EI_EXPOSE_REP","NP_UNWRITTEN_FIELD"})
public class GenericErrorResponse {
    private List<ErrorResponse> errors;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<ErrorResponse> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        int index = 1;
        if (errors != null) {
            for (ErrorResponse error : errors) {

                sb.append(index);
                sb.append(") ");
                sb.append(error.getMessage());
                if (index < errors.size())
                    sb.append("\n");
            }
        }
        return sb.toString();
    }

}
