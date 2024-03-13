package org.jenkinsci.plugins.fodupload.models;

public class LoginMacroFileCreationDetails {
    public String primaryUsername;
    public String primaryPassword;

    public void setPrimaryUsername(String primaryUsername) {
        this.primaryUsername = primaryUsername;
    }

    public void setPrimaryPassword(String primaryPassword) {
        this.primaryPassword = primaryPassword;
    }

    public void setSecondaryUsername(String secondaryUsername) {
        this.secondaryUsername = secondaryUsername;
    }

    public void setSecondaryPassword(String secondaryPassword) {
        this.secondaryPassword = secondaryPassword;
    }

    public String secondaryUsername;
    public String secondaryPassword;
}
