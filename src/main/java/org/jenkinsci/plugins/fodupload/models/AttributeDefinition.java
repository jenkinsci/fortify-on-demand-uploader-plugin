package org.jenkinsci.plugins.fodupload.models;

public class AttributeDefinition {

    private int id;
    private String name;
    private String attributeType;
    private String attributeDataType;
    private Boolean isRequired;
    private PicklistValue[] picklistValues;

    public AttributeDefinition(int id, String name, String attributeType, String attributeDataType, Boolean isRequired, PicklistValue[] picklistValues) {
        this.id = id;
        this.name = name;
        this.attributeType = attributeType;
        this.attributeDataType = attributeDataType;
        this.isRequired = isRequired;
        this.picklistValues = picklistValues.clone();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public String getAttributeDataType() {
        return attributeDataType;
    }

    public Boolean getRequired() {
        return isRequired;
    }

    public PicklistValue[] getPicklistValues() {
        return picklistValues.clone();
    }

}


