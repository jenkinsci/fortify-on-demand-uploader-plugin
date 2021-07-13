package org.jenkinsci.plugins.fodupload;

import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.AttributesController;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.CreateApplicationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SharedCreateApplicationForm {

    public static Result<Integer> submitCreateApplication(AuthenticationModel authModel, JSONObject formObject) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        String correlationId = Utils.createCorrelationId();

        AttributesController attributesController = new AttributesController(apiConnection, null, correlationId);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, correlationId);

        List<AttributeDefinition> attributes;
        try {
            attributes = attributesController.getAttributeDefinitions();
        }
        catch (IOException ex) {
            return new Result<>(false, null, 0);
        }

        Result<CreateApplicationModel> model = parseModelFromObjectAndValidate(formObject, attributes);
        if (!model.getSuccess()) {
            return new Result<>(false, model.getErrors(), 0);
        }

        CreateApplicationResponse response = applicationsController.createApplication(model.getValue());
        if (!response.getSuccess()) {
            return new Result<>(false, response.getErrors(), 0);
        }

        return new Result<>(true, null, response.getApplicationId());
    }

    private static Result<CreateApplicationModel> parseModelFromObjectAndValidate(JSONObject formObject, List<AttributeDefinition> attributeDefinitions) {
        List<String> errors = new ArrayList<>();

        if (!formObject.containsKey("applicationName") || formObject.getString("applicationName").length() == 0) {
            errors.add("Application name cannot be empty");
        }

        if (!formObject.containsKey("releaseName") || formObject.getString("releaseName").length() == 0) {
            errors.add("Release name cannot be empty");
        }

        if (formObject.getBoolean("hasMicroservices") && (!formObject.containsKey("microserviceName") || formObject.getString("microserviceName").length() == 0)) {
            errors.add("Microservice name cannot be empty");
        }

        if (!formObject.containsKey("ownerId") || !(formObject.get("ownerId") instanceof Integer)) {
            errors.add("Owner Id must be a positive integer that represents a user id");
        }

        Result<ApplicationAttribute[]> attributes = parseAttributes(formObject.getString("applicationAttributes"), attributeDefinitions);
        if (!attributes.getSuccess()) {
            errors.addAll(attributes.getErrors());
        }

        if (errors.size() > 0) {
            return new Result<>(false, errors, null);
        }

        CreateApplicationModel model = new CreateApplicationModel(
                formObject.getString("applicationName"),
                ApplicationType.fromInteger(formObject.getInt("applicationType")),
                formObject.getString("releaseName"),
                formObject.getInt("ownerId"),
                attributes.getValue(),
                BusinessCriticalityType.fromInteger(formObject.getInt("businessCriticality")),
                SDLCStatusType.fromInteger(formObject.getInt("sdlcStatus")),
                formObject.getBoolean("hasMicroservices"),
                new String[]{formObject.getString("microserviceName")},
                formObject.getString("microserviceName"));

        return new Result<>(true, null, model);
    }

    private static Result<ApplicationAttribute[]> parseAttributes(String attributesInput, List<AttributeDefinition> attributeDefinitions) {
        Result<HashMap<String, String>> userAttributes = parseAttributes(attributesInput);
        if (!userAttributes.getSuccess()) {
            return new Result<>(false, userAttributes.getErrors(), null);
        }

        HashMap<String, AttributeDefinition> attributeDefsMap = getApplicationAttributeDefinitionsMap(attributeDefinitions);
        List<String> errors = new ArrayList<>();

        for (String userAttributeKey : userAttributes.getValue().keySet()) {
            if (!attributeDefsMap.containsKey(userAttributeKey)) {
                errors.add("Attribute '" + userAttributeKey + "' does not exist");
            }
        }
        for (String defAttributeKey : attributeDefsMap.keySet()) {
            if (attributeDefsMap.get(defAttributeKey).getRequired() && !userAttributes.getValue().containsKey(defAttributeKey)) {
                errors.add("Attribute '" + defAttributeKey + "' was not provided");
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

    private static Result<HashMap<String, String>> parseAttributes(String attributesInput) {
        HashMap<String, String> map = new HashMap<>();
        if (attributesInput == null || attributesInput.trim().equals("")) {
            return new Result<>(true, null, map);
        }

        List<String> errors = new ArrayList<>();
        for (String attributeInput : attributesInput.split(",")) {
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

    private static HashMap<String, AttributeDefinition> getApplicationAttributeDefinitionsMap(List<AttributeDefinition> attributeDefinitions) {
        HashMap<String, AttributeDefinition> map = new HashMap<>();
        for (AttributeDefinition def : attributeDefinitions) {
            if (def.getAttributeType().equals("Application"))
                map.put(def.getName(), def);
        }
        return map;
    }

    private class AttributeDef {

    }
}
