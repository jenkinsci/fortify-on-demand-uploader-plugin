package org.jenkinsci.plugins.fodupload;

import org.jenkinsci.plugins.fodupload.models.ApplicationAttribute;
import org.jenkinsci.plugins.fodupload.models.AttributeDefinition;
import org.jenkinsci.plugins.fodupload.models.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AttributesHelper {

    public static Result<ApplicationAttribute[]> parseAttributes(String attributesInput, List<AttributeDefinition> attributeDefinitions) {
        Result<HashMap<String, String>> userAttributes = parseAttributes(attributesInput);
        if (!userAttributes.getSuccess()) {
            return new Result<>(false, userAttributes.getErrors(), null);
        }

        HashMap<String, AttributeDefinition> attributeDefsMap = getApplicationAttributeDefinitionsMap(attributeDefinitions);
        List<String> errors = new ArrayList<>();

        // checks that all the attributes that were provided by the user have definitions
        for (String userAttributeKey : userAttributes.getValue().keySet()) {
            if (!attributeDefsMap.containsKey(userAttributeKey)) {
                errors.add("Attribute '" + userAttributeKey + "' does not exist");
            }
        }

        // validate that all required attributes are given and that all attributes are given according to their schema
        for (String defAttributeKey : attributeDefsMap.keySet()) {
            AttributeDefinition def = attributeDefsMap.get(defAttributeKey);
            if (def.getRequired() && !userAttributes.getValue().containsKey(defAttributeKey)) {
                String expectedMessage = buildExpectedMessage(def);
                errors.add("Attribute '" + defAttributeKey + "' was not provided. " + expectedMessage);
            }
            else if (userAttributes.getValue().containsKey(defAttributeKey)) {
                String userAttributeValue = userAttributes.getValue().get(defAttributeKey);
                Result<String> formattedValue = formatUserAttribute(userAttributeValue, def);

                if (!formattedValue.getSuccess()) {
                    String expectedMessage = buildExpectedMessage(def);
                    errors.add("Unable to parse attribute '" + defAttributeKey + "'. " + expectedMessage);
                }
                else {
                    userAttributes.getValue().put(defAttributeKey, formattedValue.getValue());
                }
            }
        }

        if (errors.size() > 0) {
            return new Result<>(false, errors, null);
        }

        List<ApplicationAttribute> attributes = new ArrayList<>();

        for (String userAttribute : userAttributes.getValue().keySet()) {
            attributes.add(new ApplicationAttribute(attributeDefsMap.get(userAttribute).getId(), userAttributes.getValue().get(userAttribute)));
        }

        ApplicationAttribute[] attributesArray = new ApplicationAttribute[attributes.size()];
        attributes.toArray(attributesArray);

        return new Result<>(true, null, attributesArray);
    }

    // parses the attributes input (key1:val1,key2:val2,...) into a hash map, returning errors if parsing failed
    private static Result<HashMap<String, String>> parseAttributes(String attributesInput) {
        HashMap<String, String> map = new HashMap<>();
        if (attributesInput == null || attributesInput.trim().equals("")) {
            return new Result<>(true, null, map);
        }

        List<String> errors = new ArrayList<>();
        for (String attributeInput : attributesInput.split(";")) {
            String[] keyValue = attributeInput.split(":");
            if (keyValue.length != 2) {
                errors.add("All attributes must have a key and a value");
                break;
            }
            else {
                String key = keyValue[0].trim();
                if (map.containsKey(key)) {
                    errors.add("Attribute cannot appear more than once");
                    break;
                }
                map.put(key, keyValue[1].trim());
            }
        }

        if (errors.size() > 0) {
            return new Result<>(false, errors, null);
        }

        return new Result<>(true, null, map);
    }

    // creates an hash map (name -> attribute definition) from the list of attribute definitions
    private static HashMap<String, AttributeDefinition> getApplicationAttributeDefinitionsMap(List<AttributeDefinition> attributeDefinitions) {
        HashMap<String, AttributeDefinition> map = new HashMap<>();
        for (AttributeDefinition def : attributeDefinitions) {
            if (def.getAttributeType().equals("Application"))
                map.put(def.getName(), def);
        }
        return map;
    }

    private static SimpleDateFormat dateFormat;
    static {
        dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        dateFormat.setLenient(false);
    }

    private static Result<String> formatUserAttribute(String userAttributeValue, AttributeDefinition attributeDefinition) {
        switch (attributeDefinition.getAttributeDataType()) {
            case "Text":
            case "User": {
                if (userAttributeValue.equals("")) {
                    return new Result<>(false, null, null);
                }

                return new Result<>(true, null, userAttributeValue);
            }
            case "Picklist": {
                if (attributeDefinition.getPicklistValues() != null && !Arrays.stream(attributeDefinition.getPicklistValues()).anyMatch(v -> v.getName().equals(userAttributeValue))) {
                    return new Result<>(false, null, null);
                }

                return new Result<>(true, null, userAttributeValue);
            }
            case "Date": {
                try {
                    dateFormat.parse(userAttributeValue);
                    return new Result<>(true, null, userAttributeValue);
                }
                catch (ParseException _) {
                    return new Result<>(false, null, null);
                }
            }
            case "Boolean": {
                String attributeValueLower = userAttributeValue.toLowerCase();
                if (attributeValueLower.equals("true")) {
                    return new Result<>(true, null, attributeValueLower);
                }
                else if (attributeValueLower.equals("false")) {
                    return new Result<>(true, null, attributeValueLower);
                }

                return new Result<>(false, null, null);
            }
        }

        return new Result<>(true, null, userAttributeValue);
    }

    private static String buildExpectedMessage(AttributeDefinition attributeDefinition) {
        switch (attributeDefinition.getAttributeDataType()) {
            case "Text":
                return "Expecting a text field";
            case "User":
                return "Expecting a user id or a username";
            case "Picklist": {
                List<String> options = Arrays.stream(attributeDefinition.getPicklistValues()).map(v -> v.getName()).collect(Collectors.toList());
                return "Expecting one of ('" + String.join("', '", options) + "')";
            }
            case "Date":
                return "Expecting yyyy/MM/dd formatted date";
            case "Boolean":
                return "Expecting true/false";
        }

        return "";
    }
}
