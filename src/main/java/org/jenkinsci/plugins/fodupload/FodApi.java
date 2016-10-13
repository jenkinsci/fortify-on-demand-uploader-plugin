package org.jenkinsci.plugins.fodupload;

import com.google.gson.*;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import okhttp3.*;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.controllers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class FodApi {
    private String baseUrl;
    private OkHttpClient client;
    private String token;

    private String key;
    private String secret;

    private ProxyConfiguration proxy = Jenkins.getInstance().proxy;

    private final int CONNECTION_TIMEOUT = 10;
    private final int WRITE_TIMEOUT = 30;
    private final int READ_TIMEOUT = 30;

    private StaticScanController staticScanController;
    public StaticScanController getStaticScanController() { return staticScanController; }
    private ApplicationController applicationController;
    public ApplicationController getApplicationController() { return applicationController; }
    private ReleaseController releaseController;
    public ReleaseController getReleaseController() { return releaseController; }
    private TenantEntitlementsController tenantEntitlementsController;
    public TenantEntitlementsController getTenantEntitlementsController() { return tenantEntitlementsController; }
    private LookupItemsController lookupItemsController;
    public LookupItemsController getLookupItemsController() { return lookupItemsController; }

    public FodApi(String key, String secret, String baseUrl) {
        this.key = key;
        this.secret = secret;
        this.baseUrl = baseUrl;

        client = Create();

        staticScanController = new StaticScanController(this);
        applicationController = new ApplicationController(this);
        releaseController = new ReleaseController(this);
        tenantEntitlementsController = new TenantEntitlementsController(this);
        lookupItemsController = new LookupItemsController(this);
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

        OkHttpClient.Builder proxyClient = baseClient
                .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.name, proxy.port)));
        // Otherwise set up proxy
        Authenticator proxyAuthenticator;
        String credentials = Credentials.basic(proxy.getUserName(), proxy.getPassword());

        proxyAuthenticator = (route, response) -> response.request().newBuilder()
                .header("Proxy-Authorization", credentials)
                .build();
        proxyClient.proxyAuthenticator(proxyAuthenticator);
        return proxyClient.build();
    }

    public String getToken() { return token; }
    public String getKey() { return key; }
    public String getSecret() { return secret; }
    public String getBaseUrl() { return baseUrl; }
    public OkHttpClient getClient() { return client; }

    public boolean isAuthenticated() { return !token.isEmpty(); }

}

