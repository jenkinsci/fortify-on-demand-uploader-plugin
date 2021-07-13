package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.CreateApplicationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.response.*;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ApplicationsController extends ControllerBase {

    private static final HashMap<String, String> errorCodesMap;
    static {
        errorCodesMap = new HashMap<>();
        errorCodesMap.put("InvalidOwnerId", "Invalid Owner Id");
    }

    /**
     * Constructor
     *
     * @param apiConnection apiConnection connection object with client info
     * @param logger logger object
     * @param correlationId correlation id
     */
    public ApplicationsController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    /**
     * GET given enum
     *
     * @return array of enum values and text or null
     * @throws java.io.IOException in some circumstances
     */
    public List<ApplicationApiResponse> getApplicationList() throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        GenericListResponse<ApplicationApiResponse> response = apiConnection.requestTyped(request, new TypeToken<GenericListResponse<ApplicationApiResponse>>(){}.getType());

        return response.getItems();
    }

    /**
     * GET given enum
     *
     * @param releaseListApplicationId ApplicationId for query
     * @param microserviceId (0 = null)
     * @return list of Releases
     * @throws java.io.IOException in some circumstances
     */
    public List<ReleaseApiResponse> getReleaseListByApplication(int releaseListApplicationId, int microserviceId) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications/" + releaseListApplicationId + "/releases");

        if (microserviceId > 0) {
            urlBuilder = urlBuilder.addQueryParameter("filters", "microserviceId:" + microserviceId);
        }

        String url = urlBuilder
                .build().toString();

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        GenericListResponse<ReleaseApiResponse> response = apiConnection.requestTyped(request, new TypeToken<GenericListResponse<ReleaseApiResponse>>(){}.getType());

        return response.getItems();
    }

    /**
     * GET given enum
     *
     * @param microserviceListApplicationId ApplicationId for query
     * @return array of enum values and text or null
     * @throws java.io.IOException in some circumstances
     */
    public List<MicroserviceApiResponse> getMicroserviceListByApplication(int microserviceListApplicationId) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications/" + microserviceListApplicationId + "/microservices");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        GenericListResponse<MicroserviceApiResponse> response = apiConnection.requestTyped(request, new TypeToken<GenericListResponse<MicroserviceApiResponse>>(){}.getType());

        return response.getItems();
    }

    public CreateApplicationResponse createApplication(CreateApplicationModel applicationModel) throws IOException {
        String requestContent = Json.getInstance().toJson(applicationModel);
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .post(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        Response response = apiConnection.request(request);

        if (response.isSuccessful()) {
            return apiConnection.parseResponse(response, new TypeToken<CreateApplicationResponse>(){}.getType());
        }
        else if (response.code() >= 500) {
            return new CreateApplicationResponse(0, false, Utils.unexpectedServerResponseErrors());
        }
        else {
            GenericErrorResponse genericErrorResponse = apiConnection.parseResponse(response, new TypeToken<GenericErrorResponse>(){}.getType());
            List<String> errors = new ArrayList<>();

            genericErrorResponse.getErrors().forEach(x -> {
                String message = x.getMessage();
                if (errorCodesMap.containsKey(message)) {
                    message = errorCodesMap.get(message);
                }
                errors.add(message);
            });

            return new CreateApplicationResponse(0, false, errors);
        }
    }
}