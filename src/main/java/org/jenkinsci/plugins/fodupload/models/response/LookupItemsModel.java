package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class LookupItemsModel {
    private String value;
    private String text;
    private String group;

    public LookupItemsModel(String value, String text, String group) {
        this.value = value;
        this.text = text;
        this.group = group;
    }

    public LookupItemsModel(String value, String text) {
        this.value = value;
        this.text = text;
        this.group = "";
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public String getGroup() {
        return group;
    }
}
