package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.HttpUrl;
import okhttp3.Request;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.FodApi.ResponseContent;
import org.jenkinsci.plugins.fodupload.models.AuditPreferenceOptionsModel;
import org.jenkinsci.plugins.fodupload.models.FodApiFilterList;
import org.jenkinsci.plugins.fodupload.models.SastJobModel;
import org.jenkinsci.plugins.fodupload.models.response.*;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.jenkinsci.plugins.fodupload.Utils;

public class ReleaseController extends ControllerBase {

    /**
     * Constructor
     *
     * @param apiConnection apiConnection object with client info
     * @param logger        logger object
     * @param correlationId correlation id
     */
    public ReleaseController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    /**
     * Get an individual release with given fields
     *
     * @param releaseId release to get
     * @param fields    fields to return
     *                  at_return ReleaseDTO object with given fields
     * @throws java.io.IOException in some circumstances
     */
    public ReleaseDTO getRelease(final int releaseId, final String fields) throws IOException {
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
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        ResponseContent response = apiConnection.request(request);

        if (Utils.isUnauthorizedResponse(response)) {  // got logged out during polling so log back in
            return null;
        }

        // Read the results and close the response
        String content = response.bodyContent();

        Gson gson = new Gson();
        // Create a type of GenericList<ReleaseDTO> to play nice with gson.
        Type t = new TypeToken<GenericListResponse<ReleaseDTO>>() {
        }.getType();
        GenericListResponse<ReleaseDTO> results = gson.fromJson(content, t);
        if (results.getItems().size() > 0)
            return results.getItems().get(0);
        else
            return null;
    }

    /**
     * Get an individual release
     *
     * @param releaseId release to get
     * @param scanId    scanId to find specific scan result
     *                  at_return ScanSummaryDTO object
     * @throws java.io.IOException in some circumstances
     */
    public ScanSummaryDTO getRelease(final int releaseId, final int scanId) throws IOException {
        // TODO: Investigate why the endpoint for a release wasn't used
        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%d/scans", releaseId))
                .addQueryParameter("limit", "5")
                .addQueryParameter("orderBy", "scanId")
                .addQueryParameter("orderByDirection", "DESC");

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        ResponseContent response = apiConnection.request(request);

        if (Utils.isUnauthorizedResponse(response)) {
            return null;
        }
        // Read the results and close the response
        String content = response.bodyContent();

        Gson gson = new Gson();
        // Create a type of GenericList<ScanSummary> to play nice with gson.
        Type t = new TypeToken<GenericListResponse<ScanSummaryDTO>>() {
        }.getType();
        GenericListResponse<ScanSummaryDTO> results = gson.fromJson(content, t);
        ScanSummaryDTO resultDto = null;
        if (results.getItems().size() > 0) {
            for (ScanSummaryDTO sdto : results.getItems()) {
                if (sdto.getScanId() == scanId)
                    resultDto = sdto;
            }
        }
        return resultDto;
    }

    /**
     * Get an individual release
     *
     * @param releaseId release to get
     * @param scanId    scanId to find specific scan result
     *                  at_return ScanSummaryDTO object
     * @throws java.io.IOException in some circumstances
     */
    public PollingSummaryDTO getReleaseByScanId(final int releaseId, final int scanId) throws IOException {
        // TODO: Investigate why the endpoint for a release wasn't used
        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%d/scans/%s/polling-summary", releaseId, scanId));

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        ResponseContent response = apiConnection.request(request);

        if (Utils.isUnauthorizedResponse(response)) {
            return null;
        }

        // Read the results and close the response
        String content = response.bodyContent();

        Gson gson = new Gson();
        // Create a type of GenericList<ScanSummary> to play nice with gson.
        Type t = new TypeToken<PollingSummaryDTO>() {
        }.getType();
        PollingSummaryDTO resultDto = gson.fromJson(content, t);
        return resultDto;
    }


    //TODO DELETE ALL OF THIS! I don't think it's used after ticket US-318012

    /**
     * Get Assessment Type from bsi url
     *
     * @param model JobModel
     *              at_return returns assessment type obj
     */
    ReleaseAssessmentTypeDTO getAssessmentType(final SastJobModel model) throws IOException, URISyntaxException {


        FodApiFilterList filters = new FodApiFilterList()
                .addFilter("frequencyTypeId", model.getEntitlementPreference())
                .addFilter("assessmentTypeId", model.getBsiToken().getAssessmentTypeId());

        //TODO: "Project version ID"
        String url = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%s/assessment-types", model.getBsiToken().getReleaseId()))
                .addQueryParameter("scanType", "1")
                .addQueryParameter("filters", filters.toString())
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        ResponseContent response = apiConnection.request(request);

        if (response.code() == org.apache.http.HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
            return null;
        }

        // Read the results and close the response
        String content = response.bodyContent();

        if (!Utils.isNullOrEmpty(content)) // check if any content is returned
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

    public Integer getReleaseIdByName(final String appName, final String relName, final Boolean isMicroservice, final String microserviceName) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases/")
                .addQueryParameter("filters", "applicationName:" + appName + "+releaseName:" + relName)
                .addQueryParameter("fields", "releaseId,applicationName,releaseName,microserviceName")
                .addQueryParameter("offset", "0");

        if (isMicroservice) urlBuilder.addQueryParameter("microserviceName", microserviceName);

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Type typeToken = new TypeToken<GenericListResponse<ReleaseIdLookupResult>>() {
        }.getType();
        GenericListResponse<ReleaseIdLookupResult> response = apiConnection.requestTyped(request, typeToken);
        List<ReleaseIdLookupResult> items = response.getItems();
        int totalCount = response.getTotalCount();
        int itemsReceived = 0;

        do {
            for (ReleaseIdLookupResult rel : items) {
                if (rel.getApplicationName().equals(appName) &&
                        rel.getReleaseName().equals(relName) &&
                        (!isMicroservice || rel.getMicroserviceName().equals(microserviceName))) {
                    return rel.getReleaseId();
                }
            }

            itemsReceived = items.size();

            if (itemsReceived < totalCount) {
                urlBuilder.setQueryParameter("offset", String.valueOf(items.size()));
                request = new Request.Builder()
                        .url(urlBuilder.build())
                        .addHeader("Accept", "application/json")
                        .addHeader("CorrelationId", getCorrelationId())
                        .get()
                        .build();
                response = apiConnection.requestTyped(request, typeToken);

                if (response.getItems().size() < 1) throw new IOException("Invalid API response, releases page was empty");

                items = response.getItems();
            }
        } while (itemsReceived < totalCount);

        return null;
    }

    public AuditPreferenceOptionsModel getAuditPreferences(Integer releaseId, Integer assessmentType, Integer frequencyType) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases/" + releaseId + "/static-scan-options")
                .addQueryParameter("technologyStack", "a") // Doesn't matter for Audit Preferences
                .addQueryParameter("assessmentTypeId", assessmentType.toString())
                .addQueryParameter("entitlementFrequencyType", frequencyType.toString());

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Type typeToken = new TypeToken<GenericListResponse<ScanOption>>() {
        }.getType();
        GenericListResponse<ScanOption> response = apiConnection.requestTyped(request, typeToken);
        List<ScanOption> items = response.getItems();

        if (items != null && items.size() > 0) {
            for (ScanOption option : items) {
                if (option.getName().equals("AuditPreference")) {
                    if (option.getOptions() != null && option.getOptions().size() > 0) {
                        boolean automated = false;
                        boolean manual = false;

                        for (LookupItemsModel item : option.getOptions()) {
                            if (item.getText().equals("Automated")) automated = true;
                            else if (item.getText().equals("Manual")) manual = true;
                        }

                        return new AuditPreferenceOptionsModel(automated, manual);
                    }
                    break;
                }
            }
        }

        return new AuditPreferenceOptionsModel(false, false);
    }

}
