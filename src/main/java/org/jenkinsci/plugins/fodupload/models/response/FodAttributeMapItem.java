package org.jenkinsci.plugins.fodupload.models.response;

import org.jenkinsci.plugins.fodupload.models.AttributeDefinition;

public class FodAttributeMapItem {
    private String key;
    private String value;
    private AttributeDefinition definition;

    public FodAttributeMapItem(String key, String value, AttributeDefinition definition) {
        this.key = key;
        this.value = value;
        this.definition = definition;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public AttributeDefinition getDefinition() {
        return definition;
    }
}
