package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
     * @param apiConnection api connection object with client info
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
        if (api.getToken() == null)
            api.authenticate();

        Request request = new Request.Builder()
                .url(api.getBaseUrl() + "/api/v3/lookup-items?type=" + type.toString())
                .addHeader("Authorization", "Bearer " + api.getToken())
                .addHeader("Accept", "application/json")
                .get()
                .build();
        Response response = api.getClient().newCall(request).execute();

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
