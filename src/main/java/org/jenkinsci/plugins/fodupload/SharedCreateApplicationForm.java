package org.jenkinsci.plugins.fodupload;

import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.AttributesController;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.CreateApplicationResponse;

import java.io.IOException;
import java.util.ArrayList;
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

        if (!formObject.containsKey("ownerId") || formObject.getInt("ownerId") <= 0) {
            errors.add("Owner Id must be a positive integer");
        }

        if (errors.size() > 0) {
            return new Result<>(false, errors, null);
        }

        CreateApplicationModel model = new CreateApplicationModel(
                formObject.getString("applicationName"),
                ApplicationType.fromInteger(formObject.getInt("applicationType")),
                formObject.getString("releaseName"),
                formObject.getInt("ownerId"),
                null,
                BusinessCriticalityType.fromInteger(formObject.getInt("businessCriticality")),
                SDLCStatusType.fromInteger(formObject.getInt("sdlcStatus")),
                formObject.getBoolean("hasMicroservices"),
                new String[]{formObject.getString("microserviceName")},
                formObject.getString("microserviceName"));

        return new Result<>(true, null, model);
    }

    private class AttributeDef {

    }
}
