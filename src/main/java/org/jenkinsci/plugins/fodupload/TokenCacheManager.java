package org.jenkinsci.plugins.fodupload;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.models.FodEnums;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TokenCacheManager {

    // delete tokens that are this much close to expiry (in seconds)
    private static int DELETE_TOKEN_BEFORE_SECONDS = 120;

    private HashMap<String, Token> tokens;

    public TokenCacheManager() {
        this.tokens = new HashMap<>();
    }

    public synchronized String getToken(OkHttpClient client, String apiUrl, FodEnums.GrantType grantType, String scope, String id, String secret) throws IOException {
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
        for (String key : tokens.keySet()) {
            if (isCloseToExpiry(tokens.get(key))) {
                tokens.remove(key);
            }
        }
    }

    private Token retrieveToken(OkHttpClient client, String apiUrl, FodEnums.GrantType grantType, String scope, String id, String secret) throws IOException {
        RequestBody formBody = null;
        if (grantType == FodEnums.GrantType.CLIENT_CREDENTIALS) {
            formBody = new FormBody.Builder()
                    .add("scope", scope)
                    .add("grant_type", "client_credentials")
                    .add("client_id", id)
                    .add("client_secret", secret)
                    .build();
        } else if (grantType == FodEnums.GrantType.PASSWORD) {
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

        ResponseBody body = response.body();
        if (body == null)
            throw new IOException("Unexpected body to be null");

        InputStream stream = body.byteStream();
        try {
            String content = IOUtils.toString(stream, "utf-8");
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(content).getAsJsonObject();

            Calendar expiryTime = Calendar.getInstance();
            expiryTime.add(Calendar.SECOND, obj.get("expires_in").getAsInt());
            return new Token(obj.get("access_token").getAsString(), expiryTime);
        }
        finally {
            stream.close();
            body.close();
        }
    }

    private Boolean isCloseToExpiry(Token token) {
        return token.expiry.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() < 1000 * DELETE_TOKEN_BEFORE_SECONDS;
    }

    private class Token {
        private String value;
        private Calendar expiry;

        public Token(String value, Calendar expiry) {
            this.value = value;
            this.expiry = expiry;
        }
    }
}
