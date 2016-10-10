package org.jenkinsci.plugins.fodupload;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import hudson.model.TaskListener;
import okhttp3.*;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.Models.ApplicationDTO;
import org.jenkinsci.plugins.fodupload.Models.GenericListResponse;
import org.jenkinsci.plugins.fodupload.Models.ReleaseAssessmentTypeDTO;
import org.jenkinsci.plugins.fodupload.Models.ReleaseDTO;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FodApi {
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String BASE_URL = "baseUrl";


    private String baseUrl;
    private OkHttpClient client;
    private String token;

    private String key;
    private String secret;

    //TODO: set this up to pull from Jenkins
    private String proxy = null;

    private final int CONNECTION_TIMEOUT = 10;
    private final int WRITE_TIMEOUT = 30;
    private final int READ_TIMEOUT = 30;

    public FodApi(String key, String secret, String baseUrl) {
        this.key = key;
        this.secret = secret;
        this.baseUrl = baseUrl;

        client = Create();
    }

    public void authenticate() {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("scope", "https://hpfod.com/tenant")
                    .add("grant_type", "client_credentials")
                    .add("client_id", key)
                    .add("client_secret", secret)
                    .build();

            Request request = new Request.Builder()
                    .url(baseUrl + "/oauth/token")
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            // Parse the Response
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(content).getAsJsonObject();
            this.token = obj.get("access_token").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: retire token
    public void retireToken() {

    }

    private OkHttpClient Create() {
        OkHttpClient.Builder baseClient = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

        // If there's no proxy just create a normal client
        if(proxy == null)
            return baseClient.build();

        //TODO: ...otherwise set up proxy
        return null;
    }

    public String getToken() { return token; }
    public String getKey() { return key; }
    public String getSecret() { return secret; }
    public String getBaseUrl() { return baseUrl; }

    public boolean isAuthenticated() { return !token.isEmpty(); }

    public List<ApplicationDTO> getApplications() {
        try {
            String url = baseUrl + "/api/v3/applications";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();

            if (response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                // Re-authenticate
                authenticate();
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

    public List<ReleaseDTO> getReleases(final String applicationId) {
        try {
            String url = baseUrl + "/api/v3/applications/" + applicationId + "/releases";

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();

            if (response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                // Re-authenticate
                authenticate();
            }

            // Read the results and close the response
            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            Gson gson = new Gson();
            // Create a type of GenericList<ApplicationDTO> to play nice with gson.
            Type t = new TypeToken<GenericListResponse<ReleaseDTO>>(){}.getType();
            GenericListResponse<ReleaseDTO> results =  gson.fromJson(content, t);

            return results.getItems();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ReleaseAssessmentTypeDTO> getAssessmentTypeIds(String releaseId) {
        try {
            String url = baseUrl + "/api/v3/releases/" + releaseId + "/assessment-types?scanType=1";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();

            if (response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                // Re-authenticate
                authenticate();
            }

            // Read the results and close the response
            String content = IOUtils.toString(response.body().byteStream(), "utf-8");
            response.body().close();

            Gson gson = new Gson();
            // Create a type of GenericList<ApplicationDTO> to play nice with gson.
            Type t = new TypeToken<GenericListResponse<ReleaseAssessmentTypeDTO>>(){}.getType();
            GenericListResponse<ReleaseAssessmentTypeDTO> results =  gson.fromJson(content, t);

            return results.getItems();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

