package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.reflect.TypeToken;
import hudson.FilePath;
import okhttp3.*;
import org.jenkinsci.plugins.fodupload.Config.FodGlobalConstants;
import org.jenkinsci.plugins.fodupload.FodApi.DastScanPayloadUpload;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.FodApi.ResponseContent;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.jenkinsci.plugins.fodupload.models.response.Dast.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.jenkinsci.plugins.fodupload.Config.FodGlobalConstants.FodDastApiEndpoint.*;

public class DastScanController extends ControllerBase {
    /**
     * Base constructor for all apiConnection controllers
     *
     * @param apiConnection apiConnection object (containing client etc.) of controller
     * @param logger        logger object
     * @param correlationId correlation id
     */
    public DastScanController(FodApiConnection apiConnection, PrintStream logger, String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    public PutDastScanSetupResponse SaveDastWebSiteScanSettings(final Integer releaseId, PutDastWebSiteScanReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        System.out.println("req content " + requestContent);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastWebSiteScanPutApi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);

        PutDastScanSetupResponse putDynamicScanSetupResponse = new PutDastScanSetupResponse();
        putDynamicScanSetupResponse = (PutDastScanSetupResponse) convertHttpResponseIntoDastApiResponse(response, putDynamicScanSetupResponse);
        return putDynamicScanSetupResponse;
    }

    public PutDastScanSetupResponse SaveDastWorkflowDrivenScanSettings(final Integer releaseId, PutDastWorkflowDrivenScanReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        System.out.println("req content " + requestContent);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastWorkflowScanPutApi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);
        PutDastScanSetupResponse putDastScanSetupResponse = new PutDastScanSetupResponse();
        putDastScanSetupResponse = convertHttpResponseIntoDastApiResponse(response, putDastScanSetupResponse);
        return putDastScanSetupResponse;
    }

    public PatchDastFileUploadResponse DastFileUpload(PatchDastScanFileUploadReq requestModel) throws Exception {

        try {
            HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                    .addQueryParameter("dastFileType", (requestModel.dastFileType.getValue()))
                    .addPathSegments(String.format(FodGlobalConstants.FodDastApiEndpoint.DastFileUploadPatchApi, Integer.parseInt(requestModel.releaseId)));

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", requestModel.fileName,
                            RequestBody.create(MediaType.parse("application/octet-stream"), requestModel.Content))
                    .build();

            Request request = new Request.Builder().url(urlBuilder.build())
                    .addHeader("Accept", "application/octet-stream")
                    .addHeader("CorrelationId", getCorrelationId())
                    .patch(requestBody).build();
            ResponseContent response = apiConnection.request(request);
            PatchDastFileUploadResponse patchDastFileUploadResponse = new PatchDastFileUploadResponse();
            patchDastFileUploadResponse = convertHttpResponseIntoDastApiResponse(response, patchDastFileUploadResponse);

            return patchDastFileUploadResponse;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }


    public PatchDastFileUploadResponse DastFileUpload(FilePath payload, PrintStream logger,
                                                      PatchDastScanFileUploadReq requestModel) throws Exception {
        try {

            HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                    .addQueryParameter("dastFileType", (requestModel.dastFileType.getValue()))
                    .addPathSegments(String.format(FodGlobalConstants.FodDastApiEndpoint.DastFileUploadPatchApi, Integer.parseInt(requestModel.releaseId)));

            DastScanPayloadUpload dastScanPayloadUpload = apiConnection.getDastScanPayloadUploadInstance(payload, requestModel.releaseId,
                    urlBuilder.build().url().toString(), correlationId, logger);

            return dastScanPayloadUpload.performUpload();

        } catch (Exception ex) {
            logger.println(ex.getMessage());
            throw ex;
        }
    }


    public PostDastStartScanResponse StartDastScan(Integer releaseId) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder().addPathSegments(String.format(FodGlobalConstants.FodDastApiEndpoint.DastStartScanAPi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .post(RequestBody.create(MediaType.parse("application/json"), ""))
                .build();

        ResponseContent response = apiConnection.request(request);
        PostDastStartScanResponse postDastStartScanResponse = new PostDastStartScanResponse();
        postDastStartScanResponse = convertHttpResponseIntoDastApiResponse(response, postDastStartScanResponse);
        return postDastStartScanResponse;
    }

    public GetDastScanSettingResponse getDastScanSettings(final Integer releaseId) throws IOException {

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder().addPathSegments(String.format(FodGlobalConstants.FodDastApiEndpoint.DastGetApi, releaseId));

        Request request = new Request.Builder().url(urlBuilder.build()).addHeader("Accept", "application/json").addHeader("CorrelationId", getCorrelationId()).get().build();
        return apiConnection.requestTyped(request, new TypeToken<GetDastScanSettingResponse>() {
        }.getType());
    }

    public PutDastScanSetupResponse putDastOpenApiScanSettings(final Integer releaseId, PutDastAutomatedOpenApiReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        System.out.println("req content " + requestContent);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastOpenApiScanPutApi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);
        PutDastScanSetupResponse putDastScanSetupResponse = new PutDastScanSetupResponse();
        putDastScanSetupResponse = convertHttpResponseIntoDastApiResponse(response, putDastScanSetupResponse);
        return putDastScanSetupResponse;
    }

    public PutDastScanSetupResponse putDastGrpcScanSettings(final Integer releaseId, PutDastAutomatedGrpcReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastGrpcScanPutApi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);
        PutDastScanSetupResponse putDastScanSetupResponse = new PutDastScanSetupResponse();
        putDastScanSetupResponse = convertHttpResponseIntoDastApiResponse(response, putDastScanSetupResponse);
        return putDastScanSetupResponse;
    }

    public PutDastScanSetupResponse putDastGraphQLScanSettings(final Integer releaseId, PutDastAutomatedGraphQlReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastGraphQLScanPutApi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);
        PutDastScanSetupResponse putDastScanSetupResponse = new PutDastScanSetupResponse();
        putDastScanSetupResponse = convertHttpResponseIntoDastApiResponse(response, putDastScanSetupResponse);
        return putDastScanSetupResponse;
    }

    public PutDastScanSetupResponse putDastPostmanScanSettings(final Integer releaseId, PutDastAutomatedPostmanReqModel settings) throws IOException {

        String requestContent = Json.getInstance().toJson(settings);

        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format(DastPostmanScanPutApi, releaseId));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        ResponseContent response = apiConnection.request(request);
        PutDastScanSetupResponse putDastScanSetupResponse = new PutDastScanSetupResponse();
        putDastScanSetupResponse = convertHttpResponseIntoDastApiResponse(response, putDastScanSetupResponse);
        return putDastScanSetupResponse;
    }

    private <T> T convertHttpResponseIntoDastApiResponse(ResponseContent response, T fodApiResponse) throws IOException {
        if (response.code() < 300) {
            System.out.println("response code: " + response.code());
            return parseHttpSuccessResponse(response, fodApiResponse);

        } else {
            return parseFailureResponse(response, fodApiResponse);
        }
    }

    private <T> T parseHttpSuccessResponse(ResponseContent response, Object fodApiResponse) throws IOException {
        if (response.bodyContent().isEmpty()) {
            ((FodDastApiResponse) fodApiResponse).HttpCode = response.code();
            ((FodDastApiResponse) fodApiResponse).isSuccess = response.isSuccessful();
            ((FodDastApiResponse) fodApiResponse).reason = response.message();
            return (T) fodApiResponse;
        } else {
            return parseHttpBodyResponse(response, fodApiResponse);
        }
    }

    private <T> T parseFailureResponse(ResponseContent response, Object fodApiResponse) throws IOException {
        if (response.bodyContent() == null || response.bodyContent().isEmpty()) {
            ((FodDastApiResponse) fodApiResponse).isSuccess = false;
            ((FodDastApiResponse) fodApiResponse).HttpCode = response.code();
            ((FodDastApiResponse) fodApiResponse).reason = response.message();
            error err = new error();
            err.errorCode = response.code();
            err.message = response.message();
            ((FodDastApiResponse) fodApiResponse).errors = new ArrayList<>();
            ((FodDastApiResponse) fodApiResponse).errors.add(err);
            return (T) fodApiResponse;

        } else {
            T parsedResponse = parseHttpBodyResponse(response, fodApiResponse);
            error err = new error();
            err.errorCode = ((FodDastApiResponse) parsedResponse).HttpCode;
            err.message = ((FodDastApiResponse) parsedResponse).reason;
            ((FodDastApiResponse) parsedResponse).errors = new ArrayList<>();
            ((FodDastApiResponse) parsedResponse).errors.add(err);
            return parsedResponse;
        }
    }

    private <T> T parseHttpBodyResponse(ResponseContent response, Object fodApiResponse) throws IOException {

        if (fodApiResponse instanceof PatchDastFileUploadResponse) {
            T parsedResponse = apiConnection.parseResponse(response, new TypeToken<PatchDastFileUploadResponse>() {
            }.getType());
            ((PatchDastFileUploadResponse) parsedResponse).isSuccess = response.isSuccessful();
            ((PatchDastFileUploadResponse) parsedResponse).HttpCode = response.code();
            ((PatchDastFileUploadResponse) parsedResponse).reason = response.bodyContent();
            return parsedResponse;

        } else if (fodApiResponse instanceof PutDastScanSetupResponse) {
            T parsedResponse = apiConnection.parseResponse(response, new TypeToken<PutDastScanSetupResponse>() {
            }.getType());
            ((PutDastScanSetupResponse) parsedResponse).isSuccess = response.isSuccessful();
            ((PutDastScanSetupResponse) parsedResponse).HttpCode = response.code();
            ((PutDastScanSetupResponse) parsedResponse).reason = response.bodyContent();
            return parsedResponse;

        } else if (fodApiResponse instanceof PostDastStartScanResponse) {
            T parsedResponse = apiConnection.parseResponse(response, new TypeToken<PostDastStartScanResponse>() {
            }.getType());

            ((PostDastStartScanResponse) parsedResponse).isSuccess = response.isSuccessful();
            ((PostDastStartScanResponse) parsedResponse).HttpCode = response.code();
            ((PostDastStartScanResponse) parsedResponse).reason = response.bodyContent();
            return parsedResponse;

        } else {
            ((FodDastApiResponse) fodApiResponse).HttpCode = response.code();
            ((FodDastApiResponse) fodApiResponse).isSuccess = response.isSuccessful();
            ((FodDastApiResponse) fodApiResponse).reason = response.bodyContent();
            return (T) fodApiResponse;
        }
    }

}
