package org.jenkinsci.plugins.fodupload.FodApi;

import hudson.FilePath;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.models.response.PatchDastFileUploadResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DastScanPayloadUploadImpl {
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
            patchDastFileUploadResponse = Utils.convertHttpResponseIntoDastApiResponse(responseContent, patchDastFileUploadResponse);

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
