package org.jenkinsci.plugins.fodupload;

import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
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
import org.jenkinsci.plugins.fodupload.polling.PollReleaseStatusResult;
import org.jenkinsci.plugins.fodupload.polling.ScanStatusPoller;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.kohsuke.stapler.QueryParameter;

@SuppressWarnings("unused")
public class PollingBuildStep extends Recorder implements SimpleBuildStep {

    private static final BsiTokenParser tokenParser = new BsiTokenParser();
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String USERNAME = "username";
    private static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    private static final String TENANT_ID = "tenantId";

    private String bsiToken;
    private int pollingInterval;
    private int policyFailureBuildResultPreference;
    String projectAuthType;
    private String clientId;
    private String clientSecret;
    private String username;
    private String personalAccessToken;
    private AuthenticationModel authModel;

    @DataBoundConstructor
    public PollingBuildStep(String bsiToken,
                            String projectAuthType,
                            int pollingInterval,
                            int policyFailureBuildResultPreference,
                            String clientId,
                            String clientSecret,
                            String username,
                            String personalAccessToken,
                            String tenantId) {

        this.bsiToken = bsiToken;
        this.pollingInterval = pollingInterval;
        this.policyFailureBuildResultPreference = policyFailureBuildResultPreference;
        authModel = new AuthenticationModel(projectAuthType,
                                            clientId,
                                            clientSecret,
                                            username,
                                            personalAccessToken,
                                            tenantId);
         
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {

        final PrintStream logger = taskListener.getLogger();

        Result currentResult = run.getResult();
        if (Result.FAILURE.equals(currentResult)
                || Result.ABORTED.equals(currentResult)
                || Result.UNSTABLE.equals(currentResult)) {

            logger.println("Error: Build Failed or Unstable.  No reason to poll Fortify on Demand for results.");
            return;
        }

        if (this.getPollingInterval() <= 0) {
            logger.println("Error: Invalid polling interval (" + this.getPollingInterval() + " minutes)");
            run.setResult(Result.UNSTABLE);
        }

        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);

        try {

            BsiToken token = tokenParser.parse(this.bsiToken);
            apiConnection.authenticate();
            ScanStatusPoller poller = new ScanStatusPoller(apiConnection, this.pollingInterval, logger);
            PollReleaseStatusResult result = poller.pollReleaseStatus(token.getProjectVersionId());

            // if the polling fails, crash the build
            if (!result.isPollingSuccessful()) {
                run.setResult(Result.FAILURE);
                return;
            }

            if (!result.isPassing()) {

                PolicyFailureBuildResultPreference pref = PolicyFailureBuildResultPreference.fromInt(this.policyFailureBuildResultPreference);

                switch (pref) {

                    case MarkFailure:
                        run.setResult(Result.FAILURE);
                        break;

                    case MarkUnstable:
                        run.setResult(Result.UNSTABLE);
                        break;

                    case None:
                    default:
                        break;
                }
            }

        } catch (URISyntaxException e) {
            logger.println("Failed to parse BSI.");
            e.printStackTrace(logger);
        } finally {
            if (apiConnection != null) {
                apiConnection.retireToken();
            }
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @SuppressWarnings("unused")
    public String getBsiToken() {
        return bsiToken;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public int getPollingInterval() {
        return pollingInterval;
    }

    @SuppressWarnings("unused")
    public int getPolicyFailureBuildResultPreference() {
        return this.policyFailureBuildResultPreference;
    }

    
    @SuppressWarnings("unused")
    public String getClientId() {
        return clientId;
    }
    
    @SuppressWarnings("unused")
    public String getClientSecret() {
        return clientSecret;
    }
    
    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }
    
    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        return personalAccessToken;
    }
    
    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public PollingStepDescriptor getDescriptor() {
        return (PollingStepDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class PollingStepDescriptor extends BuildStepDescriptor<Publisher> {

        public PollingStepDescriptor() {
            super();
            load();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Poll Fortify on Demand for Results";
        }

           //testConnections
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        public FormValidation doTestApiKeyConnection(@QueryParameter(CLIENT_ID) final String clientId,
                                               @QueryParameter(CLIENT_SECRET) final String clientSecret)
        {
            FodApiConnection testApi;
            String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
            String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
            if (Utils.isNullOrEmpty(baseUrl))
                return FormValidation.error("Fortify on Demand URL is empty!");
            if (Utils.isNullOrEmpty(apiUrl))
                return FormValidation.error("Fortify on Demand API URL is empty!");
            if (Utils.isNullOrEmpty(clientId))
                return FormValidation.error("API Key is empty!");
            if (Utils.isNullOrEmpty(clientSecret))
                return FormValidation.error("Secret is empty!");
            testApi = new FodApiConnection(clientId, clientSecret, baseUrl, apiUrl, FodEnums.GrantType.CLIENT_CREDENTIALS, "api-tenant");
            return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        public FormValidation doTestPersonalAccessTokenConnection( @QueryParameter(USERNAME) final String username,
                                               @QueryParameter(PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                               @QueryParameter(TENANT_ID) final String tenantId)
        {
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
        public ListBoxModel doFillPolicyFailureBuildResultPreferenceItems() {
            ListBoxModel items = new ListBoxModel();
            for (PollingBuildStep.PolicyFailureBuildResultPreference preferenceType : PollingBuildStep.PolicyFailureBuildResultPreference.values()) {
                items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.getValue())));
            }

            return items;
        }
    }

    public boolean getAuthTypeIsApiKey()
    {
        return projectAuthType.equals("apiKeyType");
    }

    public boolean getAuthTypeIsPersonalToken()
    {
        return authModel.getProjectAuthType().equals("personalAccessTokenType");
    }
    
    public enum PolicyFailureBuildResultPreference {
        None(0),
        MarkUnstable(1),
        MarkFailure(2);

        private final int _val;

        PolicyFailureBuildResultPreference(int val) {
            this._val = val;
        }

        public int getValue() {
            return this._val;
        }

        public String toString() {
            switch (this._val) {
                case 2:
                    return "Mark Failure";
                case 1:
                    return "Mark Unstable";
                case 0:
                default:
                    return "Do nothing";
            }
        }

        public static PolicyFailureBuildResultPreference fromInt(int val) {
            switch (val) {
                case 2:
                    return MarkFailure;
                case 1:
                    return MarkUnstable;
                case 0:
                default:
                    return None;
            }
        }
    }
}
