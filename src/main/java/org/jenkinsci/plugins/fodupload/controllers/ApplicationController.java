package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApi;
import org.jenkinsci.plugins.fodupload.models.response.ApplicationDTO;
import org.jenkinsci.plugins.fodupload.models.response.GenericListResponse;

import java.lang.reflect.Type;
import java.util.List;

public class ApplicationController extends ControllerBase {
    /**
     * Constructor
     * @param api api object with client info
     */
    public ApplicationController(final FodApi api) {
        super(api);
    }

    /**
     * Get list of applications
     * @return List of application objects
     */
    public List<ApplicationDTO> getApplications() {
        try {
            String url = api.getBaseUrl() + "/api/v3/applications";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + api.getToken())
                    .get()
                    .build();
            Response response = api.getClient().newCall(request).execute();

            if (response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                // Re-authenticate
                api.authenticate();
            }

            // Read the results and close the response
            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            Gson gson = new Gson();
            // Create a type of GenericList<ApplicationDTO> to play nice with gson.
            Type t = new TypeToken<GenericListResponse<ApplicationDTO>>(){}.getType();
            GenericListResponse<ApplicationDTO> results =  gson.fromJson(content, t);
            return results.getItems();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}