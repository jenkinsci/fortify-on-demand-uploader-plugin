package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.AttributeDefinition;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class AttributesController extends ControllerBase {

    public AttributesController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public List<AttributeDefinition> getAttributeDefinitions() throws IOException {

        // TODO: check want happens when no permissions -- Arik
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/attributes");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        GenericListResponse<AttributeDefinition> response = apiConnection.requestTyped(request, new TypeToken<GenericListResponse<AttributeDefinition>>(){}.getType());

        return response.getItems();
    }
}
