package org.jenkinsci.plugins.fodupload.models.response.Dast;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD")
public class GraphQl {
    public GetDastScanSettingResponse.ApiSource sourceType;
    public String sourceUrn;
    public String schemeType;

    public String host;
    public String servicePath;
}
