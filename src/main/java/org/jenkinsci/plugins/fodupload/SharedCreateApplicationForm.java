package org.jenkinsci.plugins.fodupload;

import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.controllers.AttributesController;
import org.jenkinsci.plugins.fodupload.models.AttributeDefinition;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.CreateApplicationModel;
import org.jenkinsci.plugins.fodupload.models.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SharedCreateApplicationForm {

    public static Result<Integer> submitCreateApplication(AuthenticationModel authModel, JSONObject formObject) {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        String correlationId = Utils.createCorrelationId();

        AttributesController attributesController = new AttributesController(apiConnection, null, correlationId);

        List<AttributeDefinition> attributes;
        try {
            attributes = attributesController.getAttributeDefinitions();
        }
        catch (IOException ex) {
            return new Result<>(false, null, 0);
        }

        return null;
    }

    private static Result<CreateApplicationModel> parseModelFromObjectAndValidate(JSONObject formObject, List<AttributeDefinition> attributeDefinitions) {
        List<String> errors = new ArrayList<>();

        if (formObject.containsKey("applicationName") || formObject.getString("applicationName").length() == 0) {
            errors.add("Application name cannot be empty");
        }

        return null;
    }
}
