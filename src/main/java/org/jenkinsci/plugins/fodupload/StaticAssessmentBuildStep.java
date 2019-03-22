package org.jenkinsci.plugins.fodupload;

import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.fodupload.controllers.StaticScanController;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.FodEnums.GrantType;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;


@SuppressWarnings("unused")
public class StaticAssessmentBuildStep extends Recorder implements SimpleBuildStep {

    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String USERNAME = "username";
    private static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    private static final String TENANT_ID = "tenantId";
    private JobModel model;
    private AuthenticationModel authModel;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Entry point when building
    @DataBoundConstructor
    public StaticAssessmentBuildStep(String bsiToken,
                                    boolean overrideGlobalConfig,
                                    String username,
                                    String personalAccessToken,
                                    String tenantId,
                                    boolean includeAllFiles,
                                    boolean isBundledAssessment,
                                    boolean purchaseEntitlements,
                                    int entitlementPreference,
                                    boolean isRemediationPreferred,
                                    boolean runOpenSourceAnalysisOverride,
                                    boolean isExpressScanOverride,
                                    boolean isExpressAuditOverride,
                                    boolean includeThirdPartyOverride) throws URISyntaxException, UnsupportedEncodingException {

        model = new JobModel(bsiToken,
                includeAllFiles,
                isBundledAssessment,
                purchaseEntitlements,
                entitlementPreference,
                isRemediationPreferred,
                runOpenSourceAnalysisOverride,
                isExpressScanOverride,
                isExpressAuditOverride,
                includeThirdPartyOverride);
        
        authModel = new AuthenticationModel(overrideGlobalConfig,
                                            username,
                                            personalAccessToken,
                                            tenantId);
              
    }

   
    
    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) 
    {
        final PrintStream logger = listener.getLogger();
        if (model == null) {
            logger.println("Unexpected Error");
            build.setResult(Result.FAILURE);
            return false;
        }

        if(model.initializeBuildModel() == false){
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
    
    // logic run during a build
    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace,
                        @Nonnull Launcher launcher, @Nonnull TaskListener listener) {

        final PrintStream logger = listener.getLogger();
        FodApiConnection apiConnection = null;
        try {
            taskListener.set(listener);

            Result currentResult = build.getResult();
            if (Result.FAILURE.equals(currentResult)
                    || Result.ABORTED.equals(currentResult)
                    || Result.UNSTABLE.equals(currentResult)) {

                logger.println("Error: Build Failed or Unstable.  Halting with Fortify on Demand upload.");
                return;
            }

            logger.println("Starting FoD Upload.");

            // zips the file in a temporary location
            File payload = Utils.createZipFile(model.getBsiToken().getTechnologyStack(), workspace, logger);
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

            
             // Create apiConnection 
            apiConnection = ApiConnectionFactory.createApiConnection(authModel);
            if(apiConnection != null){
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
            }
            else
            {
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
                    logger.println("Failed to retire oauth token.");
                    e.printStackTrace(logger);
                } 
            }
        }
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public StaticAssessmentStepDescriptor getDescriptor() {
        return (StaticAssessmentStepDescriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension

    public static final class StaticAssessmentStepDescriptor extends BuildStepDescriptor<Publisher> {
        
        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        // Entry point when accessing global configuration
        public StaticAssessmentStepDescriptor() {
            super();
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }
       
        public FormValidation doCheckBsiToken(@QueryParameter String bsiToken)
        {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            if(bsiToken != null && !bsiToken.isEmpty() ){
                BsiTokenParser tokenParser = new BsiTokenParser();
                try{
                    BsiToken testToken = tokenParser.parse(bsiToken);
                    if(testToken != null){
                        return FormValidation.ok();
                    }
                }
                catch( Exception ex){
                    return FormValidation.error("Could not parse BSI token.");
                }  
            }
            else 
                return FormValidation.error("Please specify BSI Token");
            return FormValidation.error("Please specify BSI Token");
        }
        
        @Override
        public String getDisplayName() {
            return "Fortify on Demand Static Assessment";
        }
 

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection( @QueryParameter(USERNAME) final String username,
                                               @QueryParameter(PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                               @QueryParameter(TENANT_ID) final String tenantId)
        {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            FodApiConnection testApi;
            String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
            String apiUrl =  GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
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
            testApi = new FodApiConnection(tenantId + "\\" + username, personalAccessToken, baseUrl, apiUrl, GrantType.PASSWORD, "api-tenant");
            return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);

        }
        
        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            ListBoxModel items = new ListBoxModel();
            for (FodEnums.EntitlementPreferenceType preferenceType : FodEnums.EntitlementPreferenceType.values()) {
                items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.getValue())));
            }

            return items;
        }
        
        
    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    @SuppressWarnings("unused")
    public String getBsiToken() {
        return model.getBsiTokenOriginal();
    }

    @SuppressWarnings("unused")
    public String getUsername() {
        return authModel.getUsername();
    }
    
    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        return authModel.getPersonalAccessToken();
    }
    
    @SuppressWarnings("unused")
    public String getTenantId() {
        return authModel.getTenantId();
    }
    
    @SuppressWarnings("unused")
    public boolean getOverrideGlobalConfig() {
        return authModel.getOverrideGlobalConfig();
    }
    
    
    @SuppressWarnings("unused")
    public boolean getIncludeAllFiles() {
        return model.isIncludeAllFiles();
    }

    @SuppressWarnings("unused")
    public int getEntitlementPreference() {
        return model.getEntitlementPreference();
    }

    @SuppressWarnings("unused")
    public boolean getIsBundledAssessment() {
        return model.isBundledAssessment();
    }

    @SuppressWarnings("unused")
    public boolean getPurchaseEntitlements() {
        return model.isPurchaseEntitlements();
    }

    @SuppressWarnings("unused")
    public boolean getIsRemediationPreferred() {
        return model.isRemediationPreferred();
    }

    @SuppressWarnings("unused")
    public boolean getRunOpenSourceAnalysisOverride() {
        return model.isRunOpenSourceAnalysisOverride();
    }

    @SuppressWarnings("unused")
    public boolean getIsExpressScanOverride() {
        return model.isExpressScanOverride();
    }

    @SuppressWarnings("unused")
    public boolean getIsExpressAuditOverride() {
        return model.isExpressAuditOverride();
    }

    @SuppressWarnings("unused")
    public boolean getIncludeThirdPartyOverride() {
        return model.isIncludeThirdPartyOverride();
    }
    
    
}
