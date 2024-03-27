package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.ProxyConfiguration;
import hudson.remoting.RemoteOutputStream;
import jenkins.security.MasterToSlaveCallable;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

class RemoteProxyCallable extends MasterToSlaveCallable<ResponseContent, IOException> {
    private static final long serialVersionUID = 1L;
    private int _connectionTimeout;
    private int _writeTimeout;
    private int _readTimeout;
    private ProxyConfiguration _proxy;
    private HttpRequest _request;
    private RemoteOutputStream _remoteLogger;
    private transient PrintStream _logger;

    RemoteProxyCallable(HttpRequest request, int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy, PrintStream logger) {
        _connectionTimeout = connectionTimeout;
        _writeTimeout = writeTimeout;
        _readTimeout = readTimeout;
        _proxy = proxy;
        _request = request;

        if (logger != null) _remoteLogger = new RemoteOutputStream(logger);
        else _remoteLogger = null;
    }

    private void log(String msg) throws IOException {
        if (_remoteLogger != null) {
            if (_logger == null) _logger = new PrintStream(_remoteLogger, true, StandardCharsets.UTF_8.name());

            _logger.println(msg);
        }
    }

    @Override
    public ResponseContent call() throws IOException {
        log("Remote http call:\n\tVerb: " + _request.verb() + "\n\tURL: " + _request.url());
        OkHttpClient client = Utils.createOkHttpClient(_connectionTimeout, _writeTimeout, _readTimeout, _proxy);
        Request request = Utils.HttpRequestToOkHttpRequest(_request);

        return Utils.ResponseContentFromOkHttp3(client.newCall(request).execute());
    }


}
