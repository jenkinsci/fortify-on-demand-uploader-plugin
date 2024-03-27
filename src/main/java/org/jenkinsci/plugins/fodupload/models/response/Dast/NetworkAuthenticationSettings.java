package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({"UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD", "EI_EXPOSE_REP"})
public class NetworkAuthenticationSettings {
    public String networkAuthenticationType;
    public String userName;
    public String password;
}
