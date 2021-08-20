package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.response.AssessmentTypeEntitlement;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class AssessmentTypesController extends ControllerBase {

    public AssessmentTypesController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public List<AssessmentTypeEntitlement> getStaticAssessmentTypeEntitlements(Integer releaseId) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases/" + releaseId + "/assessment-types");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        GenericListResponse<AssessmentTypeEntitlement> response = apiConnection.requestTyped(request, new TypeToken<GenericListResponse<AssessmentTypeEntitlement>>(){}.getType());

        return response.getItems();
    }
}
