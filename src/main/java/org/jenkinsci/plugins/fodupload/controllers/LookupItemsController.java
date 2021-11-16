package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.response.LookupItemsModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;

public class LookupItemsController extends ControllerBase {
    /**
     * Constructor
     *
     * @param apiConnection apiConnection connection object with client info
     * @param logger logger object
     * @param correlationId correlation id
     */
    public LookupItemsController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    /**
     * GET given enum
     *
     * @param type enum to look up
     * @return array of enum values and text or null
     * @throws java.io.IOException in some circumstances
     */
    public List<LookupItemsModel> getLookupItems(FodEnums.APILookupItemTypes type) throws IOException {
        String url = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/lookup-items")
                .addQueryParameter("type", type.toString())
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();

        GenericListResponse<LookupItemsModel> response = apiConnection.requestTyped(request, new TypeToken<GenericListResponse<LookupItemsModel>>(){}.getType());

        return response.getItems();
    }
}
