package org.jenkinsci.plugins.fodupload;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.Normalizer;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;

import org.jenkinsci.plugins.fodupload.controllers.StaticScanController;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums.InProgressBuildResultType;
import org.jenkinsci.plugins.fodupload.models.FodEnums.InProgressScanActionType;
import org.jenkinsci.plugins.fodupload.models.response.StartScanResponse;
import org.jenkinsci.plugins.fodupload.models.response.StaticScanSetupResponse;
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
                                 String inProgressBuildResultType) {

        model = new JobModel(releaseId,
                bsiToken,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType);

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
        }
        else {
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
                BsiToken testToken = tokenParser.parse(bsiToken);
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

    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        final PrintStream logger = listener.getLogger();
        if (model == null) {
            logger.println("Unexpected Error");
            build.setResult(Result.FAILURE);
            return false;
        }

        if ((model.getReleaseId() == null || model.getReleaseId().isEmpty()) && model.loadBsiToken() == false) {
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
                        Launcher launcher, TaskListener listener) {

        final PrintStream logger = listener.getLogger();
        FodApiConnection apiConnection = null;
        boolean isScanInProgress = false;

        try {
            taskListener.set(listener);

            // check to see if sensitive fields are encrypte. If not halt scan and recommend encryption.
            if(authModel != null)
            {
                if(authModel.getOverrideGlobalConfig() == true){
                    if(!Utils.isCredential(authModel.getPersonalAccessToken()))
                    {
                        build.setResult(Result.UNSTABLE);
                        logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                        return ;
                    }
                }
                else
                {
                    if(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getAuthTypeIsApiKey())
                    {
                        if(!Utils.isCredential(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalClientSecret()))
                        {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return ;
                        }
                    }
                    else
                    {
                         if(!Utils.isCredential(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalPersonalAccessToken()) )
                        {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return ;
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

            Integer releaseId = 0;
            try {
                releaseId = Integer.parseInt(model.getReleaseId());
            }
            catch (NumberFormatException ex) {}

            if (releaseId == 0 && !model.loadBsiToken()) {
                build.setResult(Result.FAILURE);
                logger.println("Invalid release ID or BSI Token");
                return;
            }


            if (releaseId > 0 && model.loadBsiToken()) {
                logger.println("Warning: The BSI Token will be ignored since Release ID was entered.");
            }

            String technologyStack = null;
            StaticScanSetupResponse staticScanSetup = null;

            apiConnection = ApiConnectionFactory.createApiConnection(getAuthModel());
            if (apiConnection != null) {
                apiConnection.authenticate();

                StaticScanController staticScanController = new StaticScanController(apiConnection, logger);

                if (releaseId == 0) {
                    model.loadBsiToken();
                    technologyStack = model.getBsiToken().getTechnologyStack();
                } else {
                    staticScanSetup = staticScanController.getStaticScanSettings(releaseId);
                    if (staticScanSetup == null) {
                        logger.println("No scan settings defined for release " + releaseId.toString());
                        build.setResult(Result.FAILURE);
                        return;
                    }

                    technologyStack = staticScanSetup.getTechnologyStack();
                }

                FilePath workspaceModified = new FilePath(workspace, model.getSrcLocation());
                // zips the file in a temporary location
                File payload = Utils.createZipFile(technologyStack, workspaceModified, logger);
                if (payload.length() == 0) {

                    boolean deleteSuccess = payload.delete();
                    if (!deleteSuccess) {
                        logger.println("Unable to delete empty payload.");
                    }

                    logger.println("Source is empty for given Technology Stack and Language Level.");
                    build.setResult(Result.FAILURE);
                    return;
                }

                model.setPayload(payload);

                String notes = String.format("[%d] %s - Assessment submitted from Jenkins FoD Plugin",
                        build.getNumber(),
                        build.getDisplayName());

                StartScanResponse scanResponse = staticScanController.startStaticScan(releaseId, staticScanSetup, model, notes);
                boolean deleted = payload.delete();

                boolean isWarningSettingEnabled = model.getInProgressBuildResultType().equalsIgnoreCase(InProgressBuildResultType.WarnBuild.getValue());
                boolean isQueueEnabled = model.getInProgressScanActionType().equalsIgnoreCase(InProgressScanActionType.Queue.getValue());
                /**
                 * If(able to contact api) {
                 *      if(No scan in progress && the uploaded file deleted) {
                 *          All good
                 *      }
                 *      else if (Scan in progress && user selected WarnBuild Build Action) {
                 *          Say all good
                 *          Set flag that stops anny additional FOD stuff
                 *      }
                 *      else (Scan is in progress && user selected FailBuild Build Action) {
                 *          Fail Build
                 *      }
                 * } else (unable to contact api) {
                 *      Fail Build
                 * }
                 */
                if (scanResponse.isSuccessful()) {
                    logger.println("Scan Uploaded Successfully.");
                    if(isQueueEnabled || !scanResponse.isScanInProgress() && deleted){
                        setScanId(scanResponse.getScanId());
                        build.setResult(Result.SUCCESS);
                    } else if (isWarningSettingEnabled) {
                        logger.println("Fortify scan skipped because another scan is in progress.");
                        isScanInProgress = true;
                        build.setResult(Result.UNSTABLE);
                    } else {
                        logger.println("Build failed because another scan is in progress and queuing not selected as in progress scan action in settings.");
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
            if (apiConnection != null && !isScanInProgress) {
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
                                                                   authModel.getTenantId() );
       
        return displayModel;
    }

    public JobModel setModel(JobModel newModel) { return model = newModel; }
    
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
}
