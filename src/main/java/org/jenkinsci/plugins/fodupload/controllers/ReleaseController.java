package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.FodApiFilterList;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseAssessmentTypeDTO;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import org.jenkinsci.plugins.fodupload.Utils;

public class ReleaseController extends ControllerBase {

    /**
     * Constructor
     *
     * @param apiConnection apiConnection object with client info
     */
    public ReleaseController(FodApiConnection apiConnection) {
        super(apiConnection);
    }

    /**
     * Get an individual release with given fields
     *
     * @param releaseId release to get
     * @param fields    fields to return
     * @return ReleaseDTO object with given fields
     */
    public ReleaseDTO getRelease(final int releaseId, final String fields) throws IOException {

        // TODO: Remove every method authenticating the connection, leave that to the user
        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        FodApiFilterList filters = new FodApiFilterList().addFilter("releaseId", releaseId);

        // TODO: Investigate why the endpoint for a release wasn't used
        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/releases")
                .addQueryParameter("limit", "1")
                .addQueryParameter("filters", filters.toString());

        if (fields.length() > 0)
            builder = builder.addQueryParameter("fields", fields);

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
            // Re-authenticate
            apiConnection.authenticate();
        }

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        // Create a type of GenericList<ReleaseDTO> to play nice with gson.
        Type t = new TypeToken<GenericListResponse<ReleaseDTO>>() {
        }.getType();
        GenericListResponse<ReleaseDTO> results = gson.fromJson(content, t);
        if(results.getItems().size() > 0)
            return results.getItems().get(0);
        else 
            return null;
    }

    /**
     * Get Assessment Type from bsi url
     *
     * @param model JobModel
     * @return returns assessment type obj
     */
    ReleaseAssessmentTypeDTO getAssessmentType(final JobModel model) throws IOException, URISyntaxException {


        FodApiFilterList filters = new FodApiFilterList()
                .addFilter("frequencyTypeId", model.getEntitlementPreference())
                .addFilter("assessmentTypeId", model.getBsiToken().getAssessmentTypeId());

        if (model.isBundledAssessment())
            filters.addFilter("isBundledAssessment", true);

        String url = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%s/assessment-types", model.getBsiToken().getProjectVersionId()))
                .addQueryParameter("scanType", "1")
                .addQueryParameter("filters", filters.toString())
                .build().toString();

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .get()
                .build();

        Response response = apiConnection.getClient().newCall(request).execute();

        if (response.code() == org.apache.http.HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
            // Re-authenticate
            apiConnection.authenticate();
        }

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        if(!Utils.isNullOrEmpty(content)) // check if any content is returned
        {
            Gson gson = new Gson();
            // Create a type of GenericList<ApplicationDTO> to play nice with gson.
            Type t = new TypeToken<GenericListResponse<ReleaseAssessmentTypeDTO>>() {
            }.getType();
            GenericListResponse<ReleaseAssessmentTypeDTO> results = gson.fromJson(content, t);

            // Get entitlement based on available options
            for (ReleaseAssessmentTypeDTO assessment : results.getItems()) {
                if (model.isPurchaseEntitlements() || assessment.getEntitlementId() > 0) {
                    return assessment;
                }
            }
        }
        return null;

    }
}
