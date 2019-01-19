package org.jenkinsci.plugins.fodupload.models;

public class AuthenticationModel {
    private boolean overrideGlobalConfig;
    private String username;
    private String personalAccessToken;
    private String tenantId;
    
    public AuthenticationModel( boolean overrideGlobalConfig,
                                String username,
                                String personalAccessToken,
                                String tenantId){
        this.overrideGlobalConfig = overrideGlobalConfig;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.tenantId = tenantId;
    }
    
    public boolean getOverrideGlobalConfig()
    {
        return overrideGlobalConfig;
    }
    
    public String getUsername()
    {
        return username;
    }
    public String getPersonalAccessToken()
    {
        return personalAccessToken;
    }
    
    public String getTenantId()
    {
        return tenantId;
    }
}
