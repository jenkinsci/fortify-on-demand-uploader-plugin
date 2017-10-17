package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.StaticAssessmentBuildStep;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseAssessmentTypeDTO;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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

        // TODO: Investigate why the endpoint for a release wasn't used
        String url = apiConnection.getApiUrl() + "/api/v3/releases?filters=releaseId:" + releaseId;

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        if (fields.length() > 0) {
            url += "&fields=" + fields;
        }

        url += "&limit=1";

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
        return results.getItems().get(0);
    }

    /**
     * Get Assessment Type from bsi url
     *
     * @param model JobModel
     * @return returns assessment type obj
     */
    public ReleaseAssessmentTypeDTO getAssessmentType(final JobModel model) throws IOException {

        String filters = "frequencyTypeId:" + model.getEntitlementPreference();
        if (model.isBundledAssessment())
            filters += "+isBundledAssessment:true";

        // encode these before we put them on the URL since we're not using the URL builder
        filters = URLEncoder.encode(filters, "UTF-8");
        String url = String.format("%s/api/v3/releases/%s/assessment-types?scanType=1&filters=%s",
                apiConnection.getApiUrl(),
                model.getBsiUrl().getProjectVersionId(),
                filters);

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

        Gson gson = new Gson();
        // Create a type of GenericList<ApplicationDTO> to play nice with gson.
        Type t = new TypeToken<GenericListResponse<ReleaseAssessmentTypeDTO>>() {
        }.getType();
        GenericListResponse<ReleaseAssessmentTypeDTO> results = gson.fromJson(content, t);

        // Get entitlement based on available options
        for (ReleaseAssessmentTypeDTO assessment : results.getItems()) {
            if (assessment.getAssessmentTypeId() == model.getBsiUrl().getAssessmentTypeId() &&
                    assessment.isRemediation() == model.isRemediationScan() &&
                    (model.isPurchaseEntitlements() || assessment.getEntitlementId() > 0)) {
                return assessment;
            }
        }
        return null;

    }
}
