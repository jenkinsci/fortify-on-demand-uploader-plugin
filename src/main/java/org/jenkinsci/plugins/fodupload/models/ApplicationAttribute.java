package org.jenkinsci.plugins.fodupload.models;

public class ApplicationAttribute {
    private Integer id;
    private String value;

    public ApplicationAttribute(Integer id, String value) {
        this.id = id;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
