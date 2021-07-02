package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.response.*;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;

public class ApplicationsController extends ControllerBase {
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

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        String url = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/applications")
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        Type t = new TypeToken<GenericListResponse<ApplicationApiResponse>>() {
        }.getType();
        GenericListResponse<ApplicationApiResponse> results = gson.fromJson(content, t);
        return results.getItems();
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

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/applications/" + releaseListApplicationId + "/releases");

        if (microserviceId > 0) {
            urlBuilder = urlBuilder.addQueryParameter("filters", "microserviceId:" + microserviceId);
        }

        String url = urlBuilder
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        Type t = new TypeToken<GenericListResponse<ReleaseApiResponse>>() {
        }.getType();
        GenericListResponse<ReleaseApiResponse> results = gson.fromJson(content, t);
        return results.getItems();
    }

    /**
     * GET given enum
     *
     * @param microserviceListApplicationId ApplicationId for query
     * @return array of enum values and text or null
     * @throws java.io.IOException in some circumstances
     */
    public List<MicroserviceApiResponse> getMicroserviceListByApplication(int microserviceListApplicationId) throws IOException {

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        String url = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/applications/" + microserviceListApplicationId + "/microservices")
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        Type t = new TypeToken<GenericListResponse<MicroserviceApiResponse>>() {
        }.getType();
        GenericListResponse<MicroserviceApiResponse> results = gson.fromJson(content, t);
        return results.getItems();
    }
}
