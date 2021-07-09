package org.jenkinsci.plugins.fodupload.models;

public enum BusinessCriticalityType {

    HIGH(1),
    MEDIUM(2),
    Low(3);

    private final int type;

    private BusinessCriticalityType(int type) {
        this.type = type;
    }
}
