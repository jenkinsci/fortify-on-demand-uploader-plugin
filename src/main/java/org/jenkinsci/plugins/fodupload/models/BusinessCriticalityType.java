package org.jenkinsci.plugins.fodupload.models;

public enum BusinessCriticalityType {

    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int type;

    private BusinessCriticalityType(int type) {
        this.type = type;
    }

    public static BusinessCriticalityType fromInteger(Integer type) {
        switch (type) {
            case 1:
                return BusinessCriticalityType.HIGH;
            case 2:
                return BusinessCriticalityType.MEDIUM;
            case 3:
                return BusinessCriticalityType.LOW;
        }

        return null;
    }
}
