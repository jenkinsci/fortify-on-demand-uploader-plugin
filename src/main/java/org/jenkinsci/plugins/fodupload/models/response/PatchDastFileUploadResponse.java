package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.models.response.Dast.FodDastApiResponse;

import java.util.List;

@SuppressFBWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD","SE_NO_SERIALVERSIONID"})
public class PatchDastFileUploadResponse extends FodDastApiResponse {
    public int fileId;
    public String[] hosts;
    public String fileName;
}
