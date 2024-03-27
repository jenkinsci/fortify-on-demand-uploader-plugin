package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD","SE_NO_SERIALVERSIONID"})
public class PostDastStartScanResponse extends FodDastApiResponse {
   public Integer scanId;

}
