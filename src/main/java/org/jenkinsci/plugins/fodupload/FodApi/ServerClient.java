package org.jenkinsci.plugins.fodupload.FodApi;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.PrintStream;

class ServerClient implements IHttpClient {
    private OkHttpClient _client;
    private PrintStream _logger;

    public ServerClient(OkHttpClient client, PrintStream logger) {
        _client = client;
        _logger = logger;
    }

    private void log(String msg) {
        if (_logger != null) _logger.println(msg);
    }

    public ResponseContent execute(HttpRequest request) throws IOException {
        return Utils.ResponseContentFromOkHttp3(_client.newCall(Utils.HttpRequestToOkHttpRequest(request)).execute());
    }

    public ResponseContent execute(Request request) throws IOException {
        log("Server http call:\n\tVerb: " + request.method() + "\n\tURL: " + request.url());
        return Utils.ResponseContentFromOkHttp3(_client.newCall(request).execute());
    }

    OkHttpClient client(){
        return _client;
    }
}
