package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.IOUtils;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class StaticScanController extends ControllerBase {

    private final static int EXPRESS_SCAN_PREFERENCE_ID = 2;
    private final static int EXPRESS_AUDIT_PREFERENCE_ID = 2;
    private final static int MAX_NOTES_LENGTH = 250;
    private final static int CHUNK_SIZE = 1024 * 1024;

    /**
     * Constructor
     *
     * @param apiConnection apiConnection object with client info
     * @param logger        logger object to display to console
     * @param correlationId correlation id
     */
    public StaticScanController(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        super(apiConnection, logger, correlationId);
    }

    /**
     * Begin a static scan on FoD
     *
     * @param releaseId     id of release being targeted
     * @param uploadRequest zip file to upload
     * @param notes         notes
     * @return true if the scan succeeded
     */
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "The intent of the catch-all is to make sure that the Jenkins user and logs show the plugin's problem in the build log.")
    public StartScanResponse startStaticScan(Integer releaseId, final JobModel uploadRequest, final String notes) {
        PostStartScanResponse scanStartedResponse = null;
        StartScanResponse scanResults = new StartScanResponse();

        File uploadFile = uploadRequest.getPayload();
        try (FileInputStream fs = new FileInputStream(uploadFile)) {
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            if (apiConnection.getToken() == null) apiConnection.authenticate();

            println("Getting Assessment");

            String projectVersion = "Not Found";
            try (InputStream inputStream = this.getClass().getResourceAsStream("/application.properties")) {
                Properties props = new Properties();
                props.load(inputStream);
                projectVersion = props.getProperty("application.version", "Not Found");
            }

            HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder();

            if (Utils.isNullOrEmpty(uploadRequest.getSelectedReleaseType())) {
                if (uploadRequest.getIsPipeline()) {
                    if (releaseId == null || releaseId < 1) releaseId = createApplicationAndRelease(uploadRequest);
                    buildPipelineRequest(builder, releaseId, uploadRequest);
                } else throw new IllegalArgumentException("Invalid job model");
            } else {
                FodEnums.SelectedReleaseType type = FodEnums.SelectedReleaseType.valueOf(uploadRequest.getSelectedReleaseType());

                switch (type) {
                    case UseReleaseId:
                    case UseAppAndReleaseName:
                        buildReleaseSettingsRequest(builder, releaseId, uploadRequest);
                        break;
                    case UseBsiToken:
                        buildBsiRequest(builder, uploadRequest);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid job model");
                }
            }

            builder.addQueryParameter("inProgressScanActionType", uploadRequest.getInProgressScanActionType())
                    .addQueryParameter("purchaseEntitlement", Boolean.toString(uploadRequest.isPurchaseEntitlements()))
                    .addQueryParameter("inProgressScanActionType", uploadRequest.getInProgressScanActionType())
                    .addQueryParameter("scanMethodType", "CICD")
                    .addQueryParameter("scanTool", "Jenkins")
                    .addQueryParameter("scanToolVersion", projectVersion);

            if (!Utils.isNullOrEmpty(notes)) {
                String truncatedNotes = StringUtils.left(notes, MAX_NOTES_LENGTH);
                builder.addQueryParameter("notes", truncatedNotes);
            }
            // TODO: Come back and fix the request to set fragNo and offset query parameters
            String fragUrl = builder.build().toString();

            // Loop through chunks

            println("TOTAL FILE SIZE = " + uploadFile.length());
            println("CHUNK_SIZE = " + CHUNK_SIZE);

            while ((byteCount = fs.read(readByteArray)) != -1) {

                if (byteCount < CHUNK_SIZE) {
                    sendByteArray = Arrays.copyOf(readByteArray, byteCount);
                    fragmentNumber = -1;
                } else {
                    sendByteArray = readByteArray;
                }

                MediaType byteArray = MediaType.parse("application/octet-stream");
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                        .addHeader("Content-Type", "application/octet-stream")
                        .addHeader("Accept", "application/json")
                        .addHeader("CorrelationId", getCorrelationId())
                        // Add offsets
                        .url(fragUrl + "&fragNo=" + fragmentNumber++ + "&offset=" + offset)
                        .post(RequestBody.create(byteArray, sendByteArray))
                        .build();

                println("Uploading fragment " + fragmentNumber);
                // Get the response
                Response response = apiConnection.getClient().newCall(request).execute();

                if (response.code() == HttpStatus.SC_FORBIDDEN || response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                    String raw = apiConnection.getRawBody(response);

                    if (Utils.isNullOrEmpty(raw)) println("Uploading fragment failed, reauthenticating");
                    else println("Uploading fragment failed, reauthenticating \n" + raw);
                    // Re-authenticate
                    apiConnection.authenticate();
                    continue;
                }

                offset += byteCount;

                if (fragmentNumber % 5 == 0) {
                    println("Upload Status - Fragment No: " + fragmentNumber + ", Bytes sent: " + offset
                            + " (Response: " + response.code() + ")");
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    Gson gson = new Gson();
                    // final response has 200, try to deserialize it
                    if (response.code() == 200) {

                        scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);
                        println("Scan " + scanStartedResponse.getScanId() + " uploaded successfully. Total bytes sent: " + offset);
                        scanResults.uploadSuccessfulScanStarting(scanStartedResponse.getScanId());
                        return scanResults;

                    } else if (!response.isSuccessful()) { // There was an error along the lines of 'another scan in progress' or something
                        println("An error occurred during the upload.");
                        GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);

                        if (errors != null) {
                            if (errors.toString().contains("Can not start scan another scan is in progress")) {
                                scanResults.uploadSuccessfulScanNotStarted();
                            } else {
                                println("Package upload failed for the following reasons: ");
                                println(errors.toString());
                                scanResults.uploadNotSuccessful();
                            }
                        } else {
                            if (!Utils.isNullOrEmpty(responseJsonStr)) println("Raw response\n" + responseJsonStr);
                            else println("No response body from api");
                            scanResults.uploadNotSuccessful();
                        }

                        return scanResults; // if there is an error, get out of loop and mark build unstable
                    }
                }
                response.body().close();

            } // end while
            println("Payload upload complete");
        } catch (Exception e) {
            printStackTrace(e);
            scanResults.uploadNotSuccessful();
            return scanResults;
        }

        scanResults.uploadNotSuccessful();
        return scanResults;
    }

    private void buildBsiRequest(final HttpUrl.Builder builder, final JobModel uploadRequest) {
        builder
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced", uploadRequest.getBsiToken().getReleaseId()))
                .addQueryParameter("entitlementPreferenceType", uploadRequest.getEntitlementPreference())
                .addQueryParameter("remdiationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType())
                .addQueryParameter("bsiToken", uploadRequest.getBsiTokenOriginal());
    }

    private void buildReleaseSettingsRequest(final HttpUrl.Builder builder, final Integer releaseId, final JobModel uploadRequest) {
        builder
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced-with-defaults", releaseId))
                .addQueryParameter("remediationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType());
    }

    private void buildPipelineRequest(final HttpUrl.Builder builder, final Integer releaseId, final JobModel uploadRequest) {
        builder.addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced-with-defaults", releaseId));

        if (!Utils.isNullOrEmpty(uploadRequest.getAssessmentType())) builder.addQueryParameter("assessmentTypeId", uploadRequest.getAssessmentType());

        if (!Utils.isNullOrEmpty(uploadRequest.getEntitlementId())) builder.addQueryParameter("entitlementId", uploadRequest.getEntitlementId());

        if (!Utils.isNullOrEmpty(uploadRequest.getFrequencyId()))
            builder.addQueryParameter("entitlementFrequencyType", uploadRequest.getFrequencyId());

        if (!Utils.isNullOrEmpty(uploadRequest.getOpenSourceScan())) builder.addQueryParameter("doSonatypeScan", uploadRequest.getOpenSourceScan());

        if (!Utils.isNullOrEmpty(uploadRequest.getAuditPreference()))
            builder.addQueryParameter("auditPreferenceType", uploadRequest.getAuditPreference());

        if (!Utils.isNullOrEmpty(uploadRequest.getTechnologyStack()))
            builder.addQueryParameter("technologyTypeId", uploadRequest.getTechnologyStack());

        if (!Utils.isNullOrEmpty(uploadRequest.getLanguageLevel()))
            builder.addQueryParameter("technologyVersionTypeId", uploadRequest.getLanguageLevel());

        if (!Utils.isNullOrEmpty(uploadRequest.getRemediationScanPreferenceType()))
            builder.addQueryParameter("remediationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType());
    }

    private Integer createApplicationAndRelease(final JobModel job) throws Exception {
        PostReleaseWithUpsertApplicationModel model = new PostReleaseWithUpsertApplicationModel();

        model.setApplicationName(job.getApplicationName());
        model.setApplicationType(job.getApplicationType());
        model.setReleaseName(job.getReleaseName());
        model.setOwnerId(job.getOwner());
        model.setBusinessCriticalityType(job.getBusinessCriticality());
        model.setSdlcStatusType(job.getSdlcStatus());

        if (job.getIsMicroservice()) {
            model.setHasMicroservices(job.getIsMicroservice());
            model.setReleaseMicroserviceName(job.getMicroserviceName());
        }

        if (!job.getAttributes().isEmpty()) {
            String[] attrSpl = job.getAttributes().split("\\&");
            Map<String, String> attrs = new HashMap<>();

            for (String a : attrSpl) {
                String[] kvSpl = a.split(":");

                if (kvSpl.length == 2 && !attrs.containsKey(kvSpl[0])) attrs.put(kvSpl[0], kvSpl[1]);
                else throw new Exception("Failure parsing application attributes");
            }

            for (FodAttributeMapItem a : MapAttributesToFod(attrs)) {
                model.getAttributes().add(new ApplicationAttributeModel(a.getDefinition().getId(), a.getValue()));
            }
        }

        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments("/api/v3/releases/releaseWithUpsertApplication");

        String requestContent = Json.getInstance().toJson(model);
        Request request = new Request.Builder()
                .url(builder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        Response response = apiConnection.request(request);

        if (response.code() < 300) {
            PostReleaseWithUpsertApplicationResponseModel result = apiConnection.parseResponse(response, new TypeToken<PostReleaseWithUpsertApplicationResponseModel>() {
            }.getType());

            if (result.getSuccess()) return result.getReleaseId();
            else throw new Exception("Failed to create application and/or release: \n" + String.join("\n", result.getErrors()));
        } else {
            throw new Exception("Failed to create application and/or release: \n" + apiConnection.getRawBody(response));
        }
    }

    public List<FodAttributeMapItem> MapAttributesToFod(Map<String, String> attributes) throws IOException {
        // Todo: this should be injected
        AttributesController attrCntr = new AttributesController(apiConnection, logger, correlationId);
        List<AttributeDefinition> fodAttr = attrCntr.getAttributeDefinitions();
        List<FodAttributeMapItem> result = new ArrayList<>();

        for (Map.Entry<String, String> a : attributes.entrySet()) {
            for (AttributeDefinition fa : fodAttr) {
                if (a.getKey().equals(fa.getName())) {
                    result.add(new FodAttributeMapItem(a.getKey(), a.getValue(), fa));
                    break;
                }
            }
        }

        return result;
    }

    /**
     * @deprecated Use the {@link StaticScanController#getStaticScanSettings} method instead
     */
    @Deprecated
    public GetStaticScanSetupResponse getStaticScanSettingsOld(final Integer releaseId) throws IOException {
        if (apiConnection.getToken() == null)
            apiConnection.authenticate();

        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/scan-setup", releaseId));

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiConnection.getToken())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();
        Response response = apiConnection.getClient().newCall(request).execute();

        if (!response.isSuccessful()) {
            return null;
        }

        String content = IOUtils.toString(response.body().byteStream(), "utf-8");
        response.body().close();

        Gson gson = new Gson();
        Type t = new TypeToken<GetStaticScanSetupResponse>() {
        }.getType();
        GetStaticScanSetupResponse result = gson.fromJson(content, t);

        return result;
    }

    public GetStaticScanSetupResponse getStaticScanSettings(final Integer releaseId) throws IOException {
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/scan-setup", releaseId));

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();

        return apiConnection.requestTyped(request, new TypeToken<GetStaticScanSetupResponse>() {
        }.getType());
    }

    public PutStaticScanSetupResponse putStaticScanSettings(final Integer releaseId, PutStaticScanSetupModel settings) throws IOException {
        String requestContent = Json.getInstance().toJson(settings);
        HttpUrl.Builder urlBuilder = apiConnection.urlBuilder()
                .addPathSegments("/api/v3/releases/" + releaseId + "/static-scans/scan-setup");
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .put(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();
        Response response = apiConnection.request(request);

        if (response.code() < 300) {
            return apiConnection.parseResponse(response, new TypeToken<PutStaticScanSetupResponse>() {
            }.getType());
        } else {
            return new PutStaticScanSetupResponse(false, null, Utils.unexpectedServerResponseErrors(), null);
        }
    }
}
