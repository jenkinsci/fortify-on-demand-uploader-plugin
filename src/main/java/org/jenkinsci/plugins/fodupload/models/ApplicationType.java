package org.jenkinsci.plugins.fodupload.models;

public enum ApplicationType implements IFodEnum {

    WEB_THICK_CLIENT(1),
    MOBILE(2);

    private final int type;

    private ApplicationType(int type) {
        this.type = type;
    }

    public static ApplicationType fromInteger(Integer type) {
        switch (type) {
            case 1:
                return ApplicationType.WEB_THICK_CLIENT;
            case 2:
                return ApplicationType.MOBILE;
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
