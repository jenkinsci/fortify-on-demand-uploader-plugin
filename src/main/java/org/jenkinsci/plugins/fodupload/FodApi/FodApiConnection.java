package org.jenkinsci.plugins.fodupload.FodApi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.TokenCacheManager;
import org.jenkinsci.plugins.fodupload.models.FodEnums.GrantType;
import org.jenkinsci.plugins.fodupload.models.JobModel;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;

public class FodApiConnection {

    public final static int MAX_SIZE = 50;
    private final static int CONNECTION_TIMEOUT = 30; // seconds
    private final static int WRITE_TIMEOUT = 600; // seconds
    private final static int READ_TIMEOUT = 600; // seconds

    private final TokenCacheManager tokenCacheManager;

    private String baseUrl;
    private String apiUrl;
    private IHttpClient client;
    private String token;
    private GrantType grantType;
    private String scope;

    private String id;
    private String secret;

    private ProxyConfiguration proxy = null;

    private Launcher _launcher = null;
    private PrintStream _httpLogger = null;

    /**
     * Constructor that encapsulates the apiConnection
     *
     * @param id      apiConnection id
     * @param secret  apiConnection secret
     * @param baseUrl apiConnection baseUrl
     */
    public FodApiConnection(final String id, final String secret, final String baseUrl, final String apiUrl, final GrantType grantType, final String scope, boolean executeOnRemoteAgent, Launcher launcher, PrintStream logger) {
        this.id = id;
        this.secret = secret;
        this.baseUrl = baseUrl;
        this.apiUrl = apiUrl;
        this.grantType = grantType;
        this.scope = scope;
        this.tokenCacheManager = new TokenCacheManager(logger);

        //Jenkins instance = Jenkins.getInstance();
        Jenkins instance = Jenkins.getInstanceOrNull();

        if (instance != null) proxy = instance.proxy;
        _httpLogger = org.jenkinsci.plugins.fodupload.Utils.traceLogging() ? logger : null;

        // ToDo: implement optional env var for proxy
        if (executeOnRemoteAgent) {
            _launcher = launcher;
            client = new RemoteAgentClient(CONNECTION_TIMEOUT, WRITE_TIMEOUT, READ_TIMEOUT, proxy, _launcher, _httpLogger);
        } else client = new ServerClient(Utils.CreateOkHttpClient(CONNECTION_TIMEOUT, WRITE_TIMEOUT, READ_TIMEOUT, proxy), _httpLogger);
    }

    /**
     * Retire the current token. Unclear if this actually does anything on the backend.
     */
    public void retireToken() throws IOException {
        HttpRequest request = HttpRequest.get(apiUrl + "/oauth/retireToken")
                .addHeader("Authorization", "Bearer " + token);
        ResponseContent response = client.execute(request);

        if (response.isSuccessful()) {
            token = null;
        } else {
            throw new IOException(response.toString());
        }

    }

    public String testConnection() throws IOException {
        // ToDo: do something else here
        this.token = retrieveToken();

        if (token == null) return "Unable to retrieve authentication token.";
        else if (token.isEmpty()) return "Invalid connection information. Please check your credentials and try again.";

        return null;
    }

    private String retrieveToken() throws IOException {
        FormBodyRequest request = new FormBodyRequest(apiUrl + "/oauth/token", HttpRequest.Verb.Post);

        if (grantType == GrantType.CLIENT_CREDENTIALS) {
            request.addValue("scope", scope)
                    .addValue("grant_type", "client_credentials")
                    .addValue("client_id", id)
                    .addValue("client_secret", secret);
        } else if (grantType == GrantType.PASSWORD) {
            request.addValue("scope", scope)
                    .addValue("grant_type", "password")
                    .addValue("username", id)
                    .addValue("password", secret);
        } else {
            throw new IOException("Invalid Grant Type");
        }
        ResponseContent response = client.execute(request);

        if (!response.isSuccessful())
            throw new IOException("Unexpected code " + response);

        String content = response.bodyContent();

        // Parse the Response
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(content).getAsJsonObject();
        return obj.get("access_token").getAsString();
    }

    private String getTokenFromCache() throws IOException {
        return tokenCacheManager.getToken(client, apiUrl, grantType, scope, id, secret);
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

    public HttpUrl.Builder urlBuilder() {
        return HttpUrl.parse(getApiUrl()).newBuilder();
    }

    public ResponseContent request(Request request) throws IOException {
        if (client instanceof ServerClient) {
            Request req = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + getTokenFromCache())
                    .build();

            return ((ServerClient) client).execute(req);
        } else {
            return request(Utils.OkHttpRequestToHttpRequest(request));
        }
    }

    public ResponseContent request(HttpRequest request) throws IOException {
        request.setHeader("Authorization", "Bearer " + getTokenFromCache());

        return client.execute(request);
    }

    public <T> T requestTyped(Request request, Type t) throws IOException {
        ResponseContent res = this.request(request);

        return this.parseResponse(res, t);
    }

    public <T> T requestTyped(HttpRequest request, Type t) throws IOException {
        ResponseContent res = this.request(request);

        return this.parseResponse(res, t);
    }

    public <T> T parseResponse(ResponseContent response, Type t) throws IOException {
        String content = response.bodyContent();

        if (content == null)
            throw new IOException("Unexpected body to be null");

        return Json.getInstance().fromJson(content, t);
    }

    public ScanPayloadUpload getScanPayloadUploadInstance(JobModel uploadRequest, String correlationId, String fragUrl, PrintStream logger) throws IOException {
        if (this.client instanceof ServerClient) {
            return new ScanPayloadUploadLocal(((ServerClient) this.client).client(), getTokenFromCache(), uploadRequest, correlationId, fragUrl, logger);
        } else {
            return new ScanPayloadUploadRemote(uploadRequest, correlationId, fragUrl, getTokenFromCache(), CONNECTION_TIMEOUT, WRITE_TIMEOUT, READ_TIMEOUT, proxy, _launcher, logger);
        }
    }

    private void log(String msg) {
        if (_httpLogger != null) _httpLogger.println(msg);
    }

}

