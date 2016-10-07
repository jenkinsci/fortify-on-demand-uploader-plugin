package org.jenkinsci.plugins.fodupload;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FodApi {
    //TODO: not hardcoded
    private String baseUrl = "http://api.local";
    private OkHttpClient client;
    private String token;

    private String key;
    private String secret;

    //TODO: set this up to pull from Jenkins
    private String proxy = null;

    private final int CONNECTION_TIMEOUT = 10;
    private final int WRITE_TIMEOUT = 30;
    private final int READ_TIMEOUT = 30;

    public FodApi(String key, String secret) {
        this.key = key;
        this.secret = secret;

        client = Create();
    }

    //TODO: Authenticate
    public String authenticate() {
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
            token = obj.get("access_token").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
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
}

