package org.jenkinsci.plugins.fodupload.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.FodApi.ResponseContent;
import org.jenkinsci.plugins.fodupload.FodApi.ScanPayloadUpload;
import org.jenkinsci.plugins.fodupload.Json;
import org.jenkinsci.plugins.fodupload.Utils;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.*;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StaticScanController extends ControllerBase {

    private final static int EXPRESS_SCAN_PREFERENCE_ID = 2;
    private final static int EXPRESS_AUDIT_PREFERENCE_ID = 2;
    private final static int MAX_NOTES_LENGTH = 250;

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
     *                      at_return true if the scan succeeded
     */
    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION", justification = "The intent of the catch-all is to make sure that the Jenkins user and logs show the plugin's problem in the build log.")
    public StartScanResponse startStaticScan(Integer releaseId, final SastJobModel uploadRequest, final String notes) {
        try {
            println("Getting Assessment");

            String projectVersion = "Not Found";
            try (InputStream inputStream = this.getClass().getResourceAsStream("/application.properties")) {
                Properties props = new Properties();
                props.load(inputStream);
                projectVersion = props.getProperty("application.version", "Not Found");
            }

            HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder();

            if (org.jenkinsci.plugins.fodupload.Utils.isNullOrEmpty(uploadRequest.getSelectedReleaseType())) {
                if (uploadRequest.getIsPipeline()) {
                    if (!org.jenkinsci.plugins.fodupload.Utils.isNullOrEmpty(uploadRequest.getBsiTokenOriginal()) && releaseId <= 0) {
                        buildBsiRequest(builder, uploadRequest);
                    } else if ((releaseId == null || releaseId < 1)) releaseId = upsertApplicationAndRelease(uploadRequest);
                    if (releaseId > 0) buildPipelineRequest(builder, releaseId, uploadRequest);
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

            if (!org.jenkinsci.plugins.fodupload.Utils.isNullOrEmpty(notes)) {
                String truncatedNotes = StringUtils.left(notes, MAX_NOTES_LENGTH);
                builder.addQueryParameter("notes", truncatedNotes);
            }

            ScanPayloadUpload upload = apiConnection.getScanPayloadUploadInstance(uploadRequest, correlationId, builder.build().toString(), this.logger);

            return upload.performUpload();
        } catch (Exception e) {
            printStackTrace(e);
            StartScanResponse scanResults = new StartScanResponse();

            scanResults.uploadNotSuccessful();
            return scanResults;
        }
    }

    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Utils.getLogTimestampFormat());

    private String getLogTimestamp() {
        return dateFormat.format(LocalDateTime.now());
    }

    private void buildBsiRequest(final HttpUrl.Builder builder, final SastJobModel uploadRequest) {
        builder
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced", uploadRequest.getBsiToken().getReleaseId()))
                .addQueryParameter("entitlementPreferenceType", uploadRequest.getEntitlementPreference())
                .addQueryParameter("remdiationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType())
                .addQueryParameter("bsiToken", uploadRequest.getBsiTokenOriginal());
    }

    private void buildReleaseSettingsRequest(final HttpUrl.Builder builder, final Integer releaseId, final SastJobModel uploadRequest) {
        builder
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced-with-defaults", releaseId))
                .addQueryParameter("remediationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType());
    }

    private void buildPipelineRequest(final HttpUrl.Builder builder, final Integer releaseId, final SastJobModel uploadRequest) {
        builder.addPathSegments(String.format("/api/v3/releases/%d/static-scans/start-scan-advanced-with-defaults", releaseId));

        if (!Utils.isNullOrEmpty(uploadRequest.getAssessmentType())) builder.addQueryParameter("assessmentTypeId", uploadRequest.getAssessmentType());

        if (!Utils.isNullOrEmpty(uploadRequest.getEntitlementId())) builder.addQueryParameter("entitlementId", uploadRequest.getEntitlementId());

        if (!Utils.isNullOrEmpty(uploadRequest.getFrequencyId()))
            builder.addQueryParameter("entitlementFrequencyType", uploadRequest.getFrequencyId());

        if (!Utils.isNullOrEmpty(uploadRequest.getOpenSourceScan()))
            builder.addQueryParameter("doSonatypeScan", Utils.isNullOrEmpty(uploadRequest.getOpenSourceScan()) ? "false" : uploadRequest.getOpenSourceScan());

        if (!Utils.isNullOrEmpty(uploadRequest.getAuditPreference()))
            builder.addQueryParameter("auditPreferenceType", uploadRequest.getAuditPreference());

        if (!Utils.isNullOrEmpty(uploadRequest.getTechnologyStack()))
            builder.addQueryParameter("technologyTypeId", uploadRequest.getTechnologyStack());

        if (!Utils.isNullOrEmpty(uploadRequest.getLanguageLevel()))
            builder.addQueryParameter("technologyVersionTypeId", uploadRequest.getLanguageLevel());

        if (!Utils.isNullOrEmpty(uploadRequest.getRemediationScanPreferenceType()))
            builder.addQueryParameter("remediationScanPreferenceType", uploadRequest.getRemediationScanPreferenceType());
    }

    private Integer upsertApplicationAndRelease(final SastJobModel job) throws Exception {
        ReleaseController relCntr = new ReleaseController(apiConnection, logger, correlationId);
        Integer releaseId = relCntr.getReleaseIdByName(job.getApplicationName(), job.getReleaseName(), job.getIsMicroservice(), job.getMicroserviceName());

        if (releaseId != null) {
            println("Existing release found matching " + job.getApplicationName() + " " + job.getReleaseName());
            return releaseId;
        }

        println("Provisioning application and release");

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
            ArrayList<String> microserviceArray = new ArrayList<>();
            microserviceArray.add(job.getMicroserviceName());
            model.setMicroservices(microserviceArray);
        }

        if (!Utils.isNullOrEmpty(job.getAttributes())) {
            String[] attrSpl = job.getAttributes().split(";");
            Map<String, String> attrs = new HashMap<>();

            for (String a : attrSpl) {
                String[] kvSpl = a.split(":");

                if (kvSpl.length == 2 && !attrs.containsKey(kvSpl[0])) attrs.put(kvSpl[0], kvSpl[1]);
                else throw new Exception("Failure parsing application attributes");
            }

            for (FodAttributeMapItem a : mapAttributesToFod(attrs)) {
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
                .post(RequestBody.create(MediaType.parse("application/json"), requestContent))
                .build();

        println("Submitting application and release model");

        ResponseContent response = apiConnection.request(request);

        if (response.code() < 300) {
            PostReleaseWithUpsertApplicationResponseModel result = apiConnection.parseResponse(response, new TypeToken<PostReleaseWithUpsertApplicationResponseModel>() {
            }.getType());

            if (result.getSuccess()) {
                println("Provisioning successful. Release Id: " + result.getReleaseId());
                return result.getReleaseId();
            } else throw new Exception("Failed to create application and/or release: \n" + String.join("\n", result.getErrors()));
        } else {
            throw new Exception("Failed to create application and/or release: \n" + response.bodyContent());
        }
    }

    public List<FodAttributeMapItem> mapAttributesToFod(Map<String, String> attributes) throws Exception {
        // Todo: this should be injected
        AttributesController attrCntr = new AttributesController(apiConnection, logger, correlationId);
        List<AttributeDefinition> fodAttr = attrCntr.getAttributeDefinitions();
        List<FodAttributeMapItem> result = new ArrayList<>();
        List<String> invalidPickListAttributes = new ArrayList<>();

        for (Map.Entry<String, String> a : attributes.entrySet()) {
            for (AttributeDefinition fa : fodAttr) {
                if (a.getKey().equals(fa.getName())) {
                    if (fa.getAttributeDataType().equalsIgnoreCase("Picklist")) {
                        ArrayList<String> pickListAttrValueStrings = new ArrayList<>();
                        for (PicklistValue pv : fa.getPicklistValues()) {
                            pickListAttrValueStrings.add(pv.getName());
                        }
                        if (pickListAttrValueStrings.contains(a.getValue()))
                            result.add(new FodAttributeMapItem(a.getKey(), a.getValue(), fa));
                        else
                            invalidPickListAttributes.add(a.getKey());
                        break;
                    } else if (fa.getAttributeDataType().equalsIgnoreCase("Boolean")) {
                        Pattern queryLangPattern = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
                        Matcher matcher = queryLangPattern.matcher(a.getValue());
                        if (matcher.matches())
                            result.add(new FodAttributeMapItem(a.getKey(), a.getValue(), fa));
                        else
                            invalidPickListAttributes.add(a.getKey() + " : true/false");
                        break;
                    } else if (fa.getAttributeDataType().equalsIgnoreCase("User")) {
                        ArrayList<String> userAttrValues = new ArrayList<>();
                        ArrayList<String> userAttrValueIdNames = new ArrayList<>();
                        for (PicklistValue pv : fa.getPicklistValues()) {
                            userAttrValues.add(pv.getName());
                            userAttrValues.add(String.valueOf(pv.getId()));
                            userAttrValueIdNames.add(String.valueOf(pv.getId()) + "-" + pv.getName());
                        }
                        if (userAttrValues.contains(a.getValue()))
                            result.add(new FodAttributeMapItem(a.getKey(), a.getValue(), fa));
                        else
                            invalidPickListAttributes.add(a.getKey() + " : " + userAttrValueIdNames.stream().collect(Collectors.joining(",")));
                        break;
                    }
                    result.add(new FodAttributeMapItem(a.getKey(), a.getValue(), fa));
                    break;
                }
            }
        }
        if (invalidPickListAttributes.size() > 0) {
            throw new Exception(String.format("Invalid PickList Attributes/Values for the following Picklist Attribute/s - %s", invalidPickListAttributes.stream().collect(Collectors.joining(" & "))));
        }
        return result;
    }

    /**
     * @deprecated Use the {@link StaticScanController#getStaticScanSettings} method instead
     */
    @Deprecated
    public GetStaticScanSetupResponse getStaticScanSettingsOld(final Integer releaseId) throws IOException {
        HttpUrl.Builder builder = HttpUrl.parse(apiConnection.getApiUrl()).newBuilder()
                .addPathSegments(String.format("/api/v3/releases/%d/static-scans/scan-setup", releaseId));

        String url = builder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("CorrelationId", getCorrelationId())
                .get()
                .build();

        ResponseContent response = apiConnection.request(request);

        if (!response.isSuccessful()) {
            return null;
        }

        String content = response.bodyContent();

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
        ResponseContent response = apiConnection.request(request);

        if (response.code() < 300) {
            return apiConnection.parseResponse(response, new TypeToken<PutStaticScanSetupResponse>() {
            }.getType());
        } else {
            String rawBody = response.bodyContent();
            List<String> errors = Utils.unexpectedServerResponseErrors();

            if (!rawBody.isEmpty()) errors.add("Raw API response:\n" + rawBody);
            else errors.add("API empty response");

            return new PutStaticScanSetupResponse(false, null, errors, null);
        }
    }
}
