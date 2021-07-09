package org.jenkinsci.plugins.fodupload.models;

public enum SDLCStatusType {

    PRODUCTION(1),
    QA(2),
    DEVELOPMENT(3);

    private final int type;

    private SDLCStatusType(int type) {
        this.type = type;
    }
}
