package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.IOUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.jenkinsci.plugins.fodupload.FodApi;
import org.jenkinsci.plugins.fodupload.FodUploaderPlugin;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericErrorResponse;
import org.jenkinsci.plugins.fodupload.models.response.PostStartScanResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class StaticScanController extends ControllerBase {

    private final static int CHUNK_SIZE = 1024 * 1024;

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
    // * @param uploadRequest zip file to upload
     * @return true if the scan succeeded
     */
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "The intent of the catch-all is to make sure that the Jenkins user and logs show the plugin's problem in the build log.")
    public boolean startStaticScan(final JobModel uploadRequest) {
        PrintStream logger = FodUploaderPlugin.getLogger();

        PostStartScanResponse scanStartedResponse = null;

        File uploadFile = uploadRequest.getPayload();
        try (FileInputStream fs = new FileInputStream(uploadFile)) {
            byte[] readByteArray = new byte[CHUNK_SIZE];
            byte[] sendByteArray;
            int fragmentNumber = 0;
            int byteCount;
            long offset = 0;

            if (api.getToken() == null)
                api.authenticate();

            if (!uploadRequest.getBsiUrl().hasAssessmentTypeId() && !uploadRequest.getBsiUrl().hasTechnologyStack()) {
                logger.println("Missing Assessment Type or Technology Stack.");
                return false;
            }

            // Build 'static' portion of url
            String fragUrl = api.getBaseUrl() + "/api/v3/releases/" + uploadRequest.getBsiUrl().getProjectVersionId() +
                    "/static-scans/start-scan?";
            fragUrl += "assessmentTypeId=" + uploadRequest.getBsiUrl().getAssessmentTypeId();
            fragUrl += "&technologyStack=" + uploadRequest.getBsiUrl().getTechnologyStack();
            fragUrl += "&entitlementId=" + 6;
            fragUrl += "&entitlementFrequencyType=" + 1;

            if (uploadRequest.getBsiUrl().hasLanguageLevel())
                fragUrl += "&languageLevel=" + uploadRequest.getBsiUrl().getLanguageLevel();
            if (uploadRequest.isExpressScan())
                fragUrl += "&scanPreferenceType=2";
            if (uploadRequest.isExpressAudit())
                fragUrl += "&auditPreferenceType=2";
            if (uploadRequest.isRunOpenSourceAnalysis())
                fragUrl += "&doSonatypeScan=" + uploadRequest.isRunOpenSourceAnalysis();
            if (uploadRequest.isRemediationScan())
                fragUrl += "&isRemediationScan=" + uploadRequest.isRemediationScan();
            if (uploadRequest.isExcludeThirdParty())
                fragUrl += "&excludeThirdPartyLibs=" + uploadRequest.isExcludeThirdParty();

            Gson gson = new Gson();

            // Loop through chunks

            logger.println("TOTAL FILE SIZE = " + uploadFile.length());
            logger.println("CHUNK_SIZE = " + CHUNK_SIZE);

            logger.println(fragUrl);
            logger.println(uploadRequest.toString());

            while ((byteCount = fs.read(readByteArray)) != -1) {

                if (byteCount < CHUNK_SIZE) {
                    sendByteArray = Arrays.copyOf(readByteArray, byteCount);
                    fragmentNumber = -1;
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

                offset += byteCount;

                if (fragmentNumber % 5 == 0) {
                    logger.println("Upload Status - Fragment No: " + fragmentNumber + ", Bytes sent: " + offset
                            + " (Response: " + response.code() + ")");
                }

                if (response.code() != 202) {
                    String responseJsonStr = IOUtils.toString(response.body().byteStream(), "utf-8");

                    // final response has 200, try to deserialize it
                    if (response.code() == 200) {

                        scanStartedResponse = gson.fromJson(responseJsonStr, PostStartScanResponse.class);
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

            } // end while
        } catch (Exception e) {
            e.printStackTrace(logger);
            return false;
        }

        return false;
    }
}
