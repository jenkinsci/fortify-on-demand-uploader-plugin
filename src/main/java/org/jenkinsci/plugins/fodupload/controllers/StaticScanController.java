package org.jenkinsci.plugins.fodupload.controllers;

import com.fortify.fod.parser.BsiToken;
import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.IOUtils;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.response.GenericErrorResponse;
import org.jenkinsci.plugins.fodupload.models.response.PostStartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseAssessmentTypeDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class StaticScanController extends ControllerBase {

    private final static int EXPRESS_SCAN_PREFERENCE_ID = 2;
    private final static int EXPRESS_AUDIT_PREFERENCE_ID = 2;
    private final static int MAX_NOTES_LENGTH = 250;
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
    public boolean startStaticScan(final JobModel uploadRequest, final String notes) {

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
            ReleaseAssessmentTypeDTO assessmentType = new ReleaseController(apiConnection).getAssessmentType(uploadRequest);

            if (assessmentType == null) {
                logger.println("Entitlement not found.  Please make sure that your entitlements are good for the selected preference.");
                return false;
            }

            BsiToken token = uploadRequest.getBsiToken();
            boolean isRemediationScan = uploadRequest.isRemediationPreferred() && assessmentType.isRemediation();

            // TODO: remove these override options once legacy BSI URL is no longer present in FoD
            boolean excludeThirdPartyLibs = !token.getIncludeThirdParty() || !uploadRequest.isIncludeThirdPartyOverride();
            boolean includeOpenSourceScan = token.getIncludeOpenSourceAnalysis() || uploadRequest.isRunOpenSourceAnalysisOverride();
            int scanPreferenceId = uploadRequest.isExpressScanOverride() ? EXPRESS_SCAN_PREFERENCE_ID : token.getScanPreferenceId();
            int auditPreferenceId = uploadRequest.isExpressAuditOverride() ? EXPRESS_AUDIT_PREFERENCE_ID : token.getAuditPreferenceId();

            HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                    .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan", token.getProjectVersionId()))
                    .addQueryParameter("assessmentTypeId", Integer.toString(token.getAssessmentTypeId()))
                    .addQueryParameter("technologyStack", token.getTechnologyType())
                    .addQueryParameter("entitlementId", Integer.toString(assessmentType.getEntitlementId()))
                    .addQueryParameter("entitlementFrequencyType", Integer.toString(assessmentType.getFrequencyTypeId()))
                    .addQueryParameter("isBundledAssessment", Boolean.toString(assessmentType.isBundledAssessment()))
                    .addQueryParameter("doSonatypeScan", Boolean.toString(includeOpenSourceScan))
                    .addQueryParameter("excludeThirdPartyLibs", Boolean.toString(excludeThirdPartyLibs))
                    .addQueryParameter("scanPreferenceType", Integer.toString(scanPreferenceId))
                    .addQueryParameter("auditPreferenceType", Integer.toString(auditPreferenceId))
                    .addQueryParameter("isRemediationScan", Boolean.toString(isRemediationScan));

            if (!Utils.isNullOrEmpty(notes)) {
                String truncatedNotes = StringUtils.left(notes, MAX_NOTES_LENGTH);
                builder = builder.addQueryParameter("notes", truncatedNotes);
            }

            if (assessmentType.getParentAssessmentTypeId() != 0 && assessmentType.isBundledAssessment()) {
                builder = builder.addQueryParameter("parentAssessmentTypeId", Integer.toString(assessmentType.getParentAssessmentTypeId()));
            }

            if (token.getTechnologyVersion() != null) {
                builder = builder.addQueryParameter("languageLevel", token.getTechnologyVersion());
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

                    Gson gson = new Gson();
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
