package org.jenkinsci.plugins.fodupload.models;

public class PicklistValue {
    private int id;
    private String name;


    public PicklistValue(int id, String name) {
        this.id = id;
        this.name = name;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
