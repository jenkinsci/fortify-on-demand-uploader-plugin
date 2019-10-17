package org.jenkinsci.plugins.fodupload.models;

import hudson.util.Secret;

public class AuthenticationModel {
    private boolean overrideGlobalConfig;
    private String username;
    private Secret personalAccessToken;
    private String tenantId;

    public AuthenticationModel(boolean overrideGlobalConfig,
                               String username,
                               Secret personalAccessToken,
                               String tenantId) {
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.tenantId = tenantId;
    }

    public boolean getOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    public String getUsername() {
        return username;
    }

    public Secret getPersonalAccessToken() {
        return personalAccessToken;
    }

    public String getTenantId() {
        return tenantId;
    }
}
