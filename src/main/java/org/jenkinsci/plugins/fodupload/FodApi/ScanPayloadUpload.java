package org.jenkinsci.plugins.fodupload.FodApi;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.remoting.RemoteOutputStream;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.jenkinsci.plugins.fodupload.models.SastJobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericErrorResponse;
import org.jenkinsci.plugins.fodupload.models.response.PostStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.StartScanResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public interface ScanPayloadUpload {
    StartScanResponse performUpload() throws IOException;
}

final class ScanPayloadUploadImpl {
    private final static int CHUNK_SIZE = 1024 * 1024; //1MB

    static StartScanResponse performUpload(SastJobModel uploadRequest, String correlationId, String fragUrl, String bearerToken, OkHttpClient client, PrintStream log) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(org.jenkinsci.plugins.fodupload.Utils.getLogTimestampFormat());
        PostStartScanResponse scanStartedResponse = null;
        StartScanResponse scanResults = new StartScanResponse();
        File uploadFile = new File(uploadRequest.getPayload().getRemote());

        try (FileInputStream fs = new FileInputStream(uploadFile)) {
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            // Loop through chunks

            log.println("TOTAL FILE SIZE = " + uploadFile.length());
            log.println("CHUNK_SIZE = " + CHUNK_SIZE);

            while ((byteCount = fs.read(readByteArray)) != -1) {

                if (byteCount < CHUNK_SIZE) {
                    sendByteArray = Arrays.copyOf(readByteArray, byteCount);
                    fragmentNumber = -1;
                } else {
                    sendByteArray = readByteArray;
                }

                MediaType byteArray = MediaType.parse("application/octet-stream");
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + bearerToken)
                        .addHeader("Content-Type", "application/octet-stream")
                        .addHeader("Accept", "application/json")
                        .addHeader("CorrelationId", correlationId)
                        // Add offsets
                        .url(fragUrl + "&fragNo=" + fragmentNumber++ + "&offset=" + offset)
                        .post(RequestBody.create(byteArray, sendByteArray))
                        .build();

                log.println(getLogTimestamp(dateFormat) + " Uploading fragment " + fragmentNumber);
                // Get the response
                Response response = client.newCall(request).execute();

                if (response.code() == HttpStatus.SC_FORBIDDEN || response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                    String raw = Utils.getRawBody(response.body().byteStream());

                    response.body().close();

                    if (org.jenkinsci.plugins.fodupload.Utils.isNullOrEmpty(raw))
                        log.println(getLogTimestamp(dateFormat) + " Uploading fragment failed, reauthenticating");
                    else log.println(getLogTimestamp(dateFormat) + " Uploading fragment failed, reauthenticating \n" + raw);
                    continue;
                }

                offset += byteCount;

                if (fragmentNumber % 5 == 0) {
                    log.println(getLogTimestamp(dateFormat) + " Upload Status - Fragment No: " + fragmentNumber + ", Bytes sent: " + offset
                            + " (Response: " + response.code() + ")");
                }

                if (response.code() != 202) {
                    String raw = Utils.getRawBody(response.body().byteStream());

                    response.body().close();

                    Gson gson = new Gson();
                    // final response has 200, try to deserialize it
                    if (response.code() == 200) {

                        scanStartedResponse = gson.fromJson(raw, PostStartScanResponse.class);
                        log.println(getLogTimestamp(dateFormat) + " Scan " + scanStartedResponse.getScanId() + " uploaded successfully. Total bytes sent: " + offset);
                        scanResults.uploadSuccessfulScanStarting(scanStartedResponse.getScanId());
                        return scanResults;

                    } else if (!response.isSuccessful()) { // There was an error along the lines of 'another scan in progress' or something
                        log.println(getLogTimestamp(dateFormat) + " An error occurred during the upload.");
                        GenericErrorResponse errors = gson.fromJson(raw, GenericErrorResponse.class);

                        if (errors != null) {
                            if (errors.toString().contains("Can not start scan another scan is in progress")) {
                                scanResults.uploadSuccessfulScanNotStarted();
                            } else {
                                log.println(getLogTimestamp(dateFormat) + " Package upload failed for the following reasons: ");
                                log.println(errors);
                                scanResults.uploadNotSuccessful();
                            }
                        } else {
                            if (!org.jenkinsci.plugins.fodupload.Utils.isNullOrEmpty(raw))
                                log.println(getLogTimestamp(dateFormat) + " Raw response\n" + raw);
                            else log.println(getLogTimestamp(dateFormat) + " No response body from api");
                            scanResults.uploadNotSuccessful();
                        }

                        return scanResults; // if there is an error, get out of loop and mark build unstable
                    }
                }

            } // end while
            log.println(getLogTimestamp(dateFormat) + " Payload upload complete");
        } catch (Throwable e) {
            e.printStackTrace(log);
            scanResults.uploadNotSuccessful();
            return scanResults;
        }

        scanResults.uploadNotSuccessful();
        return scanResults;
    }

    private static String getLogTimestamp(DateTimeFormatter dateFormat) {
        return dateFormat.format(LocalDateTime.now());
    }

}

final class ScanPayloadUploadLocal implements ScanPayloadUpload {
    private OkHttpClient _client;
    private SastJobModel _uploadRequest;
    private String _correlationId;
    private String _fragUrl;
    private String _bearerToken;
    private PrintStream _logger;

    ScanPayloadUploadLocal(OkHttpClient client, String bearerToken, SastJobModel uploadRequest, String correlationId, String fragUrl, PrintStream logger) {
        _client = client;
        _bearerToken = bearerToken;
        _uploadRequest = uploadRequest;
        _correlationId = correlationId;
        _fragUrl = fragUrl;
        _logger = logger;
    }

    @Override
    public StartScanResponse performUpload() {
        return ScanPayloadUploadImpl.performUpload(_uploadRequest, _correlationId, _fragUrl, _bearerToken, _client, _logger);
    }
}

final class ScanPayloadUploadRemote extends MasterToSlaveCallable<StartScanResponse, IOException> implements ScanPayloadUpload {
    private static final long serialVersionUID = 1L;
    private SastJobModel _uploadRequest;
    private String _correlationId;
    private String _fragUrl;
    private String _bearerToken;
    private int _connectionTimeout;
    private int _writeTimeout;
    private int _readTimeout;
    private ProxyConfiguration _proxy;
    private transient VirtualChannel _channel;
    private RemoteOutputStream _logger;

    ScanPayloadUploadRemote(SastJobModel uploadRequest, String correlationId, String fragUrl,
                            String bearerToken, int connectionTimeout, int writeTimeout, int readTimeout, ProxyConfiguration proxy,
                            Launcher launcher, PrintStream logger) {
        _uploadRequest = uploadRequest;
        _correlationId = correlationId;
        _fragUrl = fragUrl;
        _bearerToken = bearerToken;
        _connectionTimeout = connectionTimeout;
        _writeTimeout = writeTimeout;
        _readTimeout = readTimeout;
        _proxy = proxy;

        _channel = launcher.getChannel();
        if (_channel == null) {
            throw new IllegalStateException("Launcher doesn't support remoting but it is required");
        }

        _logger = new RemoteOutputStream(logger);
    }

    @Override
    public StartScanResponse call() throws IOException {
        OkHttpClient client = Utils.createOkHttpClient(_connectionTimeout, _writeTimeout, _readTimeout, _proxy);
        PrintStream logger = new PrintStream(_logger, true, StandardCharsets.UTF_8.name());

        return ScanPayloadUploadImpl.performUpload(_uploadRequest, _correlationId, _fragUrl, _bearerToken, client, logger);
    }


    @Override
    public StartScanResponse performUpload() throws IOException {
        try {
            return _channel.call(this);
        } catch (InterruptedException e) {
            throw new IOException("Remote agent http call failed", e);
        }
    }
}
