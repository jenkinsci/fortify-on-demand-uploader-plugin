package org.jenkinsci.plugins.fodupload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.DynamicScanController;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.DynamicScanJobModel;
import org.jenkinsci.plugins.fodupload.models.PutDynamicScanSetupModel;
import org.jenkinsci.plugins.fodupload.models.StartDynamicScanReqModel;
import org.jenkinsci.plugins.fodupload.models.response.PutDynamicScanSetupResponse;
import org.jenkinsci.plugins.fodupload.models.response.StartDynamicScanResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

public class DynamicScanSharedBuildStep {
    private final DynamicScanJobModel model;
    private AuthenticationModel authModel;

    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private int scanId;

    public DynamicScanSharedBuildStep(boolean overrideGlobalConfig, String username,

                                      String personalAccessToken, String tenantId,
                                      String releaseId, String selectedReleaseType,
                                      List<String> webSiteUrl, String dastEnv,
                                      String scanPolicyType, boolean scanHost,
                                      boolean allowHttp, boolean allowFormSubmissionCrawl,
                                      String selectedScanType, String selectedDynamicTimeZone,
                                      boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
                                      String loginFileMacro, String webSiteNetworkAuthPassword,
                                      String userSelectedApplication,
                                      String userSelectedRelease, String assessmentTypeId,
                                      String entitlementId, String entitlementFrequencyId,
                                      String entitlementFrequencyType, String userSelectedEntitlement,
                                      String selectedDynamicGeoLocation, boolean requireWebSiteNetworkAuth,
                                      boolean requireWebSiteLoginMacro,
                                      String networkAuthType) {

        authModel = new AuthenticationModel(overrideGlobalConfig, username, personalAccessToken, tenantId);

        model = new DynamicScanJobModel(overrideGlobalConfig,
                username, personalAccessToken,
                tenantId, releaseId, selectedReleaseType,
                webSiteUrl, dastEnv,
                scanPolicyType, scanHost,
                allowHttp, allowFormSubmissionCrawl,
                selectedScanType, selectedDynamicTimeZone,
                enableRedundantPageDetection, webSiteNetworkAuthUserName,
                loginFileMacro, webSiteNetworkAuthPassword,
                userSelectedApplication,
                userSelectedRelease, assessmentTypeId,
                entitlementId, entitlementFrequencyId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedDynamicGeoLocation, requireWebSiteNetworkAuth,
                requireWebSiteLoginMacro, networkAuthType);

    }

    private FodApiConnection getApiConnection() throws FormValidation {

        return ApiConnectionFactory.createApiConnection(this.getAuthModel(), false, null ,null);
    }

    public int getScanId() {
        return scanId;
    }

    public DynamicScanJobModel getModel() {
        return model;
    }

    public AuthenticationModel getAuthModel() {
        return authModel;
    }

    public void saveReleaseSettingsForWebSiteScan(String userSelectedRelease, String assessmentTypeID,
                                                  String entitlementId, String entitlementFreq, String geoLocationId, String loginMacroId,
                                                  String timeZone, String scanType, String scanPolicy, List<String> webSiteAssessmentUrl,
                                                  boolean allowFrmSubmission, boolean allowSameHostRedirect, boolean restrictDirectories,
                                                  boolean redundantPageDetection,String scanEnvironment,
                                                  boolean requireNetworkAuth, boolean requireLoginMacroAuth,
                                                  String networkAuthUserName, String networkAuthPassword
    )
            throws IllegalArgumentException, IOException {

        String releaseId = "";

        DynamicScanController dynamicController = new DynamicScanController(getApiConnection(), null, Utils.createCorrelationId());

        try {

            releaseId = userSelectedRelease;

            PutDynamicScanSetupModel dynamicScanSetupReqModel;
            dynamicScanSetupReqModel = new PutDynamicScanSetupModel();
            dynamicScanSetupReqModel.setGeoLocationId(Integer.parseInt(geoLocationId)); //Check here for null
            dynamicScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dynamicScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dynamicScanSetupReqModel.setTimeZone(timeZone);
            dynamicScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));

            if (loginMacroId != "" && loginMacroId != null) {
                dynamicScanSetupReqModel.setLoginMacroFileId(Integer.parseInt(loginMacroId));
            }

            dynamicScanSetupReqModel.setPolicy(scanPolicy);
            dynamicScanSetupReqModel.setScanType(scanType);

            dynamicScanSetupReqModel.setRequiresNetworkAuthentication(requireNetworkAuth);

            dynamicScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireLoginMacroAuth && (loginMacroId != "" && loginMacroId != null) && Integer.parseInt(loginMacroId) != 0)
                dynamicScanSetupReqModel.setRequiresSiteAuthentication(true);

            if (requireNetworkAuth) {
                dynamicScanSetupReqModel.getNetworkAuthenticationSettings().password = networkAuthPassword;
                dynamicScanSetupReqModel.getNetworkAuthenticationSettings().userName = networkAuthUserName;
            }

            dynamicScanSetupReqModel.setAllowFormSubmissions(allowFrmSubmission);
            dynamicScanSetupReqModel.setEnableRedundantPageDetection(redundantPageDetection);
            dynamicScanSetupReqModel.setRestrictToDirectoryAndSubdirectories(restrictDirectories);

            if (!webSiteAssessmentUrl.isEmpty()) {

                dynamicScanSetupReqModel.setWebSites().urls.addAll(webSiteAssessmentUrl);
            }

            PutDynamicScanSetupResponse response = dynamicController.putDynamicWebSiteScanSettings(Integer.parseInt(releaseId),
                    dynamicScanSetupReqModel);

            if (response.isSuccess()) {
                System.out.println("Successfully saved settings for release id = " + releaseId);
            } else if (response.getErrors() != null && !response.getErrors().isEmpty()) {

                String errs = response.getErrors().toString().replace("\n", "\n\t\t");
                //String errs = response.getErrors().stream().map(s -> s.replace("\n", "\n\t\t")).collect(Collectors.joining("\n\t"));

                System.err.println("Error saving settings for release id = " + releaseId + "\n\t" + errs);
                throw new IllegalArgumentException("Failed to save scan settings for release id = " + releaseId);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to save scan settings for release id = " + releaseId, e);
        }
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace,
                        Launcher launcher, TaskListener listener, String correlationId) {
        final PrintStream logger = listener.getLogger();
        FodApiConnection apiConnection = null;

        try {
            taskListener.set(listener);
            // check to see if sensitive fields are encrypted. If not halt scan and recommend encryption.
            if (authModel != null) {
                if (authModel.getOverrideGlobalConfig()) {
                    if (!Utils.isCredential(authModel.getPersonalAccessToken())) {
                        build.setResult(Result.UNSTABLE);
                        logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                        return;
                    }
                } else {
                    if (Objects.requireNonNull(GlobalConfiguration.all().get(FodGlobalDescriptor.class)).getAuthTypeIsApiKey()) {
                        if (!Utils.isCredential(Objects.requireNonNull(GlobalConfiguration.all().get(FodGlobalDescriptor.class)).getOriginalClientSecret())) {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return;
                        }
                    } else {
                        if (!Utils.isCredential(Objects.requireNonNull(GlobalConfiguration.all().get(FodGlobalDescriptor.class)).getOriginalPersonalAccessToken())) {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return;
                        }
                    }
                }
            }
            Result currentResult = build.getResult();
            if (Result.FAILURE.equals(currentResult)
                    || Result.ABORTED.equals(currentResult)
                    || Result.UNSTABLE.equals(currentResult)) {

                logger.println("Error: Build Failed or Unstable.  Halting with Fortify on Demand upload.");
                return;
            }
            logger.println("Correlation Id = " + correlationId);

            Integer releaseId = Integer.parseInt(model.get_releaseId());

            try {
                apiConnection = ApiConnectionFactory.createApiConnection(this.getAuthModel(), false, null, null);
            } catch (IOException e) {
                build.setResult(Result.FAILURE);
                throw new RuntimeException(e);
            }

            StartDynamicScanReqModel startDynamicScanReqModel = new StartDynamicScanReqModel();

            startDynamicScanReqModel.setRemediationScan(model.isEnableRedundantPageDetection());
            startDynamicScanReqModel.setStartDate("2023-09-19T15:38:33.828Z");
            startDynamicScanReqModel.setEntitlementId(Integer.parseInt(model.getEntitlementId()));
            startDynamicScanReqModel.setAssessmentTypeId(Integer.parseInt(model.getAssessmentTypeId()));
            startDynamicScanReqModel.setEntitlementFrequencyType(model.getEntitlementFrequencyType());

            DynamicScanController dynamicController = new DynamicScanController(apiConnection, null, Utils.createCorrelationId());
            StartDynamicScanResponse response = dynamicController.StartDynamicScan(releaseId, startDynamicScanReqModel);

            if (response.isSuccess()) {
                build.setResult(Result.SUCCESS);
            } else {
                build.setResult(Result.FAILURE);
            }
//            if (!model.getIsPipeline() && releaseId == 0 && !model.loadBsiToken()) {
//                build.setResult(Result.FAILURE);
//                logger.println("Invalid release ID or BSI Token");
//                return;
//            }
//
//            if (releaseId > 0 && model.loadBsiToken()) {
//                logger.println("Warning: The BSI Token will be ignored since Release ID was entered.");
//            }


        } catch (IOException e) {
            build.setResult(Result.FAILURE);
            throw new RuntimeException(e);
        }
    }
}



