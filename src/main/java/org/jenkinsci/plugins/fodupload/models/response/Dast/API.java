package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD")
public class API {
    public ApiType apiType;
    public OpenApi openAPI;
    public GraphQl graphQL;
    public Grpc gRPC;
    public Postman postman;
}
