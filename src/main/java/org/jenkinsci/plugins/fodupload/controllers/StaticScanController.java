package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.IOUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericErrorResponse;
import org.jenkinsci.plugins.fodupload.models.response.PostStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseAssessmentTypeDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class StaticScanController extends ControllerBase {

    private final static int CHUNK_SIZE = 1024 * 1024;
    private PrintStream logger;

    /**
     * Constructor
     *
     * @param apiConnection apiConnection object with client info
     */
    public StaticScanController(final FodApiConnection apiConnection, final PrintStream logger) {
        super(apiConnection);
        this.logger = logger;
    }

    /**
     * Begin a static scan on FoD
     * <p>
     * // * @param uploadRequest zip file to upload
     *
     * @return true if the scan succeeded
     */
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "The intent of the catch-all is to make sure that the Jenkins user and logs show the plugin's problem in the build log.")
    public boolean startStaticScan(final JobModel uploadRequest) {

        PostStartScanResponse scanStartedResponse = null;

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
            // Get entitlement info
            ReleaseAssessmentTypeDTO assessment = new ReleaseController(apiConnection).getAssessmentType(uploadRequest);

            if (assessment == null) {
                logger.println("Assessment not found");
                return false;
            }

            // Build 'static' portion of url
            String fragUrl = apiConnection.getApiUrl() + "/api/v3/releases/" + uploadRequest.getBsiToken().getProjectVersionId() +
                    "/static-scans/start-scan?";
            fragUrl += "assessmentTypeId=" + uploadRequest.getBsiToken().getAssessmentTypeId();
            fragUrl += "&technologyStack=" + uploadRequest.getBsiToken().getTechnologyStack();
            fragUrl += "&entitlementId=" + assessment.getEntitlementId();
            fragUrl += "&entitlementFrequencyType=" + assessment.getFrequencyTypeId();
            fragUrl += "&isBundledAssessment=" + assessment.isBundledAssessment();
            if (assessment.getParentAssessmentTypeId() != 0 && assessment.isBundledAssessment())
                fragUrl += "&parentAssessmentTypeId=" + assessment.getParentAssessmentTypeId();
            if (uploadRequest.getBsiToken().getTechnologyVersion() != null)
                fragUrl += "&languageLevel=" + uploadRequest.getBsiToken().getLanguageLevel();
            fragUrl += "&doSonatypeScan=" + uploadRequest.getBsiToken().getIncludeOpenSourceAnalysis();



            fragUrl += "&excludeThirdPartyLibs=" + !uploadRequest.getBsiToken().getIncludeThirdParty();
            fragUrl += "&scanPreferenceType=" + uploadRequest.getBsiToken().getScanPreferenceId();
            fragUrl += "&auditPreferenceType=" + uploadRequest.getBsiToken().getAuditPreferenceId();

            // TODO: Figure out if we can submit the remediation scan
            fragUrl += "&isRemediationScan=" + uploadRequest.isRemediationScan();

            Gson gson = new Gson();

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
                        // Add offsets
                        .url(fragUrl + "&fragNo=" + fragmentNumber++ + "&offset=" + offset)
                        .post(RequestBody.create(byteArray, sendByteArray))
                        .build();

                // Get the response
                Response response = apiConnection.getClient().newCall(request).execute();

                if (response.code() == HttpStatus.SC_FORBIDDEN) {  // got logged out during polling so log back in
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
