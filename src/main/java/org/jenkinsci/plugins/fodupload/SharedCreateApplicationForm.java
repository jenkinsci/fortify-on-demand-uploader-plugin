package org.jenkinsci.plugins.fodupload;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.AttributesController;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.CreateApplicationResponse;
import org.jenkinsci.plugins.fodupload.models.response.CreateMicroserviceResponse;
import org.jenkinsci.plugins.fodupload.models.response.CreateReleaseResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SharedCreateApplicationForm {

    //<editor-fold desc="Create Application">

    public static Result<CreateApplicationResponse> submitCreateApplication(AuthenticationModel authModel, JSONObject formObject) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        String correlationId = Utils.createCorrelationId();

        System.out.println("Creating an application with FOD API. [CorrelationId = " + correlationId + "]");

        AttributesController attributesController = new AttributesController(apiConnection, null, correlationId);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, correlationId);

        List<AttributeDefinition> attributes;
        try {
            attributes = attributesController.getAttributeDefinitions();
        }
        catch (IOException ex) {
            return new Result<>(false, null, null);
        }

        Result<CreateApplicationModel> model = parseCreateApplicationModelFromObjectAndValidate(formObject, attributes);
        if (!model.getSuccess()) {
            return new Result<>(false, model.getErrors(), null);
        }

        CreateApplicationResponse response = applicationsController.createApplication(model.getValue());
        if (!response.getSuccess()) {
            return new Result<>(false, response.getErrors(), null);
        }

        return new Result<>(true, null, response);
    }

    private static Result<CreateApplicationModel> parseCreateApplicationModelFromObjectAndValidate(JSONObject formObject, List<AttributeDefinition> attributeDefinitions) {
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

        Result<ApplicationAttribute[]> attributes = AttributesHelper.parseAttributes(formObject.getString("applicationAttributes"), attributeDefinitions);
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

    //</editor-fold>

    //<editor-fold desc="Create Microservice">

    public static Result<Integer> submitCreateMicroservice(AuthenticationModel authModel, JSONObject formObject) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        String correlationId = Utils.createCorrelationId();

        System.out.println("Creating a microservice with FOD API. [CorrelationId = " + correlationId + "]");

        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, correlationId);

        Result<CreateMicroserviceModel> model = parseCreateMicroserviceModelFromObjectAndValidate(formObject);
        if (!model.getSuccess()) {
            return new Result<>(false, model.getErrors(), 0);
        }

        CreateMicroserviceResponse response = applicationsController.createMicroservice(model.getValue());
        if (!response.getSuccess()) {
            return new Result<>(false, response.getErrors(), 0);
        }

        return new Result<>(true, null, response.getMicroserviceId());
    }

    private static Result<CreateMicroserviceModel> parseCreateMicroserviceModelFromObjectAndValidate(JSONObject formObject) {
        List<String> errors = new ArrayList<>();

        if (!formObject.containsKey("applicationId") || !(formObject.get("applicationId") instanceof Integer)) {
            errors.add("Application id must be provided to create a microservice");
        }

        if (!formObject.containsKey("microserviceName") || formObject.getString("microserviceName").length() == 0) {
            errors.add("Microservice name cannot be empty");
        }

        if (errors.size() > 0) {
            return new Result<>(false, errors, null);
        }

        CreateMicroserviceModel model = new CreateMicroserviceModel(
                formObject.getInt("applicationId"),
                formObject.getString("microserviceName"));

        return new Result<>(true, null, model);
    }

    //</editor-fold>

    //<editor-fold desc="Create Release">

    public static Result<Integer> submitCreateRelease(AuthenticationModel authModel, JSONObject formObject) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        String correlationId = Utils.createCorrelationId();

        System.out.println("Creating a release with FOD API. [CorrelationId = " + correlationId + "]");

        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, correlationId);

        Result<CreateReleaseModel> model = parseCreateReleaseModelFromObjectAndValidate(formObject);
        if (!model.getSuccess()) {
            return new Result<>(false, model.getErrors(), 0);
        }

        CreateReleaseResponse response = applicationsController.createRelease(model.getValue());
        if (!response.getSuccess()) {
            return new Result<>(false, response.getErrors(), 0);
        }

        return new Result<>(true, null, response.getReleaseId());
    }

    private static Result<CreateReleaseModel> parseCreateReleaseModelFromObjectAndValidate(JSONObject formObject) {
        List<String> errors = new ArrayList<>();

        if (!formObject.containsKey("applicationId") || !(formObject.get("applicationId") instanceof Integer)) {
            errors.add("Application id must be provided to create a release");
        }

        if (!formObject.containsKey("releaseName") || formObject.getString("releaseName").length() == 0) {
            errors.add("Release name cannot be empty");
        }

        if (formObject.containsKey("microserviceId") && !(formObject.get("microserviceId") instanceof JSONNull) && !(formObject.get("microserviceId") instanceof Integer)) {
            errors.add("If Microservice id is provided, it must be a positive integer");
        }

        if (errors.size() > 0) {
            return new Result<>(false, errors, null);
        }

        CreateReleaseModel model = new CreateReleaseModel(
                formObject.getInt("applicationId"),
                formObject.getString("releaseName"),
                (formObject.containsKey("microserviceId") && formObject.get("microserviceId") instanceof Integer) ? formObject.getInt("microserviceId") : null,
                SDLCStatusType.fromInteger(formObject.getInt("sdlcStatus")));

        return new Result<>(true, null, model);
    }

    //</editor-fold>
}
