package org.jenkinsci.plugins.fodupload.controllers;


import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
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
     *
     * @param api api object with client info
     */
    public StaticScanController(final FodApi api) {
        super(api);
    }

    /**
     * Begin a static scan on FoD
     *
     * @param uploadRequest zip file to upload
     * @return true if the scan succeeded
     */
    public boolean StartStaticScan(final JobConfigModel uploadRequest) {
        PrintStream logger = FodUploaderPlugin.getLogger();

        PostStartScanResponse scanStartedResponse = null;

        try (FileInputStream fs = new FileInputStream(uploadRequest.getUploadFile())) {
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            if (api.getToken() == null)
                api.authenticate();

            if (!uploadRequest.hasAssessmentTypeId() && !uploadRequest.hasTechnologyStack()) {
                logger.println("Missing Assessment Type or Technology Stack.");
                return false;
            }

            // Build 'static' portion of url
            String fragUrl = api.getBaseUrl() + "/api/v3/releases/" + uploadRequest.getReleaseId() +
                    "/static-scans/start-scan?";
            fragUrl += "assessmentTypeId=" + uploadRequest.getAssessmentTypeId();
            fragUrl += "&technologyStack=" + uploadRequest.getTechnologyStack();
            fragUrl += "&entitlementId=" + uploadRequest.getEntitlementId();
            fragUrl += "&entitlementFrequencyType=" + uploadRequest.getEntitlementFrequencyTypeId();

            if (uploadRequest.hasLanguageLevel())
                fragUrl += "&languageLevel=" + uploadRequest.getLanguageLevel();
            if (uploadRequest.getIsExpressScan())
                fragUrl += "&scanPreferenceType=2";
            if (uploadRequest.getIsExpressAudit())
                fragUrl += "&auditPreferenceType=2";
            if (uploadRequest.getRunOpenSourceAnalysis())
                fragUrl += "&doSonatypeScan=" + uploadRequest.getRunOpenSourceAnalysis();
            if (uploadRequest.getIsRemediationScan())
                fragUrl += "&isRemediationScan=" + uploadRequest.getIsRemediationScan();
            if (uploadRequest.getExcludeThirdParty())
                fragUrl += "&excludeThirdPartyLibs=" + uploadRequest.getExcludeThirdParty();

            Gson gson = new Gson();

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

                if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
                    // Re-authenticate
                    api.authenticate();

                    // if you had to reauthenticate here, would the loop and request not need to be resubmitted?
                    // possible continue?
                }

                if (fragmentNumber % 5 == 0) {
                    logger.println("Upload Status - Fragment No: " + fragmentNumber + ", Bytes sent:" + offset
                            + " (Response: " + response.code() + ")");
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    // final response has 200, try to deserialize it
                    if (response.code() == 200) {
                        scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);

                        logger.println("Scan started response: " + responseJsonStr);
                        logger.println("Scan " + scanStartedResponse.getScanId() + " uploaded successfully. Total bytes sent: " + offset);

                        return true;

                    } else if (!response.isSuccessful()) { // There was an error along the lines of 'another scan in progress' or something

                        logger.println("An error occurred during the upload.");
                        GenericErrorResponse errors = gson.fromJson(responseJsonStr, GenericErrorResponse.class);
                        if (errors != null)
                            logger.println("Package upload failed for the following reasons: " + errors.toString());
                        return false; // if there is an error, get out of loop and mark build unstable
                    }
                }

                response.body().close();
                offset += byteCount;
            } // end while

        } catch (Exception e) {
            logger.println(e.getStackTrace());
            return false;
        }

        return false;
    }
}
