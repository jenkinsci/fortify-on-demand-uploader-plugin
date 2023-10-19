package org.jenkinsci.plugins.fodupload.models.response.Dast;

import java.util.List;

public class FodDastApiResponse {
    public List<error> errors;
    public boolean isSuccess;
    public int HttpCode;

    public String reason;

}
