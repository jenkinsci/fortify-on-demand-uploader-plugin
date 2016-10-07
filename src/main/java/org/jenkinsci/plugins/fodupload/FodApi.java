package org.jenkinsci.plugins.fodupload;


import okhttp3.*;

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

        client = Create(key, secret);
    }

    //TODO: Authenticate
    public void authenticate() {
        try {
            FormBody.Builder formBodyBuilder = new FormBody.Builder().add("scope", "https://hpfod.com/tenant");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO: retire token
    public void retireToken() {

    }

    private OkHttpClient Create(String key, String secret) {
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

