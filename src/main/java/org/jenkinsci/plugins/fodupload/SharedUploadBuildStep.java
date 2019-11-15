package org.jenkinsci.plugins.fodupload;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;

import org.jenkinsci.plugins.fodupload.controllers.StaticScanController;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.JobModel;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;

public class SharedUploadBuildStep {

    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private JobModel model;
    private AuthenticationModel authModel;

    public SharedUploadBuildStep(String bsiToken,
                                 boolean overrideGlobalConfig,
                                 String username,
                                 String personalAccessToken,
                                 String tenantId,
                                 boolean purchaseEntitlements,
                                 String entitlementPreference,
                                 String srcLocation,
                                 String remediationScanPreferenceType,
                                 String inProgressScanActionType) {

        model = new JobModel(bsiToken,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType);

        username = Utils.encrypt(username);
        personalAccessToken = Utils.encrypt(personalAccessToken);
        tenantId = Utils.encrypt(tenantId);
                
        authModel = new AuthenticationModel(overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId);
    }

    public static FormValidation doCheckBsiToken(String bsiToken) {
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
        } else
            return FormValidation.error("Please specify BSI Token");
        return FormValidation.error("Please specify BSI Token");
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static FormValidation doTestPersonalAccessTokenConnection(final String username,
                                                                     final String personalAccessToken,
                                                                     final String tenantId) {
        FodApiConnection testApi;
        String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
        String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
        if (Utils.isNullOrEmpty(baseUrl))
            return FormValidation.error("Fortify on Demand URL is empty!");
        if (Utils.isNullOrEmpty(apiUrl))
            return FormValidation.error("Fortify on Demand API URL is empty!");
        if (Utils.isNullOrEmpty(username))
            return FormValidation.error("Username is empty!");
        if (Utils.isNullOrEmpty(personalAccessToken))
            return FormValidation.error("Personal Access Token is empty!");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");
        testApi = new FodApiConnection(tenantId + "\\" + username, personalAccessToken, baseUrl, apiUrl, FodEnums.GrantType.PASSWORD, "api-tenant");
        return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);

    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillEntitlementPreferenceItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.EntitlementPreferenceType preferenceType : FodEnums.EntitlementPreferenceType.values()) {
            items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.toString())));
        }

        return items;
    }

    @SuppressWarnings("unused")
    public static ListBoxModel doFillRemediationScanPreferenceTypeItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.RemediationScanPreferenceType remediationType : FodEnums.RemediationScanPreferenceType.values()) {
            items.add(new ListBoxModel.Option(remediationType.toString(), String.valueOf(remediationType.toString())));
        }
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

    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        final PrintStream logger = listener.getLogger();
        if (model == null) {
            logger.println("Unexpected Error");
            build.setResult(Result.FAILURE);
            return false;
        }

        if (model.initializeBuildModel() == false) {
            logger.println("Invalid BSI Token");
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
        try {
            taskListener.set(listener);

            // check to see if sensitive fields are encrypte. If not halt scan and recommend encryption.
            if(authModel != null)
            {
                if(authModel.getOverrideGlobalConfig() == true){
                    if(!Utils.isEncrypted(authModel.getPersonalAccessToken()) ||
                       !Utils.isEncrypted(authModel.getUsername()) ||
                       !Utils.isEncrypted(authModel.getTenantId()))
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
                        if(!Utils.isEncrypted(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalClientId()) ||
                           !Utils.isEncrypted(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalClientSecret()))
                        {
                            build.setResult(Result.UNSTABLE);
                            logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                            return ;
                        }
                    }
                    else
                    {
                         if(!Utils.isEncrypted(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalTenantId()) ||
                            !Utils.isEncrypted(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalUsername()) ||
                            !Utils.isEncrypted(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalPersonalAccessToken()) )
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

            if (model.getBsiToken() == null) { // Hack because pipeline step doesn't call prebuild
                model.initializeBuildModel();
            }

            FilePath workspaceModified = new FilePath(workspace, model.getSrcLocation());
            // zips the file in a temporary location
            File payload = Utils.createZipFile(model.getBsiToken().getTechnologyStack(), workspaceModified, logger);
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

            apiConnection = ApiConnectionFactory.createApiConnection(getAuthModel());
            if (apiConnection != null) {
                apiConnection.authenticate();

                StaticScanController staticScanController = new StaticScanController(apiConnection, logger);
                String notes = String.format("[%d] %s - Assessment submitted from Jenkins FoD Plugin",
                        build.getNumber(),
                        build.getDisplayName());

                boolean success = staticScanController.startStaticScan(model, notes);
                boolean deleted = payload.delete();

                if (success && deleted) {
                    logger.println("Scan Uploaded Successfully.");
                }
                build.setResult(success && deleted ? Result.SUCCESS : Result.UNSTABLE);
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
                                                                   Utils.decrypt(authModel.getUsername()),
                                                                   Utils.decrypt(authModel.getPersonalAccessToken()),
                                                                   Utils.decrypt(authModel.getTenantId()) );
       
        return displayModel;
    }
    
    public AuthenticationModel setAuthModel(AuthenticationModel newAuthModel) {
        return authModel = newAuthModel;
    }

    public JobModel getModel() {
        return model;
    }
}
