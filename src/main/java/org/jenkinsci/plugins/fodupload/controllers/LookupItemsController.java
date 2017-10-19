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
import java.lang.reflect.Type;
import java.util.List;

public class LookupItemsController extends ControllerBase {
    /**
     * Constructor
     *
     * @param apiConnection apiConnection connection object with client info
     */
    public LookupItemsController(FodApiConnection apiConnection) {
        super(apiConnection);
    }

    /**
     * GET given enum
     *
     * @param type enum to look up
     * @return array of enum values and text or null
     */
    public List<LookupItemsModel> getLookupItems(FodEnums.APILookupItemTypes type) throws IOException {

        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        String url = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/lookup-items")
                .addQueryParameter("type", type.toString())
                .build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        // Read the results and close the response
        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        Type t = new TypeToken<GenericListResponse<LookupItemsModel>>() {
        }.getType();
        GenericListResponse<LookupItemsModel> results = gson.fromJson(content, t);
        return results.getItems();
    }
}
