package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.IOUtils;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.BsiToken;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericErrorResponse;
import org.jenkinsci.plugins.fodupload.models.response.PostStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.StartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.StaticScanSetupResponse;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Properties;

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
     * @param releaseId     id of release being targeted
     * @param staticScanSettings config information for scan
     * @param uploadRequest zip file to upload
     * @param notes         notes
     * @return true if the scan succeeded
     */
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "The intent of the catch-all is to make sure that the Jenkins user and logs show the plugin's problem in the build log.")
    public StartScanResponse startStaticScan(final Integer releaseId, final StaticScanSetupResponse staticScanSettings, final JobModel uploadRequest, final String notes) {

        PostStartScanResponse scanStartedResponse = null;
        StartScanResponse scanResults = new StartScanResponse();

        File uploadFile = uploadRequest.getPayload();
        try (FileInputStream fs = new FileInputStream(uploadFile)) {
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            if (apiConnection.getToken() == null)
                apiConnection.authenticate();

            logger.println("Getting Assessment");

            BsiToken token = null;
            if (releaseId == 0) {
                token = uploadRequest.getBsiToken();
            }

            String projectVersion;
            try (InputStream inputStream = this.getClass().getResourceAsStream("/application.properties")) {
                Properties props = new Properties();
                props.load(inputStream);
                projectVersion = props.getProperty("application.version", "Not Found");
            }

            HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                    .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced", releaseId != 0 ? releaseId : token.getProjectVersionId()))
                    .addQueryParameter("technologyStack", releaseId == 0 ? token.getTechnologyType() : staticScanSettings.getTechnologyStack())
                    .addQueryParameter("entitlementPreferenceType", uploadRequest.getEntitlementPreference())
                    .addQueryParameter("purchaseEntitlement", Boolean.toString(uploadRequest.isPurchaseEntitlements()))
                    .addQueryParameter("remdiationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType())
                    .addQueryParameter("inProgressScanActionType", uploadRequest.getInProgressScanActionType())
                    .addQueryParameter("scanMethodType", "CICD")
                    .addQueryParameter("scanTool", "Jenkins")
                    .addQueryParameter("scanToolVersion", projectVersion != null ? projectVersion : "NotFound");

            if (releaseId == 0) {
                builder = builder.addQueryParameter("bsiToken", uploadRequest.getBsiTokenOriginal());
            }

            if (!Utils.isNullOrEmpty(notes)) {
                String truncatedNotes = StringUtils.left(notes, MAX_NOTES_LENGTH);
                builder = builder.addQueryParameter("notes", truncatedNotes);
            }

            if ((releaseId == 0 ? token.getTechnologyVersion() : staticScanSettings.getLanguageLevel()) != null) {
                builder = builder.addQueryParameter("languageLevel", releaseId == 0 ? token.getTechnologyVersion() : staticScanSettings.getLanguageLevel());
            }

            // TODO: Come back and fix the request to set fragNo and offset query parameters
            String fragUrl = builder.build().toString();

            // Loop through chunks

            logger.println("TOTAL FILE SIZE = " + uploadFile.length());
            logger.println("CHUNK_SIZE = " + CHUNK_SIZE);

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

                // Get the response
                Response response = apiConnection.getClient().newCall(request).execute();

                if (response.code() == HttpStatus.SC_FORBIDDEN || response.code() == HttpStatus.SC_UNAUTHORIZED) {  // got logged out during polling so log back in
                    // Re-authenticate
                    apiConnection.authenticate();

                    // if you had to reauthenticate here, would the loop and request not need to be resubmitted?
                    // possible continue?
                }

                offset += byteCount;

                if (fragmentNumber % 5 == 0) {
                    logger.println("Upload Status - Fragment No: " + fragmentNumber + ", Bytes sent: " + offset
                            + " (Response: " + response.code() + ")");
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    Gson gson = new Gson();
                    // final response has 200, try to deserialize it
                    if (response.code() == 200) {

                        scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);
                        logger.println("Scan " + scanStartedResponse.getScanId() + " uploaded successfully. Total bytes sent: " + offset);
                        scanResults.uploadSuccessfulScanStarting(scanStartedResponse.getScanId());
                        return scanResults;

                    } else if (!response.isSuccessful()) { // There was an error along the lines of 'another scan in progress' or something

                        logger.println("An error occurred during the upload.");
                        GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
                        if (errors != null) {
                            if(errors.toString().contains("Can not start scan another scan is in progress")) {
                                scanResults.uploadSuccessfulScanNotStarted();
                            }
                            else {
                                logger.println("Package upload failed for the following reasons: ");
                                logger.println(errors.toString());
                                scanResults.uploadNotSuccessful();
                            }
                        }
                            
                        
                        return scanResults; // if there is an error, get out of loop and mark build unstable
                    }
                }
                response.body().close();

            } // end while

        } catch (Exception e) {
            e.printStackTrace(logger);
            scanResults.uploadNotSuccessful();
            return scanResults;
        }

        scanResults.uploadNotSuccessful();
        return scanResults;
    }

    public StaticScanSetupResponse getStaticScanSettings(final Integer releaseId) throws IOException {
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
        Type t = new TypeToken<StaticScanSetupResponse>() {}.getType();
        StaticScanSetupResponse result = gson.fromJson(content, t);

        return result;
    }
}
