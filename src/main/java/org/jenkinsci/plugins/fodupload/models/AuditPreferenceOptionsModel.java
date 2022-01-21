package org.jenkinsci.plugins.fodupload.models;

public class AuditPreferenceOptionsModel {
    private boolean automated;
    private boolean manual;

    public AuditPreferenceOptionsModel(boolean automated, boolean manual) {
        this.automated = automated;
        this.manual = manual;
    }

    public boolean isAutomated() {
        return automated;
    }

    public boolean isManual() {
        return manual;
    }
}
