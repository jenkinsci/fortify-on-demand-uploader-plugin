package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.AttributeDefinition;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;

public class AttributesController extends ControllerBase {

    public AttributesController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public List<AttributeDefinition> getAttributeDefinitions() throws IOException {

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        String url = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/attributes")
                .build()
                .toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();
        // TODO: check want happens when no permissions -- Arik
        Gson gson = new Gson();
        Type t = new TypeToken<GenericListResponse<AttributeDefinition>>(){}.getType();
        GenericListResponse<AttributeDefinition> results = gson.fromJson(content, t);

        return results.getItems();
    }
}
