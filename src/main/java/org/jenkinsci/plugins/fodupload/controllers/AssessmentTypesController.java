package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.response.AssessmentTypeEntitlement;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;

public class AssessmentTypesController extends ControllerBase {

    public AssessmentTypesController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public List<AssessmentTypeEntitlement> getStaticAssessmentTypeEntitlements(Integer releaseId) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases/" + releaseId + "/assessment-types")
                .addQueryParameter("scanType", "1")
                .addQueryParameter("offset", "0");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Type typeToken = new TypeToken<GenericListResponse<AssessmentTypeEntitlement>>() {
        }.getType();
        GenericListResponse<AssessmentTypeEntitlement> response = apiConnection.requestTyped(request, typeToken);
        List<AssessmentTypeEntitlement> items = response.getItems();
        int totalCount = response.getTotalCount();

        while (items.size() < totalCount) {
            urlBuilder.setQueryParameter("offset", String.valueOf(items.size()));
            request = new Request.Builder()
                    .url(urlBuilder.build())
                    .addHeader("Accept", "application/json")
                    .addHeader("CorrelationId", getCorrelationId())
                    .get()
                    .build();
            response = apiConnection.requestTyped(request, typeToken);

            if (response.getItems().size() < 1) throw new IOException("Invalid API response, assessment-types page was empty");

            items.addAll(response.getItems());
        }

        return items;
    }

    public List<AssessmentTypeEntitlement> getStaticAssessmentTypeEntitlements(Boolean isMicroservice) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/tenant-assessment-types")
                .addQueryParameter("scanType", "1")
                .addQueryParameter("forMicroservice", isMicroservice != null ? isMicroservice.toString() : "false" );

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Type typeToken = new TypeToken<GenericListResponse<AssessmentTypeEntitlement>>() {
        }.getType();
        GenericListResponse<AssessmentTypeEntitlement> response = apiConnection.requestTyped(request, typeToken);
        List<AssessmentTypeEntitlement> items = response.getItems();

        return items;
    }
}
