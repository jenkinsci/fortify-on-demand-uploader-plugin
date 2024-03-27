package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.FilePath;
import okhttp3.OkHttpClient;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;

import java.io.IOException;
import java.io.PrintStream;

public final class DastScanPayloadUploadLocal implements DastScanPayloadUpload {
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
