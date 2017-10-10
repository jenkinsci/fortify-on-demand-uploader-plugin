package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApi;
import org.jenkinsci.plugins.fodupload.StaticAssessmentBuildStep;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseAssessmentTypeDTO;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ReleaseController extends ControllerBase {
    /**
     * Constructor
     *
     * @param api api object with client info
     */
    public ReleaseController(FodApi api) {
        super(api);
    }

    /**
     * Get list of releases for a given application
     *
     * @param applicationId application to get releases of
     * @return List of releases
     */
    public List<ReleaseDTO> getReleases(final int applicationId) {
        try {
            int offset = 0, resultSize = api.MAX_SIZE;
            List<ReleaseDTO> releaseList = new ArrayList<>();

            // Pagination. Will continue until the results are less than the MAX_SIZE, which indicates that you've
            // hit the end of the results.
            while (resultSize == api.MAX_SIZE) {
                String url = api.getBaseUrl() + "/api/v3/applications/" + applicationId + "/releases?" +
                        "offset=" + offset + "&limit=" + api.MAX_SIZE;

                if (api.getToken() == null)
                    api.authenticate();

                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + api.getToken())
                        .get()
                        .build();
                Response response = api.getClient().newCall(request).execute();

                if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                    // Re-authenticate
                    api.authenticate();
                }

                // Read the results and close the response
                String content = IOUtils.toString(response.body().byteStream(), "utf-8");
                response.body().close();

                Gson gson = new Gson();
                Type t = new TypeToken<GenericListResponse<ReleaseDTO>>() {
                }.getType();
                GenericListResponse<ReleaseDTO> results = gson.fromJson(content, t);

                resultSize = results.getItems().size();
                offset += api.MAX_SIZE;
                releaseList.addAll(results.getItems());
            }
            return releaseList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get an individual release with given fields
     *
     * @param releaseId release to get
     * @param fields    fields to return
     * @return ReleaseDTO object with given fields
     */
    public ReleaseDTO getRelease(final int releaseId, final String fields) {
        try {
            PrintStream logger = StaticAssessmentBuildStep.getLogger();
            String url = api.getBaseUrl() + "/api/v3/releases?filters=releaseId:" + releaseId;

            if (api.getToken() == null)
                api.authenticate();

            if (fields.length() > 0) {
                url += "&fields=" + fields + "&limit=1";
            }
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();
            Response response = api.getClient().newCall(request).execute();

            if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                logger.println("Token expired re-authorizing");
                // Re-authenticate
                api.authenticate();
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get Assessment Type from bsi url
     * @param model JobModel
     * @return returns assessment type obj
     */
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    public ReleaseAssessmentTypeDTO getAssessmentType(final JobModel model) {
        final PrintStream logger = StaticAssessmentBuildStep.getLogger();
        try {

            String filters = "frequencyTypeId:" + model.getEntitlementPreference();
            if (model.isBundledAssessment())
                filters += "+isBundledAssessment:true";

            // encode these before we put them on the URL since we're not using the URL builder
            filters = URLEncoder.encode(filters, "UTF-8");
            String url = String.format("%s/api/v3/releases/%s/assessment-types?scanType=1&filters=%s",
                    api.getBaseUrl(),
                    model.getBsiUrl().getProjectVersionId(),
                    filters);

            if (api.getToken() == null)
                api.authenticate();

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .addHeader("Accept", "application/json")
                    .get()
                    .build();
            Response response = api.getClient().newCall(request).execute();

            if (response.code() == org.apache.http.HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                // Re-authenticate
                api.authenticate();
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
        } catch (Exception e) {
            logger.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
