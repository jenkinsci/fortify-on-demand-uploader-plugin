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
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PutDastScanSetupResponse;
import org.jenkinsci.plugins.fodupload.models.response.Dast.PostDastStartScanResponse;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DynamicScanSharedBuildStep {
    private final DynamicScanJobModel model;
    private final AuthenticationModel authModel;

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
                                      String webSiteUrl, String dastEnv,
                                      String scanTimebox,
                                      List<String> standardScanTypeExcludeUrlsRow,
                                      String scanPolicyType, boolean scanScope,
                                      String selectedScanType, String selectedDynamicTimeZone,
                                      boolean webSiteLoginMacroEnabled, boolean webSiteNetworkAuthSettingEnabled,
                                      boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
                                      String loginMacroId, String workflowMacroId, String allowedHost, String webSiteNetworkAuthPassword,
                                      String userSelectedApplication,
                                      String userSelectedRelease, String assessmentTypeId,
                                      String entitlementId,
                                      String entitlementFrequencyType, String userSelectedEntitlement,
                                      String selectedDynamicGeoLocation, String selectedNetworkAuthType) {

        authModel = new AuthenticationModel(overrideGlobalConfig, username, personalAccessToken, tenantId);


        model = new DynamicScanJobModel(overrideGlobalConfig, username, personalAccessToken, tenantId,
                releaseId, selectedReleaseType, webSiteUrl
                , dastEnv, scanTimebox, standardScanTypeExcludeUrlsRow, scanPolicyType, scanScope, selectedScanType
                , selectedDynamicTimeZone, webSiteLoginMacroEnabled,
                webSiteNetworkAuthSettingEnabled, enableRedundantPageDetection,
                webSiteNetworkAuthUserName, loginMacroId, workflowMacroId, allowedHost
                , webSiteNetworkAuthPassword, userSelectedApplication,
                userSelectedRelease, assessmentTypeId, entitlementId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedDynamicGeoLocation, selectedNetworkAuthType);

    }

    private FodApiConnection getApiConnection() throws FormValidation {

        return ApiConnectionFactory.createApiConnection(this.getAuthModel(), false, null, null);
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
                                                  String entitlementId, String entitlementFreq, String loginMacroId,
                                                  String timeZone, String scanPolicy, String webSiteAssessmentUrl,
                                                  boolean scanScope,
                                                  boolean redundantPageDetection, String scanEnvironment,
                                                  boolean requireNetworkAuth, boolean requireLoginMacroAuth,
                                                  String networkAuthUserName, String networkAuthPassword
            , String networkAuthType, String timeboxScan

    )
            throws IllegalArgumentException, IOException {

        DynamicScanController dynamicController = new DynamicScanController(getApiConnection(), null, Utils.createCorrelationId());

        try {

            PutDastWebSiteScanReqModel dynamicScanSetupReqModel;
            dynamicScanSetupReqModel = new PutDastWebSiteScanReqModel();
            dynamicScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dynamicScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dynamicScanSetupReqModel.setTimeZone(timeZone);
            dynamicScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));

            if (!Objects.equals(loginMacroId, "") && loginMacroId != null && requireLoginMacroAuth) {
                dynamicScanSetupReqModel.setLoginMacroFileId(Integer.parseInt(loginMacroId));
            }

            try {
                if (!timeboxScan.isEmpty())
                    dynamicScanSetupReqModel.setTimeBoxInHours(Integer.parseInt(timeboxScan));
            } catch (NumberFormatException exception) {
                // Should warn not throw in front end.
                throw new IllegalArgumentException(" value for TimeBox Scan");
            }

            dynamicScanSetupReqModel.setPolicy(scanPolicy);
            dynamicScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireLoginMacroAuth && (!Objects.equals(loginMacroId, "") && loginMacroId != null))
                if (Integer.parseInt(loginMacroId) != 0) {
                    dynamicScanSetupReqModel.setRequiresSiteAuthentication(true);
                }
            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkSetting = dynamicScanSetupReqModel.getNetworkAuthenticationSettings();
                networkSetting.setPassword(networkAuthPassword);
                networkSetting.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkSetting.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);

                networkSetting.setRequiresNetworkAuthentication(true);

                dynamicScanSetupReqModel.setNetworkAuthenticationSettings(networkSetting);
            }

            if (scanScope) //if true => Restrict scan to URL directories and subdirectories
                dynamicScanSetupReqModel.setRestrictToDirectoryAndSubdirectories(scanScope);
            else
                dynamicScanSetupReqModel.setRestrictToDirectoryAndSubdirectories(true);

            dynamicScanSetupReqModel.setDynamicSiteUrl(webSiteAssessmentUrl);

            PutDastScanSetupResponse response = dynamicController.putDastWebSiteScanSettings(Integer.parseInt(userSelectedRelease),
                    dynamicScanSetupReqModel);

            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {

                throw new Exception("Failed to save scan settings for release id: " + userSelectedRelease);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to save scan settings for release id = " + userSelectedRelease, e);
        }
    }


    public void saveReleaseSettingsForWorkflowDrivenScan(String userSelectedRelease, String assessmentTypeID,
                                                         String entitlementId, String entitlementFreq, String workflowMacroId,
                                                         String workflowMacroHosts,
                                                         String timeZone, String scanPolicy,
                                                         boolean redundantPageDetection, String scanEnvironment,
                                                         boolean requireNetworkAuth,
                                                         String networkAuthUserName, String networkAuthPassword
            , String networkAuthType)
            throws IllegalArgumentException, IOException {

        DynamicScanController dynamicController = new DynamicScanController(getApiConnection(), null, Utils.createCorrelationId());
        try {

            PutDastWorkflowDrivenScanReqModel dastWorkflowScanSetupReqModel;
            dastWorkflowScanSetupReqModel = new PutDastWorkflowDrivenScanReqModel();
            dastWorkflowScanSetupReqModel.setEntitlementFrequencyType(entitlementFreq);
            dastWorkflowScanSetupReqModel.setAssessmentTypeId(Integer.parseInt(assessmentTypeID));
            dastWorkflowScanSetupReqModel.setTimeZone(timeZone);
            dastWorkflowScanSetupReqModel.setEntitlementId(Integer.parseInt(entitlementId));

            if (workflowMacroId.isEmpty() || workflowMacroHosts.isEmpty()) {

                throw new IllegalArgumentException(String.format("WorkflowMacro FileId=%s or WorkflowHost=%s not set for release Id={%s}"
                        , workflowMacroId, workflowMacroHosts, userSelectedRelease));
            } else {
                //Support for only one file upload. need to add the validation as part of this.
                dastWorkflowScanSetupReqModel.workflowDrivenMacro = new ArrayList<>();
                WorkflowDrivenMacro wrkDrivenMacro = new WorkflowDrivenMacro();
                wrkDrivenMacro.fileId = Integer.parseInt(workflowMacroId);
                wrkDrivenMacro.allowedHosts = workflowMacroHosts.split(",");
                dastWorkflowScanSetupReqModel.workflowDrivenMacro.add(wrkDrivenMacro);
            }

            dastWorkflowScanSetupReqModel.setPolicy(scanPolicy);
            dastWorkflowScanSetupReqModel.setDynamicScanEnvironmentFacingType(scanEnvironment);

            if (requireNetworkAuth) {
                PutDastScanSetupReqModel.NetworkAuthentication networkAuthentication = dastWorkflowScanSetupReqModel.getNetworkAuthenticationSettings();
                networkAuthentication.setPassword(networkAuthPassword);
                networkAuthentication.setUserName(networkAuthUserName);

                if (!networkAuthType.isEmpty()) {
                    networkAuthentication.setNetworkAuthenticationType((networkAuthType));
                } else
                    throw new IllegalArgumentException("Network Auth Type not set for releaseId: " + userSelectedRelease);
                networkAuthentication.setRequiresNetworkAuthentication(true);
                dastWorkflowScanSetupReqModel.setNetworkAuthenticationSettings(networkAuthentication);
            }

            PutDastScanSetupResponse response = dynamicController.putDastWorkflowDrivenScanSettings(Integer.parseInt(userSelectedRelease),
                    dastWorkflowScanSetupReqModel);
            if (response.isSuccess && response.errors == null) {
                System.out.println("Successfully saved settings for release id = " + userSelectedRelease);

            } else {
                throw new Exception(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Failed to save scan settings for release id %d", Integer.parseInt(userSelectedRelease)), e);
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
            DynamicScanController dynamicController = new DynamicScanController(apiConnection, null, Utils.createCorrelationId());
            PostDastStartScanResponse response = dynamicController.StartDynamicScan(releaseId);

            if (response.errors == null && response.getScanId() > 0) {
                build.setResult(Result.SUCCESS);
                build.setDescription(String.format("Successfully triggered Dynamic scan for scan id %d", response.getScanId()));
            } else {
                build.setResult(Result.FAILURE);
                build.setDescription(String.format("Failed to trigger Dynamic scan for release id %d", releaseId));
            }


        } catch (IOException e) {
            build.setResult(Result.FAILURE);
            throw new RuntimeException(e);
        }
    }
}



