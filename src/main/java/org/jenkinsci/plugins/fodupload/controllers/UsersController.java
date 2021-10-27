package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.Result;
import org.jenkinsci.plugins.fodupload.models.response.CurrentUserSessionResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class UsersController extends ControllerBase {

    public UsersController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public Result<CurrentUserSessionResponse> getCurrentUserSession() throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/users/me/session");

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();

        Response res = apiConnection.request(request);

        if (!res.isSuccessful()) {
            if (res.code() == 401) {
                return new Result<>(false, new ArrayList<String>(){ { add("HTTP Error " + res.code()); } }, "no_auth", null);
            }
            else {
                return new Result<>(false, new ArrayList<String>(){ { add("HTTP Error " + res.code()); } }, null);
            }
        }

        CurrentUserSessionResponse session = apiConnection.parseResponse(res, new TypeToken<CurrentUserSessionResponse>(){}.getType());
        return new Result<>(true, null, session);
    }
}