package org.jenkinsci.plugins.fodupload;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import okhttp3.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class FodApiConnection {

    private final static int CONNECTION_TIMEOUT = 30; // seconds
    private final static int WRITE_TIMEOUT = 30; // seconds
    private final static int READ_TIMEOUT = 30; // seconds
    public final static int MAX_SIZE = 50;

    private String baseUrl;
    private String apiUrl;
    private OkHttpClient client;
    private String token;

    private String key;
    private String secret;

    private ProxyConfiguration proxy = null;

    /**
     * Constructor that encapsulates the apiConnection
     *
     * @param key     apiConnection key
     * @param secret  apiConnection secret
     * @param baseUrl apiConnection url
     */
    FodApiConnection(final String key, final String secret, final String baseUrl, final String apiUrl) {
        this.key = key;
        this.secret = secret;
        this.baseUrl = baseUrl;
        this.apiUrl = apiUrl;

        Jenkins instance = Jenkins.getInstance();
        if (instance != null)
            proxy = instance.proxy;

        client = Create();
    }

    /**
     * Used for authenticating in the case of a time out using the saved apiConnection credentials.
     */
    public void authenticate() throws IOException {

        RequestBody formBody = new FormBody.Builder()
                .add("scope", "api-tenant")
                .add("grant_type", "client_credentials")
                .add("client_id", key)
                .add("client_secret", secret)
                .build();

        Request request = new Request.Builder()
                .url(apiUrl + "/oauth/token")
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
    }

    /**
     * Retire the current token. Unclear if this actually does anything on the backend.
     */
    void retireToken() throws IOException {

        Request request = new Request.Builder()
                .url(apiUrl + "/oauth/retireToken")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            response.body().close();
            token = null;
        }
    }

    /**
     * Creates a okHttp client to connect with.
     *
     * @return returns a client object
     */
    private OkHttpClient Create() {
        OkHttpClient.Builder baseClient = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

        // If there's no proxy just create a normal client
        if (proxy == null)
            return baseClient.build();

        OkHttpClient.Builder proxyClient = baseClient
                .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.name, proxy.port)));
        // Otherwise set up proxy
        Authenticator proxyAuthenticator;
        final String credentials = Credentials.basic(proxy.getUserName(), proxy.getPassword());

        proxyAuthenticator = (route, response) -> response.request().newBuilder()
                .header("Proxy-Authorization", credentials)
                .build();

        proxyClient.proxyAuthenticator(proxyAuthenticator);
        return proxyClient.build();
    }

    public String getToken() {
        return token;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public OkHttpClient getClient() {
        return client;
    }
}

