package org.jenkinsci.plugins.fodupload.models.response.Dast;

import java.io.Serializable;
import java.util.List;

public class FodDastApiResponse implements Serializable {
    public List<error> errors;
    public boolean isSuccess;
    public int HttpCode;

    public String reason;

}
