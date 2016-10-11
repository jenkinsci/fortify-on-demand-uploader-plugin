package org.jenkinsci.plugins.fodupload.controllers;


import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.fodupload.FodApi;
import org.jenkinsci.plugins.fodupload.FodUploaderPlugin;
import org.jenkinsci.plugins.fodupload.models.JobConfigModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericErrorResponse;
import org.jenkinsci.plugins.fodupload.models.response.PostStartScanResponse;

import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class StaticScanController extends ControllerBase {
    private final int CHUNK_SIZE = 1024 * 1024;
    /**
     * Constructor
     * @param api api object with client info
     */
    public StaticScanController(final FodApi api) { super(api); }

    /**
     * Begin a static scan on FoD
     * @param uploadRequest zip file to upload
     * @return true if the scan succeeded
     */
    public boolean StartStaticScan(final JobConfigModel uploadRequest) {
        PrintStream logger = FodUploaderPlugin.getLogger();

        PostStartScanResponse scanStartedResponse = null;

        try(FileInputStream fs = new FileInputStream(uploadRequest.getUploadFile())) {
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            if (!uploadRequest.hasAssessmentTypeId() && !uploadRequest.hasTechnologyStack()) {
                return false;
            }

            // Build 'static' portion of url
            String fragUrl = api.getBaseUrl() + "/api/v3/releases/" + uploadRequest.getReleaseId() +
                    "/static-scans/start-scan?";
            fragUrl += "assessmentTypeId=" + uploadRequest.getAssessmentTypeId();
            fragUrl += "&technologyStack=" + uploadRequest.getTechnologyStack();
            fragUrl += "&entitlementId=6";
            fragUrl += "&entitlementFrequencyType=1";

            if (uploadRequest.hasLanguageLevel())
                fragUrl += "&languageLevel=" + uploadRequest.getLanguageLevel();
            if (uploadRequest.getIsExpressScan())
                fragUrl += "&scanPreferenceId=2";
            if (uploadRequest.getIsExpressAudit())
                fragUrl += "&auditPreferenceId=2";
            if (uploadRequest.getRunOpenSourceAnalysis())
                fragUrl += "&doSonatypeScan=" + uploadRequest.getRunOpenSourceAnalysis();
            if (uploadRequest.getIsRemediationScan())
                fragUrl += "&isRemediationScan=" + uploadRequest.getIsRemediationScan();
            if (uploadRequest.getIncludeThirdParty())
                fragUrl += "&excludeThirdPartyLibs=" + !uploadRequest.getIncludeThirdParty();

            // Loop through chunks
            while ((byteCount = fs.read(readByteArray)) != -1) {
                if (byteCount < CHUNK_SIZE) {
                    fragmentNumber = -1;
                    sendByteArray = Arrays.copyOf(readByteArray, byteCount);
                } else {
                    sendByteArray = readByteArray;
                }

                MediaType byteArray = MediaType.parse("application/octet-stream");
                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + api.getToken())
                        .addHeader("Content-Type", "application/octet-stream")
                        // Add offsets
                        .url(fragUrl + "&fragNo=" + fragmentNumber++ + "&offset=" + offset)
                        .post(RequestBody.create(byteArray, sendByteArray))
                        .build();

                // Get the response
                Response response = api.getClient().newCall(request).execute();

                if (fragmentNumber != 0 && fragmentNumber % 5 == 0) {
                    logger.println("Upload Status - Bytes sent:" + offset);
                }
                // Read the results and close the response
                String finalResponse = IOUtils.toString(response.body().byteStream(), "utf-8");
                response.body().close();

                Gson gson = new Gson();
                // Scan successfully uploaded
                if (response.isSuccessful()) {
                    scanStartedResponse = gson.fromJson(finalResponse, PostStartScanResponse.class);
                    // There was an error along the lines of 'another scan in progress' or something
                } else {
                    GenericErrorResponse errors = gson.fromJson(finalResponse, GenericErrorResponse.class);
                    logger.println("Package upload failed for the following reasons: " +
                            errors.toString());
                    break;
                }
                offset += byteCount;
            }
            if (scanStartedResponse != null) {
                logger.println("Scan " + scanStartedResponse.getScanId() +
                        " uploaded successfully. Total bytes sent: " + offset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scanStartedResponse != null;
    }
}