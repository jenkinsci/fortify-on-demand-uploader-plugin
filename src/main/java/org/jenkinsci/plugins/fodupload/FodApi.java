package org.jenkinsci.plugins.fodupload;

import com.google.gson.*;
import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import okhttp3.*;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.controllers.*;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FodApi {

    private final static int CONNECTION_TIMEOUT = 30; // seconds
    private final static int WRITE_TIMEOUT = 30; // seconds
    private final static int READ_TIMEOUT = 30; // seconds
    public final static int MAX_SIZE = 50;

    private String baseUrl;
    private OkHttpClient client;
    private String token;

    private String key;
    private String secret;

    private ProxyConfiguration proxy = null;

    private StaticScanController staticScanController;

    public StaticScanController getStaticScanController() {
        return staticScanController;
    }

    private ReleaseController releaseController;

    public ReleaseController getReleaseController() {
        return releaseController;
    }

    private LookupItemsController lookupItemsController;

    public LookupItemsController getLookupItemsController() {
        return lookupItemsController;
    }

    /**
     * Constructor that encapsulates the api
     *
     * @param key     api key
     * @param secret  api secret
     * @param baseUrl api url
     */
    public FodApi(final String key, final String secret, final String baseUrl) {
        this.key = key;
        this.secret = secret;
        this.baseUrl = baseUrl;

        Jenkins instance = Jenkins.getInstance();
        if (instance != null)
            proxy = instance.proxy;

        client = Create();

        staticScanController = new StaticScanController(this);
        releaseController = new ReleaseController(this);
        lookupItemsController = new LookupItemsController(this);
    }

    /**
     * Used for authenticating in the case of a time out using the saved api credentials.
     */
    public void authenticate() {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("scope", "api-tenant")
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

    /**
     * Retire the current token. Unclear if this actually does anything on the backend.
     */
    public void retireToken() {
        try {
            PrintStream logger = FodUploaderPlugin.getLogger();

            Request request = new Request.Builder()
                    .url(baseUrl + "/oauth/retireToken")
                    .addHeader("Authorization", "Bearer " + token)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                // Read the results and close the response
                String content = IOUtils.toString(response.body().byteStream(), "utf-8");
                response.body().close();

                JsonParser parser = new JsonParser();
                JsonObject obj = parser.parse(content).getAsJsonObject();
                String messageResponse = obj.get("message").getAsString();

                logger.println("Retiring Token : " + messageResponse);
                token = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a okHttp client to connect with.
     *
     * @return returns a client object
     */
    private OkHttpClient Create() {

      SSLContext sslContext;

      try {
        sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, null);
      } catch ( NoSuchAlgorithmException | KeyManagementException e) {
        throw Throwables.propagate(e);
      }

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        OkHttpClient.Builder baseClient = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .sslSocketFactory(socketFactory)
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS));

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

    public OkHttpClient getClient() {
        return client;
    }
}
