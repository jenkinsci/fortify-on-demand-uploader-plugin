package org.jenkinsci.plugins.fodupload.models;

public class ApplicationAttribute {
    private String name;
    private String value;

    public ApplicationAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
