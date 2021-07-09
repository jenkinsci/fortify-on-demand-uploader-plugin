package org.jenkinsci.plugins.fodupload.models;

public enum ApplicationType {

    WEB_THICK_CLIENT(1),
    MOBILE(2);

    private final int type;

    private ApplicationType(int type) {
        this.type = type;
    }
}
