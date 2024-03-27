package org.jenkinsci.plugins.fodupload;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.FodApi.FormBodyRequest;
import org.jenkinsci.plugins.fodupload.FodApi.HttpRequest;
import org.jenkinsci.plugins.fodupload.FodApi.IHttpClient;
import org.jenkinsci.plugins.fodupload.FodApi.ResponseContent;
import org.jenkinsci.plugins.fodupload.models.FodEnums;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class TokenCacheManager {

    // delete tokens that are this much close to expiry (in seconds)
    private static int DELETE_TOKEN_BEFORE_SECONDS = 120;
    private final static HashMap<String, Token> tokens = new HashMap<>();
    private PrintStream _logger;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public TokenCacheManager(PrintStream logger) {
        _logger = logger;
    }

    private void log(String msg) {
        if (_logger != null) _logger.println(msg);
    }

    public synchronized String getToken(IHttpClient client, String apiUrl, FodEnums.GrantType grantType, String scope, String id, String secret) throws IOException {
        String key = buildCacheKey(apiUrl, grantType, scope, id, secret);
        clearCache();

        if (!tokens.containsKey(key)) {
            Token token = retrieveToken(client, apiUrl, grantType, scope, id, secret);

            tokens.put(key, token);
            return token.value;
        }

        return tokens.get(key).value;
    }

    private String buildCacheKey(String apiUrl, FodEnums.GrantType grantType, String scope, String id, String secret) {
        return apiUrl + "$" + grantType + "$" + scope + "$" + id + "$" + secret;
    }

    private void clearCache() {
        for (Map.Entry<String, Token> token : tokens.entrySet()) {
            if (isCloseToExpiry(token.getValue())) {
                tokens.remove(token.getKey());
            }
        }
    }

    private Token retrieveToken(IHttpClient client, String apiUrl, FodEnums.GrantType grantType, String scope, String id, String secret) throws IOException {
        FormBodyRequest request = new FormBodyRequest(apiUrl + "/oauth/token", HttpRequest.Verb.Post);

        if (grantType == FodEnums.GrantType.CLIENT_CREDENTIALS) {
            log("Logging into API with token");
            request.addValue("scope", scope)
                    .addValue("grant_type", "client_credentials")
                    .addValue("client_id", id)
                    .addValue("client_secret", secret);
        } else if (grantType == FodEnums.GrantType.PASSWORD) {
            log("Logging into API with PAT");
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

        if (content == null || content.isEmpty())
            throw new IOException("Unexpected body to be null");

        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(content).getAsJsonObject();
        Calendar expiryTime = Calendar.getInstance();

        expiryTime.add(Calendar.SECOND, obj.get("expires_in").getAsInt());

        return new Token(obj.get("access_token").getAsString(), expiryTime);
    }

    private Boolean isCloseToExpiry(Token token) {
        return token.expiry.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 1000L * DELETE_TOKEN_BEFORE_SECONDS;
    }

    private static class Token {
        private String value;
        private Calendar expiry;

        public Token(String value, Calendar expiry) {
            this.value = value;
            this.expiry = expiry;
        }
    }
}
