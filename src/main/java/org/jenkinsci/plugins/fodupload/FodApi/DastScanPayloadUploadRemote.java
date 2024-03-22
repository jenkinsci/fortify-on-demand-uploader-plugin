package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.FilePath;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;
import okhttp3.OkHttpClient;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class DastScanPayloadUploadRemote extends MasterToSlaveCallable<PatchDastFileUploadResponse, IOException> implements DastScanPayloadUpload {
    private static final long serialVersionUID = 1L;
    private final String _releaseId;
    private final String _correlationId;
    private final String _bearerToken;
    private final FilePath _filePath;
    private final int _connectionTimeout;
    private final int _writeTimeout;
    private final int _readTimeout;
    private final ProxyConfiguration _proxy;
    private final transient VirtualChannel _channel;
    private final RemoteOutputStream _logger;
    private final String _apiUri;

    DastScanPayloadUploadRemote(String releaseId, String bearerToken, FilePath remoteFilePath, String apiUri, String correlationId, PrintStream logger
            , int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy,
                                Launcher launcher) {
        _bearerToken = bearerToken;
        _correlationId = correlationId;
        _connectionTimeout = connectionTimeout;
        _writeTimeout = writeTimeout;
        _readTimeout = readTimeout;
        _filePath = remoteFilePath;
        _proxy = proxy;
        _apiUri = apiUri;
        _releaseId = releaseId;
        _channel = launcher.getChannel();
        if (_channel == null) {
            throw new IllegalStateException("Launcher doesn't support remoting but it is required");
        }

        _logger = new RemoteOutputStream(logger);
    }

    @Override
    public PatchDastFileUploadResponse call() {

        PrintStream logger = null;
        try {
            logger = new PrintStream(_logger, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        OkHttpClient client = Utils.createOkHttpClient(_connectionTimeout, _writeTimeout, _readTimeout, _proxy);
        try {
            return DastScanPayloadUploadImpl.performUpload(_filePath, _releaseId, _apiUri, _correlationId, _bearerToken, client, logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PatchDastFileUploadResponse performUpload() throws IOException {
        try {
            return _channel.call(this);
        } catch (InterruptedException e) {
            throw new IOException("Remote agent http call failed", e);
        }
    }
}
