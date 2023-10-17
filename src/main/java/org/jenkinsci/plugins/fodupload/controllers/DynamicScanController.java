package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.Config.FodGlobalConstants;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.FodApi.ResponseContent;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastAutomatedScanSetupResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PostDastStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PutDynamicScanSetupResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.jenkinsci.plugins.fodupload.Config.FodGlobalConstants.FodDastApiConstants.DastWebSiteScanPutApi;
import static org.jenkinsci.plugins.fodupload.Config.FodGlobalConstants.FodDastApiConstants.DastWorkflowScanPutApi;

public class DynamicScanController extends ControllerBase {
    /**
     * Base constructor for all apiConnection controllers
     *
     * @param apiConnection apiConnection object (containing client etc.) of controller
     * @param logger        logger object
     * @param correlationId correlation id
     */
    public DynamicScanController(FodApiConnection apiConnection, PrintStream logger, String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public PutDynamicScanSetupResponse putDynamicWebSiteScanSettings(final Integer releaseId, PutDastStandardScanReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        System.out.println("req content " + requestContent);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastWebSiteScanPutApi ,releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);

        if (response.code() < 300) {

            System.out.println("response code: " + response.code());
            return apiConnection.parseResponse(response, new TypeToken<PutDynamicScanSetupResponse>() {
            }.getType());

        } else {
            String rawBody = apiConnection.parseResponse(response, new TypeToken<PutDynamicScanSetupResponse>() {
            }.getType());

            List<String> errors = Utils.unexpectedServerResponseErrors();

            if (!rawBody.isEmpty()) errors.add("Raw API response:\n" + rawBody);
            else errors.add("API empty response");

            return apiConnection.parseResponse(response, new TypeToken<PutDynamicScanSetupResponse>() {
            }.getType());
        }
    }

    public PutDynamicScanSetupResponse putDynamicWorkflowDrivenScanSettings(final Integer releaseId, PutDastWorkflowDrivenScanReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        System.out.println("req content " + requestContent);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastWorkflowScanPutApi ,releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);

        if (response.code() < 300) {
            System.out.println("response code: " + response.code());
            PutDynamicScanSetupResponse putDynamicScanSetupResponse = new PutDynamicScanSetupResponse();
            putDynamicScanSetupResponse.HttpCode =response.code();
            putDynamicScanSetupResponse.isSuccess = response.isSuccessful();
            return putDynamicScanSetupResponse;
        } else {
            String rawBody = apiConnection.parseResponse(response, new TypeToken<PutDynamicScanSetupResponse>() {
            }.getType());

            List<String> errors = Utils.unexpectedServerResponseErrors();
            if (!rawBody.isEmpty()) errors.add("Raw API response:\n" + rawBody);
            else errors.add("API empty response");
            return apiConnection.parseResponse(response, new TypeToken<PutDynamicScanSetupResponse>() {
            }.getType());
        }
    }
     public PatchDastFileUploadResponse PatchDynamicScan(PatchDastScanFileUploadReq requestModel) {

        try {

            File temp = null;
            String fileName = null;

            switch (requestModel.dastFileType) {
                case LoginMacro:
                    temp = File.createTempFile("loginmacro", ".webmacro");
                    fileName = "loginmacro.webmacro";
                    break;

                case WorkflowDrivenMacro:
                    temp = File.createTempFile("worflowdriven", requestModel.releaseId);
                    fileName = "WorkflowDriven.webmacro";
                    break;

            }
            if (temp == null) {
                throw new RuntimeException("File upload not available");
            }
            try (FileOutputStream outputStream = new FileOutputStream(temp)) {
                outputStream.write(requestModel.Content);
            }
            System.out.println(temp.getAbsolutePath());

            HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                    .addQueryParameter("dastFileType", (requestModel.dastFileType.getValue()))
                    .addPathSegments(String.format(FodGlobalConstants.FodDastApiConstants.DastFileUploadPatchApi, Integer.parseInt(requestModel.releaseId)));

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName,
                            RequestBody.create(MediaType.parse("application/octet-stream"), temp))
                    .build();

            Request request = new Request.Builder().url(urlBuilder.build())
                    .addHeader("Accept", "application/octet-stream")
                    .addHeader("CorrelationId", getCorrelationId())
                    .patch(requestBody).build();

            //ToDo:- Require code refactor to align with the error response from Fod API
            ResponseContent response = apiConnection.request(request);

            if (response.code() < 300) {

                System.out.println("response code: " + response.code());
                return apiConnection.parseResponse(response, new TypeToken<PatchDastFileUploadResponse>() {
                }.getType());


            } else {
                String rawBody = apiConnection.parseResponse(response, new TypeToken<PatchDastFileUploadResponse>() {
                }.getType());
                List<String> errors = Utils.unexpectedServerResponseErrors();
                if (!rawBody.isEmpty()) errors.add("Raw API response:\n" + rawBody);
                else errors.add("API empty response");
                return new PatchDastFileUploadResponse();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new PatchDastFileUploadResponse();
        }

    }

    public PostDastStartScanResponse StartDynamicScan(Integer releaseId) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder().addPathSegments(String.format(FodGlobalConstants.FodDastApiConstants.DastStartScanAPi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .post(RequestBody.create(MediaType.parse("application/json"),""))
                .build();

        ResponseContent response = apiConnection.request(request);
        if (response.code() < 300) {

            System.out.println("response code: " + response.code());
            return apiConnection.parseResponse(response, new TypeToken<PostDastStartScanResponse>() {
            }.getType());

        } else {
            String rawBody = apiConnection.parseResponse(response, new TypeToken<PostDastStartScanResponse>() {
            }.getType());

            List<String> errors = Utils.unexpectedServerResponseErrors();

            if (!rawBody.isEmpty()) errors.add("Raw API response:\n" + rawBody);
            else errors.add("API empty response");

            return new PostDastStartScanResponse();
        }

    }

    public GetDastAutomatedScanSetupResponse getDynamicScanSettings(final Integer releaseId) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder().addPathSegments(String.format(FodGlobalConstants.FodDastApiConstants.DastGetApi, releaseId));

        System.out.println("retrieve dynamic scan settings....");

        Request request = new Request.Builder().url(urlBuilder.build()).addHeader("Accept", "application/json").addHeader("CorrelationId", getCorrelationId()).get().build();
        return apiConnection.requestTyped(request, new TypeToken<GetDastAutomatedScanSetupResponse>() {
        }.getType());
    }


}
