package org.jenkinsci.plugins.fodupload;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudbees.plugins.credentials.CredentialsProvider;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jenkinsci.plugins.fodupload.controllers.ApplicationsController;
import org.jenkinsci.plugins.fodupload.controllers.StaticScanController;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.BsiToken;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums.InProgressBuildResultType;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.verb.POST;

public class SharedUploadBuildStep {

    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private JobModel model;
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
                                 String scanCentralBuildToolVersion,
                                 String scanCentralVirtualEnv,
                                 String scanCentralRequirementFile) {

        model = new JobModel(releaseId,
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

        model = new JobModel(releaseId,
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

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @POST
    public static FormValidation doTestPersonalAccessTokenConnection(final String username,
                                                                     final String personalAccessToken,
                                                                     final String tenantId,
                                                                     @AncestorInPath Job job) {
        job.checkPermission(Item.CONFIGURE);
        FodApiConnection testApi;
        String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
        String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
        String plainTextPersonalAccessToken = Utils.retrieveSecretDecryptedValue(personalAccessToken);
        if (Utils.isNullOrEmpty(baseUrl))
            return FormValidation.error("Fortify on Demand URL is empty!");
        if (Utils.isNullOrEmpty(apiUrl))
            return FormValidation.error("Fortify on Demand API URL is empty!");
        if (Utils.isNullOrEmpty(username))
            return FormValidation.error("Username is empty!");
        if (!Utils.isCredential(personalAccessToken))
            return FormValidation.error("Personal Access Token is empty!");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");
        testApi = new FodApiConnection(tenantId + "\\" + username, plainTextPersonalAccessToken, baseUrl, apiUrl, FodEnums.GrantType.PASSWORD, "api-tenant");
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
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getApplicationList(searchTerm, offset, limit);
    }

    public static org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> customFillUserApplicationById(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        ApplicationsController applicationsController = new ApplicationsController(apiConnection, null, null);
        org.jenkinsci.plugins.fodupload.models.Result<ApplicationApiResponse> result = applicationsController.getApplicationById(applicationId);

        return result;
    }

    public static List<MicroserviceApiResponse> customFillUserSelectedMicroserviceList(int applicationId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getMicroserviceListByApplication(applicationId);
    }

    public static GenericListResponse<ReleaseApiResponse> customFillUserSelectedReleaseList(int applicationId, int microserviceId, String searchTerm, Integer offset, Integer limit, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
        ApplicationsController applicationController = new ApplicationsController(apiConnection, null, null);
        return applicationController.getReleaseListByApplication(applicationId, microserviceId, searchTerm, offset, limit);
    }

    public static org.jenkinsci.plugins.fodupload.models.Result<ReleaseApiResponse> customFillUserReleaseById(int releaseId, AuthenticationModel authModel) throws IOException {
        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
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
            logger.println("Unexpected Error");
            build.setResult(Result.FAILURE);
            return false;
        }

        if (!model.getIsPipeline() && (model.getReleaseId() == null || model.getReleaseId().isEmpty()) && model.loadBsiToken() == false) {
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

            apiConnection = ApiConnectionFactory.createApiConnection(getAuthModel());
            if (apiConnection != null) {
                apiConnection.authenticate();

                StaticScanController staticScanController = new StaticScanController(apiConnection, logger, correlationId);

                if (releaseId <= 0 && model.loadBsiToken())
                    technologyStack = model.getBsiToken().getTechnologyStack();
                else if (model.getIsPipeline() || releaseId > 0)
                    technologyStack = model.getTechnologyStack();

                if (Utils.isNullOrEmpty(technologyStack)) {
                    GetStaticScanSetupResponse staticScanSetup = staticScanController.getStaticScanSettingsOld(releaseId);

                    if (staticScanSetup == null || Utils.isNullOrEmpty(staticScanSetup.getTechnologyStack())) {
                        logger.println("No scan settings defined for release " + releaseId);
                        build.setResult(Result.FAILURE);
                        return;
                    }

                    technologyStack = staticScanSetup.getTechnologyStack();
                }

                FilePath workspaceModified = new FilePath(workspace, model.getSrcLocation());
                File payload;
                if (model.getSelectedScanCentralBuildType().equalsIgnoreCase(FodEnums.SelectedScanCentralBuildType.None.toString())) {
                    // zips the file in a temporary location
                    payload = Utils.createZipFile(technologyStack, workspaceModified, logger);
                    if (payload.length() == 0) {
                        boolean deleteSuccess = payload.delete();
                        if (!deleteSuccess) {
                            logger.println("Unable to delete empty payload.");
                        }
                        logger.println("Source is empty for given Technology Stack and Language Level.");
                        build.setResult(Result.FAILURE);
                        return;
                    }
                } else {
                    FilePath scanCentralPath = null;

                    try {
                        String scsetting = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getScanCentralPath();

                        if (Utils.isNullOrEmpty(scsetting)) {
                            logger.println("ScanCentral location not set");
                            build.setResult(Result.FAILURE);
                        }
                        scanCentralPath = new FilePath(new File(scsetting));
                    } catch (Exception e) {
                        logger.println("Failed to retrieve ScanCentral location");
                        build.setResult(Result.FAILURE);
                    }

                    logger.println("Scan Central Path : " + scanCentralPath);
                    Path scPackPath = packageScanCentral(workspaceModified, scanCentralPath, workspace, model, logger, build);
                    logger.println("Packaged File Output Path : " + scPackPath);

                    if (scPackPath != null) {
                        payload = new File(scPackPath.toString());

                        if (!payload.exists()) {
                            build.setResult(Result.FAILURE);
                            return;
                        }
                    } else {
                        logger.println("Scan Central package output not found.");
                        build.setResult(Result.FAILURE);
                        return;
                    }
                }

                model.setPayload(payload);
                String notes = String.format("[%d] %s - Assessment submitted from Jenkins FoD Plugin",
                        build.getNumber(),
                        build.getDisplayName());

                StartScanResponse scanResponse = staticScanController.startStaticScan(releaseId, model, notes);
                boolean deleted = payload.delete();
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
                            logger.println("Unable to delete temporary zip file. Please manually delete file at location: " + payload.getAbsolutePath());
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

    public JobModel setModel(JobModel newModel) {
        return model = newModel;
    }

    public AuthenticationModel setAuthModel(AuthenticationModel newAuthModel) {
        return authModel = newAuthModel;
    }

    public JobModel getModel() {
        return model;
    }

    public int getScanId() {
        return scanId;
    }

    public int setScanId(int newScanId) {
        return scanId = newScanId;
    }

    private Path packageScanCentral(FilePath srcLocation, FilePath scanCentralLocation, FilePath outputLocation, JobModel job, PrintStream logger, Run<?, ?> build) {
        BufferedReader stdInputVersion = null, stdInput = null;
        String scexec = SystemUtils.IS_OS_WINDOWS ? "scancentral.bat" : "scancentral";

        try {
            //version check
            logger.println("Checking ScanCentralVersion");
            String scanCentralbatLocation = Paths.get(String.valueOf(scanCentralLocation)).resolve(scexec).toString();
            ArrayList scanCentralVersionCommandList = new ArrayList<>();
            scanCentralVersionCommandList.add(scanCentralbatLocation);
            scanCentralVersionCommandList.add("--version");
            Process pVersion = runProcessBuilder(scanCentralVersionCommandList, scanCentralLocation);
            stdInputVersion = new BufferedReader(new InputStreamReader(
                    pVersion.getInputStream()));
            String versionLine = null;
            String scanCentralVersion = null;
            Boolean isValidVersion = false;

            while ((versionLine = stdInputVersion.readLine()) != null) {
                logger.println(versionLine);
                if (versionLine.contains("version")) {

                    Pattern versionPattern = Pattern.compile("(?<=version:  ).*");
                    Matcher m = versionPattern.matcher(versionLine);

                    if (m.find()) {
                        scanCentralVersion = m.group().trim();

                        ComparableVersion minScanCentralVersion = new ComparableVersion("21.1.2.0002");
                        ComparableVersion userScanCentralVersion = new ComparableVersion(scanCentralVersion);

                        if (userScanCentralVersion.compareTo(minScanCentralVersion) < 0) {
                            logger.println("ScanCentral client version used is outdated. Update to the latest version provided on Tools page");
                            build.setResult(Result.FAILURE);
                            return null;
                        }
                        break;
                    }
                }
            }
            if (versionLine.contains("version")) {
                Path outputZipFolderPath = Paths.get(String.valueOf(outputLocation)).resolve("output.zip");
                FodEnums.SelectedScanCentralBuildType buildType = FodEnums.SelectedScanCentralBuildType.valueOf(model.getSelectedScanCentralBuildType());
                if (buildType == FodEnums.SelectedScanCentralBuildType.Gradle) {
                    logger.println("Giving permission to gradlew");
                    int permissionsExitCode = givePermissionsToGradle(srcLocation, logger);
                    logger.println("Finished Giving Permissions : " + permissionsExitCode);
                    if (permissionsExitCode != 0) {
                        logger.println("Errors giving permissions to gradle : " + permissionsExitCode);
                        build.setResult(Result.FAILURE);
                    }
                }
                ArrayList scanCentralPackageCommandList = new ArrayList<>();
                scanCentralPackageCommandList.add(scanCentralbatLocation);
                scanCentralPackageCommandList.add("package");
                scanCentralPackageCommandList.add("--bt");

                switch (buildType) {
                    case Gradle:
                        scanCentralPackageCommandList.add("gradle");
                        if (model.getScanCentralSkipBuild()) scanCentralPackageCommandList.add("--skipBuild");
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(model.getScanCentralBuildCommand());
                        }
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + model.getScanCentralBuildFile() + "\"");
                        }
                        break;
                    case Maven:
                        scanCentralPackageCommandList.add("mvn");
                        if (model.getScanCentralSkipBuild()) scanCentralPackageCommandList.add("--skipBuild");
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(model.getScanCentralBuildCommand());
                        }
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + model.getScanCentralBuildFile() + "\"");
                        }
                        break;
                    case MSBuild:
                        scanCentralPackageCommandList.add("msbuild");
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildCommand())) {
                            scanCentralPackageCommandList.add("--build-command");
                            scanCentralPackageCommandList.add(transformMsBuildCommand(model.getScanCentralBuildCommand()));
                        }
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildFile())) {
                            scanCentralPackageCommandList.add("--build-file");
                            scanCentralPackageCommandList.add("\"" + model.getScanCentralBuildFile() + "\"");
                        } else {
                            logger.println("Build File is a required field for msbuild build type. Please fill in the .sln file name in the current source folder ");
                            build.setResult(Result.FAILURE);
                        }
                        break;
                    case Python:
                        scanCentralPackageCommandList.add("none");
                        if (!Utils.isNullOrEmpty(model.getScanCentralVirtualEnv())) {
                            scanCentralPackageCommandList.add("--python-virtual-env");
                            scanCentralPackageCommandList.add(model.getScanCentralVirtualEnv());
                        }
                        ;
                        if (!Utils.isNullOrEmpty(model.getScanCentralRequirementFile())) {
                            scanCentralPackageCommandList.add("--python-requirements");
                            scanCentralPackageCommandList.add(model.getScanCentralRequirementFile());
                        }
                        ;
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildToolVersion())) {
                            scanCentralPackageCommandList.add("--python-version");
                            scanCentralPackageCommandList.add(model.getScanCentralBuildToolVersion());
                        }
                        ;
                        break;
                    case PHP:
                        scanCentralPackageCommandList.add("none");
                        if (!Utils.isNullOrEmpty(model.getScanCentralBuildToolVersion())) {
                            scanCentralPackageCommandList.add("--php-version");
                            scanCentralPackageCommandList.add(model.getScanCentralBuildToolVersion());
                        }
                        ;
                        break;
                }
                scanCentralPackageCommandList.add("--o");
                scanCentralPackageCommandList.add("\"" + outputZipFolderPath.toString() + "\"");

                logger.println("Packaging ScanCentral\n" + String.join(" ", scanCentralPackageCommandList));

                Process scanCentralProcess = runProcessBuilder(scanCentralPackageCommandList, srcLocation);
                stdInput = new BufferedReader(new InputStreamReader(scanCentralProcess.getInputStream()));
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    logger.println(s);
                }
                int exitCode = scanCentralProcess.waitFor();
                if (exitCode != 0) {
                    logger.println("Errors executing Scan Central. Exiting with errorcode : " + exitCode);
                    build.setResult(Result.FAILURE);
                } else {
                    return outputZipFolderPath;
                }
            } else {
                build.setResult(Result.FAILURE);
                logger.println("ScanCentral not found or invalid version");
            }
            return null;
        } catch (IOException | InterruptedException e) {
            logger.println(String.format("Failed executing scan central : ", e));
        } finally {
            try {
                if (stdInputVersion != null) {
                    stdInputVersion.close();
                }
                if (stdInput != null) {
                    stdInput.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private int givePermissionsToGradle(FilePath srcLocation, PrintStream logger) {
        if (!SystemUtils.IS_OS_WINDOWS) {
            BufferedReader stdInput = null;
            ArrayList linuxPermissionsList = new ArrayList<>();

            linuxPermissionsList.add("chmod");
            linuxPermissionsList.add("u+x");
            linuxPermissionsList.add("gradlew");

            try {
                if (linuxPermissionsList.size() > 0) {
                    Process gradlePermissionsProcess = runProcessBuilder(linuxPermissionsList, srcLocation);
                    stdInput = new BufferedReader(new InputStreamReader(gradlePermissionsProcess.getInputStream()));
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        logger.println(s);
                    }
                    return gradlePermissionsProcess.waitFor();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private String transformMsBuildCommand(String cmd) {
        if (!Utils.isNullOrEmpty(cmd)) {
            String[] arrOfCmds = cmd.split(" ");
            StringBuilder transformedCommands = new StringBuilder();
            for (String command : arrOfCmds) {
                if (command.charAt(0) == '-') {
                    command = '/' + command.substring(1);
                }
                transformedCommands.append(command).append(" ");
            }
            return transformedCommands.substring(0, transformedCommands.length() - 1);
        }
        return null;
    }

    private Process runProcessBuilder(ArrayList cmdList, FilePath directoryLocation) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.directory(new File(String.valueOf(directoryLocation)));
            Process p = pb.start();
            System.out.println(pb.redirectErrorStream());
            pb.redirectErrorStream(true);
            return p;
        } catch (IOException e) {
            throw e;
        }
    }
}
