package org.jenkinsci.plugins.fodupload.models;

public enum SDLCStatusType implements IFodEnum {

    PRODUCTION(1),
    QA(2),
    DEVELOPMENT(3);

    private final int type;

    private SDLCStatusType(int type) {
        this.type = type;
    }

    public static SDLCStatusType fromInteger(Integer type) {
        switch (type) {
            case 1:
                return SDLCStatusType.PRODUCTION;
            case 2:
                return SDLCStatusType.QA;
            case 3:
                return SDLCStatusType.DEVELOPMENT;
        }

        return null;
    }

    @Override
    public Integer getIntValue() {
        return type;
    }

    @Override
    public String getStringValue() {
        return this.toString();
    }
}
