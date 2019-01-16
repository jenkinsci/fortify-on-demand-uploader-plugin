package org.jenkinsci.plugins.fodupload.models;

public class AuthenticationModel {
    private String projectAuthType;
    private String clientId;
    private String clientSecret;
    private String username;
    private String personalAccessToken;
    private String tenantId;
    
    public AuthenticationModel( String projectAuthType,
                                String clientId ,
                                String clientSecret,
                                String username,
                                String personalAccessToken,
                                String tenantId){
        this.projectAuthType = projectAuthType;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.personalAccessToken = personalAccessToken;
        this.tenantId = tenantId;
    }
    
    public String getProjectAuthType()
    {
        return projectAuthType;
    }
    
    public String getClientId()
    {
        return clientId;
    }
    
    public String getClientSecret()
    {
        return clientSecret;
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
