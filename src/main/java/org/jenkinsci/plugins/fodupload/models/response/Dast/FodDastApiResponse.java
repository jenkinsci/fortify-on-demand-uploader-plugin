package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.List;
@SuppressFBWarnings("NM_FIELD_NAMING_CONVENTION")
public class FodDastApiResponse implements Serializable {
    public List<error> errors;
    public boolean isSuccess;
    public int HttpCode;

    public String reason;

}
