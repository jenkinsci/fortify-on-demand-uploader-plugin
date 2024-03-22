package org.jenkinsci.plugins.fodupload;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.StaticScanController;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.BsiToken;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.FodEnums.InProgressBuildResultType;
import org.jenkinsci.plugins.fodupload.models.SastJobModel;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.verb.POST;

import java.io.*;
import java.util.EnumSet;
import java.util.List;

import static org.jenkinsci.plugins.fodupload.Utils.FOD_URL_ERROR_MESSAGE;
import static org.jenkinsci.plugins.fodupload.Utils.isValidUrl;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class SharedUploadBuildStep {

    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private SastJobModel model;
    private AuthenticationModel authModel;
    private int scanId;

    public SharedUploadBuildStep(String releaseId,
                                 String bsiToken,
                                 boolean overrideGlobalConfig,
                                 String username,
                                 String personalAccessToken,
                                 String tenantId,
                                 boolean purchaseEntitlements,
                                 String entitlementPreference,
                                 String srcLocation,
                                 String remediationScanPreferenceType,
                                 String inProgressScanActionType,
                                 String inProgressBuildResultType,
                                 String selectedReleaseType,
                                 String userSelectedApplication,
                                 String userSelectedMicroservice,
                                 String userSelectedRelease,
                                 String selectedScanCentralBuildType,
                                 boolean scanCentralSkipBuild,
                                 String scanCentralBuildCommand,
                                 String scanCentralBuildFile,
                                 String scanCentralExcludeFiles,
                                 String scanCentralBuildToolVersion,
                                 String scanCentralVirtualEnv,
                                 String scanCentralRequirementFile) {

        model = new SastJobModel(releaseId,
                bsiToken,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType,
                selectedReleaseType,
                userSelectedApplication,
                userSelectedMicroservice,
                userSelectedRelease,
                selectedScanCentralBuildType,
                scanCentralSkipBuild,
                scanCentralBuildCommand,
                scanCentralBuildFile,
                scanCentralExcludeFiles,
                scanCentralBuildToolVersion,
                scanCentralVirtualEnv,
                scanCentralRequirementFile,
                false, null, null, null, null, null, null, null,
                false, null, null, null, null, null, null, null, null, null);

        authModel = new AuthenticationModel(overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId);
    }

    public SharedUploadBuildStep(String releaseId,
                                 String bsiToken,
                                 boolean overrideGlobalConfig,
                                 String username,
                                 String personalAccessToken,
                                 String tenantId,
                                 boolean purchaseEntitlements,
                                 String entitlementPreference,
                                 String srcLocation,
                                 String remediationScanPreferenceType,
                                 String inProgressScanActionType,
                                 String inProgressBuildResultType,
                                 String selectedScanCentralBuildType,
                                 boolean scanCentralSkipBuild,
                                 String scanCentralBuildCommand,
                                 String scanCentralBuildFile,
                                 String scanCentralExcludeFiles,
                                 String scanCentralBuildToolVersion,
                                 String scanCentralVirtualEnv,
                                 String scanCentralRequirementFile,
                                 String assessmentType,
                                 String entitlementId,
                                 String frequencyId,
                                 String auditPreference,
                                 String technologyStack,
                                 String languageLevel,
                                 String openSourceScan,
                                 Boolean autoProvision,
                                 String applicationName,
                                 String applicationType,
                                 String releaseName,
                                 Integer owner,
                                 String attributes,
                                 String businessCriticality,
                                 String sdlcStatus,
                                 String microserviceName,
                                 Boolean isMicroservice) {

        model = new SastJobModel(releaseId,
                bsiToken,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType,
                null,
                null,
                null,
                null,
                selectedScanCentralBuildType,
                scanCentralSkipBuild,
                scanCentralBuildCommand,
                scanCentralBuildFile,
                scanCentralExcludeFiles,
                scanCentralBuildToolVersion,
                scanCentralVirtualEnv,
                scanCentralRequirementFile,
                true,
                assessmentType,
                entitlementId,
                frequencyId,
                auditPreference,
                technologyStack,
                languageLevel,
                openSourceScan,
                autoProvision,
                applicationName,
                applicationType,
                releaseName,
                owner,
                attributes,
                businessCriticality,
                sdlcStatus,
                microserviceName,
                isMicroservice);

        authModel = new AuthenticationModel(overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId);
    }

    public static FormValidation doCheckReleaseId(String releaseId, String bsiToken) {
        if (releaseId != null && !releaseId.isEmpty()) {
            try {
                Integer testReleaseId = Integer.parseInt(releaseId);
                return FormValidation.ok();
            } catch (NumberFormatException ex) {
                return FormValidation.error("Could not parse Release ID.");
            }
        } else {
            if (bsiToken != null && !bsiToken.isEmpty()) {
                return FormValidation.ok();
            }

            return FormValidation.error("Please specify Release ID or BSI Token.");
        }
    }

    public static FormValidation doCheckBsiToken(String bsiToken, String releaseId) {
        if (bsiToken != null && !bsiToken.isEmpty()) {
            BsiTokenParser tokenParser = new BsiTokenParser();
            try {
                BsiToken testToken = tokenParser.parseBsiToken(bsiToken);
                if (testToken != null) {
                    return FormValidation.ok();
                }
            } catch (Exception ex) {
                return FormValidation.error("Could not parse BSI token.");
            }
        } else {
            if (releaseId != null && !releaseId.isEmpty()) {
                return FormValidation.ok();
            }
            return FormValidation.error("Please specify Release ID or BSI Token.");
        }
        return FormValidation.error("Please specify Release ID or BSI Token.");
    }

    /*
    Maura E. Ardden: 09/15/2022
    Added URL validation using org.jenkinsci.plugins.fodupload.Utils.isValidUrl(url) to
       doTestPersonalAccessTokenConnection
    */

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @POST
    public static FormValidation doTestPersonalAccessTokenConnection(final String username,
                                                                     final String personalAccessToken,
                                                                     final String tenantId,
                                                                     @AncestorInPath Job job) throws FormValidation {
        job.checkPermission(Item.CONFIGURE);
        FodApiConnection testApi;
        String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
        String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
        String plainTextPersonalAccessToken = Utils.retrieveSecretDecryptedValue(personalAccessToken);
        if (Utils.isNullOrEmpty(isValidUrl(baseUrl)))
            return FormValidation.error(FOD_URL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(isValidUrl(apiUrl)))
            return FormValidation.error(FOD_URL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(username))
            return FormValidation.error("Username is empty!");
        if (!Utils.isCredential(personalAccessToken))
            return FormValidation.error("Personal Access Token is empty!");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");
        testApi = new FodApiConnection(tenantId + "\\" + username, plainTextPersonalAccessToken, baseUrl, apiUrl, FodEnums.GrantType.PASSWORD, "api-tenant", false, null, null);
        return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);

    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillEntitlementPreferenceItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.EntitlementPreferenceType preferenceType : FodEnums.EntitlementPreferenceType.values()) {
            items.add(new ListBoxModel.Option(preferenceType.toString(), preferenceType.getValue()));
        }

        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillRemediationScanPreferenceTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.RemediationScanPreferenceType remediationType : FodEnums.RemediationScanPreferenceType.values()) {
            items.add(new ListBoxModel.Option(remediationType.toString(), remediationType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillStringCredentialsItems(@AncestorInPath Job job) {
        job.checkPermission(Item.CONFIGURE);
        ListBoxModel items = CredentialsProvider.listCredentials(
                StringCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                null,
                null
        );

        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillInProgressScanActionTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.InProgressScanActionType scanActionType : FodEnums.InProgressScanActionType.values()) {
            items.add(new ListBoxModel.Option(scanActionType.toString(), scanActionType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillInProgressBuildResultTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.InProgressBuildResultType buildResultType : FodEnums.InProgressBuildResultType.values()) {
            items.add(new ListBoxModel.Option(buildResultType.toString(), buildResultType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillSelectedReleaseTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.SelectedReleaseType selectedReleaseType : FodEnums.SelectedReleaseType.values()) {
            items.add(new ListBoxModel.Option(selectedReleaseType.toString(), selectedReleaseType.getValue()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillSelectedScanCentralBuildTypeItems() {
        return doFillFromEnum(FodEnums.SelectedScanCentralBuildType.class);
    }

    private static <T extends Enum<T>> ListBoxModel doFillFromEnum(Class<T> enumClass) {
        ListBoxModel items = new ListBoxModel();
        for (T selected : EnumSet.allOf(enumClass)) {
            items.add(new ListBoxModel.Option(selected.toString(), selected.name()));
        }
        return items;
    }

    @SuppressWarnings("unused")
    public static GenericListResponse<ApplicationApiResponse> customFillUserSelectedApplicationList(String searchTerm, int offset, int limit, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getApplicationList(searchTerm, offset, limit);
    }

    public static org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> customFillUserApplicationById(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> result = applicationsController.getApplicationById(applicationId);

        return result;
    }

    public static List<MicroserviceApiResponse> customFillUserSelectedMicroserviceList(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getMicroserviceListByApplication(applicationId);
    }

    public static GenericListResponse<ReleaseApiResponse> customFillUserSelectedReleaseList(int applicationId, int microserviceId, String searchTerm, Integer offset, Integer limit, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getReleaseListByApplication(applicationId, microserviceId, searchTerm, offset, limit);
    }

    public static org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> customFillUserReleaseById(int releaseId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> result = applicationsController.getReleaseById(releaseId);

        return result;
    }

    public static EntitlementSettings customFillEntitlementSettings(int releaseId, AuthenticationModel authModel) throws IOException {
        return new EntitlementSettings(
                1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
                1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
                1, java.util.Arrays.asList(new LookupItemsModel[]{new LookupItemsModel("1", "Placeholder")}),
                1, 1, false);
    }

    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        final PrintStream logger = listener.getLogger();
        if (model == null) {
            logger.println("Unexpected Error: prebuild model is null");
            build.setResult(Result.FAILURE);
            return false;
        }

        if (!model.getIsPipeline() && (model.getReleaseId() == null || model.getReleaseId().isEmpty()) && !model.loadBsiToken()) {
            logger.println("Invalid release ID or BSI Token");
            build.setResult(Result.FAILURE);
            return false;
        }

        if (!model.validate(logger)) {
            build.setResult(Result.FAILURE);
            return false;
        }

        return true;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace,
                        Launcher launcher, TaskListener listener, String correlationId) {

        final PrintStream logger = listener.getLogger();
        FodApiConnection apiConnection = null;
        boolean isRemoteAgent = workspace.isRemote();

        try {
            taskListener.set(listener);

            // check to see if sensitive fields are encrypted. If not halt scan and recommend encryption.
            if (authModel != null) {
                if (authModel.getOverrideGlobalConfig() == true) {
                    if (!Utils.isCredential(authModel.getPersonalAccessToken())) {
                        build.setResult(Result.UNSTABLE);
                        logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                        return;
                    }
                } else {
                    if (GlobalConfiguration.all().get(FodGlobalDescriptor.class).getAuthTypeIsApiKey()) {
                        if (!Utils.isCredential(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalClientSecret())) {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return;
                        }
                    } else {
                        if (!Utils.isCredential(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalPersonalAccessToken())) {
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

            logger.println("Starting FoD Upload.");
            logger.println("Correlation Id = " + correlationId);

            Integer releaseId = 0;
            try {
                releaseId = Integer.parseInt(model.getReleaseId());
            } catch (NumberFormatException ex) {
            }

            if (!model.getIsPipeline() && releaseId == 0 && !model.loadBsiToken()) {
                build.setResult(Result.FAILURE);
                logger.println("Invalid release ID or BSI Token");
                return;
            }

            if (releaseId > 0 && model.loadBsiToken()) {
                logger.println("Warning: The BSI Token will be ignored since Release ID was entered.");
            }

            String technologyStack = null;
            Boolean openSourceAnalysis = false;

            apiConnection = ApiConnectionFactory.createApiConnection(getAuthModel(), isRemoteAgent, launcher, logger);

            if (apiConnection != null) {
                StaticScanController staticScanController = new StaticScanController(apiConnection, logger, correlationId);

                if (releaseId <= 0 && model.loadBsiToken()) technologyStack = model.getBsiToken().getTechnologyStack();
                else if (model.getIsPipeline() || releaseId > 0) technologyStack = model.getTechnologyStack();

                if (Utils.isNullOrEmpty(technologyStack) || model.getOpenSourceScan() == null) {
                    logger.println("Getting scan settings for release " + releaseId);
                    GetStaticScanSetupResponse staticScanSetup = staticScanController.getStaticScanSettingsOld(releaseId);

                    if (Utils.isNullOrEmpty(technologyStack)) {
                        if (staticScanSetup == null || Utils.isNullOrEmpty(staticScanSetup.getTechnologyStack())) {
                            logger.println("No scan settings defined for release " + releaseId);
                            build.setResult(Result.FAILURE);
                            return;
                        }
                        technologyStack = staticScanSetup.getTechnologyStack();
                    }

                    if (model.getOpenSourceScan() == null) openSourceAnalysis = staticScanSetup.isPerformOpenSourceAnalysis();
                }

                if (model.getOpenSourceScan() != null) openSourceAnalysis = Boolean.parseBoolean(model.getOpenSourceScan());

                String scsetting = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getScanCentralPath();
                PayloadPackaging packaging = PayloadPackaging.getInstance(model, technologyStack, openSourceAnalysis, scsetting, workspace, launcher, logger);

                try {
                    model.setPayload(packaging.packagePayload());
                } catch (Exception e) {
                    logger.println(e.getMessage());
                    build.setResult(Result.FAILURE);
                    return;
                }

                String notes = String.format("[%d] %s - Assessment submitted from Jenkins FoD Plugin",
                        build.getNumber(),
                        build.getDisplayName());

                StartScanResponse scanResponse = staticScanController.startStaticScan(releaseId, model, notes);
                boolean deleted = false;

                try {
                    deleted = packaging.deletePayload();
                } catch (Exception ignored) {

                }

                boolean isWarningSettingEnabled = model.getInProgressBuildResultType().equalsIgnoreCase(InProgressBuildResultType.WarnBuild.getValue());

                /**
                 * If(able to contact api) {
                 *      if(Scan is allowed to start && the uploaded file is deleted) {
                 *          All good
                 *      }
                 *      else if (Scan in not allowed to start && user selected WarnBuild Build Action) {
                 *          Say all good
                 *          Set flag that stops anny additional FOD stuff
                 *      }
                 *      else (Scan is not allowed to start && user selected FailBuild Build Action) {
                 *          Fail Build
                 *      }
                 * } else (unable to contact api) {
                 *      Fail Build
                 * }
                 */
                if (scanResponse.isSuccessful()) {
                    if (scanResponse.isScanUploadAccepted()) {
                        logger.println("Scan Uploaded Successfully.");
                        setScanId(scanResponse.getScanId());
                        build.setResult(Result.SUCCESS);
                        if (!deleted) {
                            logger.println("Unable to delete temporary zip file. Please manually delete file at location: " + model.getPayload().getRemote());
                        }
                    } else if (isWarningSettingEnabled) {
                        logger.println("Fortify scan skipped because another scan is in progress.");
                        build.setResult(Result.UNSTABLE);
                    } else {
                        logger.println("Build failed because another scan is in progress and queuing is not selected as the \"in progress scan\" action in settings.");
                        build.setResult(Result.FAILURE);
                    }
                } else {
                    build.setResult(Result.FAILURE);
                }
            } else {
                logger.println("Failed to authenticate");
                build.setResult(Result.FAILURE);
            }


        } catch (IOException e) {
            logger.println(e.getMessage());
            build.setResult(Result.FAILURE);
        } catch (IllegalArgumentException iae) {
            logger.println(iae.getMessage());
            build.setResult(Result.FAILURE);
        } finally {
            if (apiConnection != null) {
                try {
                    apiConnection.retireToken();
                } catch (IOException e) {
                    logger.println(String.format("Failed to retire oauth token. Response code is %s", e));
                }
            }
        }
    }

    public AuthenticationModel getAuthModel() {
        AuthenticationModel displayModel = new AuthenticationModel(authModel.getOverrideGlobalConfig(),
                authModel.getUsername(),
                authModel.getPersonalAccessToken(),
                authModel.getTenantId());

        return displayModel;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public SastJobModel getModel() {
        return model;
    }

    public int getScanId() {
        return scanId;
    }

    public int setScanId(int newScanId) {
        return scanId = newScanId;
    }

}
