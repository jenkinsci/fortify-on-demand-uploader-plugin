package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.FilePath;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface DastScanPayloadUpload {
    PatchDastFileUploadResponse performUpload() throws IOException;
}

class DastScanPayloadUploadImpl {
    static PatchDastFileUploadResponse performUpload(FilePath payload, String releaseId, String apiUri, String correlationId,
                                                     String bearerToken, OkHttpClient client, PrintStream log) throws IOException {

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(org.jenkinsci.plugins.fodupload.Utils.getLogTimestampFormat());
        File uploadFile = new File(payload.getRemote());

        if (!uploadFile.exists()) {
            throw new IOException(String.format("DAST scan payload file=%s not found", uploadFile.getName()));
        }
        log.println(uploadFile.getAbsolutePath());
        Path filePath = Paths.get(uploadFile.getAbsolutePath());

        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", uploadFile.getName(),
                        RequestBody.create(mediaType, Files.readAllBytes(filePath)))
                .build();
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + bearerToken)
                .addHeader("Content-Type", "application/octet-stream")
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", correlationId)
                .url(apiUri)
                .patch(requestBody)
                .build();

        PatchDastFileUploadResponse patchDastFileUploadResponse;

        try (Response response = client.newCall(request).execute()) {

            ResponseContent responseContent = Utils.ResponseContentFromOkHttp3(response);

            if (!response.isSuccessful()) {
                log.printf("Fortify OnDemand: %s , Failed to upload DAST manifest payload for release Id: %s ,Response Code %d , response content: %s%n",
                        getLogTimestamp(dateFormat), releaseId, responseContent.code(), responseContent.bodyContent());

                throw new IOException("Fortify OnDemand: Failed to upload DAST manifest payload");
            }
            patchDastFileUploadResponse = new PatchDastFileUploadResponse();
            patchDastFileUploadResponse = Utils.ConvertHttpResponseIntoDastApiResponse(responseContent, patchDastFileUploadResponse);

        } catch (IOException ex) {
            log.printf("Failed to upload DAST manifest payload for release id %s", releaseId);
            throw ex;
        }
        return patchDastFileUploadResponse;
    }

    private static String getLogTimestamp(DateTimeFormatter dateFormat) {
        return dateFormat.format(LocalDateTime.now());
    }
}

class DastScanPayloadUploadLocal implements DastScanPayloadUpload {
    private final OkHttpClient _client;
    private final String _releaseId;
    private final String _correlationId;
    private final String _bearerToken;
    private final PrintStream _logger;
    private final FilePath _filePath;
    private final String _apiUri;

    DastScanPayloadUploadLocal(OkHttpClient client, String releaseId, String bearerToken, String apiUri, FilePath remoteFilePath, String correlationId, PrintStream logger) {
        _client = client;
        _bearerToken = bearerToken;
        _correlationId = correlationId;
        _logger = logger;
        _filePath = remoteFilePath;
        _releaseId = releaseId;
        _apiUri = apiUri;
    }

    @Override
    public PatchDastFileUploadResponse performUpload() throws IOException {
        return DastScanPayloadUploadImpl.performUpload(_filePath, _releaseId, _apiUri, _correlationId, _bearerToken, _client, _logger);
    }
}

class DastScanPayloadUploadRemote extends MasterToSlaveCallable<PatchDastFileUploadResponse, IOException> implements DastScanPayloadUpload {
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
        OkHttpClient client = Utils.CreateOkHttpClient(_connectionTimeout, _writeTimeout, _readTimeout, _proxy);
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