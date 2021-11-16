package org.jenkinsci.plugins.fodupload.models.response;

public class CurrentUserSessionResponse {

    private Integer userId;
    private String username;
    private String[] scopes;
    private String[] permissions;

    public CurrentUserSessionResponse(Integer userId, String username, String[] scopes, String[] permissions) {
        this.userId = userId;
        this.username = username;
        this.scopes = scopes;
        this.permissions = permissions;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String[] getScopes() {
        return scopes;
    }

    public String[] getPermissions() {
        return permissions;
    }
}
