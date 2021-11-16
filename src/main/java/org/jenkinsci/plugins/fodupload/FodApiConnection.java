package org.jenkinsci.plugins.fodupload;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.org.apache.xpath.internal.operations.Bool;
import hudson.ProxyConfiguration;
import javafx.util.Pair;
import jenkins.model.Jenkins;
import okhttp3.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.FodEnums.GrantType;

public class FodApiConnection {

    public final static int MAX_SIZE = 50;
    private final static int CONNECTION_TIMEOUT = 30; // seconds
    private final static int WRITE_TIMEOUT = 600; // seconds
    private final static int READ_TIMEOUT = 600; // seconds

    private final static TokenCacheManager tokenCacheManager = new TokenCacheManager();

    private String baseUrl;
    private String apiUrl;
    private OkHttpClient client;
    private String token;
    private GrantType grantType;
    private String scope;

    private String id;
    private String secret;

    private ProxyConfiguration proxy = null;

    /**
     * Constructor that encapsulates the apiConnection
     *
     * @param id     apiConnection id
     * @param secret  apiConnection secret
     * @param baseUrl apiConnection baseUrl
     */
    FodApiConnection(final String id, final String secret, final String baseUrl, final String apiUrl, final GrantType grantType, final String scope) {
        this.id = id;
        this.secret = secret;
        this.baseUrl = baseUrl;
        this.apiUrl = apiUrl;
        this.grantType = grantType;
        this.scope = scope;

        Jenkins instance = Jenkins.getInstance();
        if (instance != null)
            proxy = instance.proxy;

        client = Create();
    }

    /**
     * Used for authenticating in the case of a time out using the saved apiConnection credentials.
     * @deprecated
     * Use the {@link FodApiConnection#request(Request)} method instead
     * @throws java.io.IOException in some circumstances
     */
    @Deprecated
    public void authenticate() throws IOException {
        this.token = retrieveToken();
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
        } else {
            throw new IOException(response.toString());
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

    private String retrieveToken() throws IOException {
        RequestBody formBody = null;
        if (grantType == GrantType.CLIENT_CREDENTIALS) {
            formBody = new FormBody.Builder()
                    .add("scope", scope)
                    .add("grant_type", "client_credentials")
                    .add("client_id", id)
                    .add("client_secret", secret)
                    .build();
        } else if (grantType == GrantType.PASSWORD) {
            formBody = new FormBody.Builder()
                    .add("scope", scope)
                    .add("grant_type", "password")
                    .add("username", id)
                    .add("password", secret)
                    .build();
        } else {
            throw new IOException("Invalid Grant Type");
        }

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
        return obj.get("access_token").getAsString();
    }

    private String getTokenFromCache() throws IOException {
        return tokenCacheManager.getToken(client, apiUrl, grantType, scope, id, secret);
    }

    /**
     * @deprecated
     * Use the {@link FodApiConnection#request(Request)} method instead
     * @return
     */
    @Deprecated
    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
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

    /**
     * @deprecated
     * Use the {@link FodApiConnection#request(Request)} method instead
     * @return
     */
    @Deprecated
    public OkHttpClient getClient() {
        return client;
    }

    public HttpUrl.Builder urlBuilder() {
        return HttpUrl.parse(getApiUrl()).newBuilder();
    }

    public Response request(Request request) throws IOException {
        request = request.newBuilder()
                .addHeader("Authorization", "Bearer " + getTokenFromCache())
                .build();

        return client.newCall(request).execute();
    }

    public <T> T requestTyped(Request request, Type t) throws IOException {
        Response res = this.request(request);
        return this.parseResponse(res, t);
    }

    public <T> T parseResponse(Response response, Type t) throws IOException {
        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("Unexpected body to be null");

        InputStream stream = body.byteStream();
        try {
            String content = IOUtils.toString(stream, "utf-8");
            return Json.getInstance().fromJson(content, t);
        }
        finally {
            stream.close();
            body.close();
        }
    }

    public String getRawBody(Response response) {
        ResponseBody body = response.body();
        if (body == null) return null;

        InputStream stream = body.byteStream();
        String content = null;

        try {
            content = IOUtils.toString(stream, "utf-8");

            stream.close();
            body.close();
        }
        catch (Exception e) {

        }

        return content;
    }

    /**
     * @deprecated
     * Use the {@link FodApiConnection#request(Request)} method instead
     * @return
     */
    public Request reauthenticateRequest(Request request) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + getToken())
                .build();
    }
}

