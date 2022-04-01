package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.CreateApplicationModel;
import org.jenkinsci.plugins.fodupload.models.CreateMicroserviceModel;
import org.jenkinsci.plugins.fodupload.models.CreateReleaseModel;
import org.jenkinsci.plugins.fodupload.models.Result;
import org.jenkinsci.plugins.fodupload.models.response.*;

import java.io.IOException;
import java.io.PrintStream;
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
     * @param logger        logger object
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
    public GenericListResponse<ApplicationApiResponse> getApplicationList(String searchTerm, Integer offset, Integer limit) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications");

        if (searchTerm != null && !searchTerm.trim().equals("")) {
            urlBuilder = urlBuilder.addQueryParameter("filters", "applicationname:" + searchTerm.trim());
        }
        if (offset != null) {
            urlBuilder = urlBuilder.addQueryParameter("offset", offset.toString());
        }
        if (limit != null) {
            urlBuilder = urlBuilder.addQueryParameter("limit", limit.toString());
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response resp = apiConnection.request(request);

        if (resp.code() < 300) {
            return apiConnection.parseResponse(resp, new TypeToken<GenericListResponse<ApplicationApiResponse>>() {}.getType());
        } else {
            String rawBody = apiConnection.getRawBody(resp);
            String msg = String.format("Failed getApplicationList(%s, %d, %d). %s", searchTerm, offset, limit, !rawBody.isEmpty() ? "Raw API response:\n" + rawBody : "API empty response");

            throw new IOException(msg);
        }
    }

    public Result<ApplicationApiResponse> getApplicationById(Integer applicationId) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications/" + applicationId);
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response res = apiConnection.request(request);
        if (!res.isSuccessful()) {
            return new Result<>(false, new ArrayList<String>(){ { add("HTTP Error " + res.code()); } }, null);
        }

        ApplicationApiResponse application = apiConnection.parseResponse(res, new TypeToken<ApplicationApiResponse>(){}.getType());
        return new Result<>(true, null, application);
    }

    public Result<ReleaseApiResponse> getReleaseById(Integer releaseId) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases/" + releaseId);
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response res = apiConnection.request(request);
        if (!res.isSuccessful()) {
            return new Result<>(false, new ArrayList<String>(){ { add("HTTP Error " + res.code()); } }, null);
        }

        ReleaseApiResponse release = apiConnection.parseResponse(res, new TypeToken<ReleaseApiResponse>(){}.getType());
        return new Result<>(true, null, release);
    }

    /**
     * GET given enum
     *
     * @param releaseListApplicationId ApplicationId for query
     * @param microserviceId (0 = null)
     * @return list of Releases
     * @throws java.io.IOException in some circumstances
     */
    public GenericListResponse<ReleaseApiResponse> getReleaseListByApplication(int releaseListApplicationId, int microserviceId, String searchTerm, Integer offset, Integer limit) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications/" + releaseListApplicationId + "/releases");

        List<String> filters = new ArrayList<>();
        if (microserviceId > 0) {
            filters.add("microserviceId:" + microserviceId);
        }
        if (searchTerm != null && !searchTerm.trim().equals("")) {
            filters.add("releasename:" + searchTerm.trim());
        }

        if (filters.size() > 0) {
            urlBuilder = urlBuilder.addQueryParameter("filters", String.join("+", filters));
        }
        if (offset != null) {
            urlBuilder = urlBuilder.addQueryParameter("offset", offset.toString());
        }
        if (limit != null) {
            urlBuilder = urlBuilder.addQueryParameter("limit", limit.toString());
        }

        if (microserviceId > 0) {
            urlBuilder = urlBuilder.addQueryParameter("filters", "microserviceId:" + microserviceId);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        return apiConnection.requestTyped(request, new TypeToken<GenericListResponse<ReleaseApiResponse>>(){}.getType());
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
        urlBuilder = urlBuilder.addQueryParameter("includeReleases", "false");

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
        else if (response.code() == 401) {
            return new CreateApplicationResponse(0, 0, 0, false, Utils.unauthorizedServerResponseErrors());
        }
        else if (response.code() >= 400) {
            return new CreateApplicationResponse(0, 0, 0, false, Utils.unexpectedServerResponseErrors());
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

            return new CreateApplicationResponse(0, 0, 0, false, errors);
        }
    }

    public CreateMicroserviceResponse createMicroservice(CreateMicroserviceModel microserviceModel) throws IOException {
        String requestContent = Json.getInstance().toJson(microserviceModel);
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/applications/" + microserviceModel.getApplicationId() + "/microservices");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .post(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        Response response = apiConnection.request(request);

        if (response.isSuccessful()) {
            return apiConnection.parseResponse(response, new TypeToken<CreateMicroserviceResponse>(){}.getType());
        }
        else if (response.code() >= 500) {
            return new CreateMicroserviceResponse(0, false, Utils.unexpectedServerResponseErrors());
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

            return new CreateMicroserviceResponse(0, false, errors);
        }
    }

    public CreateReleaseResponse createRelease(CreateReleaseModel releaseModel) throws IOException {
        String requestContent = Json.getInstance().toJson(releaseModel);
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .post(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        Response response = apiConnection.request(request);

        if (response.isSuccessful()) {
            return apiConnection.parseResponse(response, new TypeToken<CreateReleaseResponse>(){}.getType());
        }
        else if (response.code() >= 500) {
            return new CreateReleaseResponse(0, false, Utils.unexpectedServerResponseErrors());
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

            return new CreateReleaseResponse(0, false, errors);
        }
    }
}