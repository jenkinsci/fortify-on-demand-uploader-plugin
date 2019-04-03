package org.jenkinsci.plugins.fodupload;

import com.fortify.fod.parser.BsiToken;
import com.fortify.fod.parser.BsiTokenParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.polling.PollReleaseStatusResult;
import org.jenkinsci.plugins.fodupload.polling.ScanStatusPoller;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class SharedPollingBuildStep {

    public static final BsiTokenParser tokenParser = new BsiTokenParser();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private String bsiToken;
    private int pollingInterval;

    private int policyFailureBuildResultPreference;

    private AuthenticationModel authModel;

    public SharedPollingBuildStep(String bsiToken,
                            boolean overrideGlobalConfig,
                            int pollingInterval,
                            int policyFailureBuildResultPreference,
                            String clientId,
                            String clientSecret,
                            String username,
                            String personalAccessToken,
                            String tenantId){

        this.bsiToken = bsiToken;
        this.pollingInterval = pollingInterval;
        this.policyFailureBuildResultPreference = policyFailureBuildResultPreference;
        authModel = new AuthenticationModel(overrideGlobalConfig,
                username,
                personalAccessToken,
                tenantId);
    }

    public void perform(Run<?, ?> run,
                        FilePath filePath,
                        Launcher launcher,
                        TaskListener taskListener) throws InterruptedException, IOException {

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
            return;
        }

        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(getAuthModel());

        try {

            BsiToken token = tokenParser.parse(this.getBsiToken());
            if(apiConnection != null)
            {
                apiConnection.authenticate();
                ScanStatusPoller poller = new ScanStatusPoller(apiConnection, this.getPollingInterval(), logger);
                PollReleaseStatusResult result = poller.pollReleaseStatus(token.getProjectVersionId());

                // if the polling fails, crash the build
                if (!result.isPollingSuccessful()) {
                    run.setResult(Result.FAILURE);
                    return;
                }

                if (!result.isPassing()) {

                    PolicyFailureBuildResultPreference pref = PolicyFailureBuildResultPreference.fromInt(this.getPolicyFailureBuildResultPreference());

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
            }else{
                logger.println("Failed to authenticate");
                run.setResult(Result.FAILURE);
            }

        } catch (URISyntaxException e) {
            logger.println("Failed to parse BSI.");
        } finally {
            if (apiConnection != null) {
                apiConnection.retireToken();
            }
        }
    }

    public String getBsiToken() {
        return bsiToken;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public int getPolicyFailureBuildResultPreference() {
        return policyFailureBuildResultPreference;
    }

    public AuthenticationModel getAuthModel() {
        return authModel;
    }


    public static FormValidation doCheckBsiToken(String bsiToken)
    {
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


    public static FormValidation doCheckPollingInterval(String pollingInterval)
    {
        if(Utils.isNullOrEmpty(pollingInterval))
            return FormValidation.error("Polling interval is required to perform this step.");
        try {
            int pollingIntervalNumeric = Integer.parseInt(pollingInterval);
            if(pollingIntervalNumeric <= 0)
                return FormValidation.error("Value must be greater than 0");
        }
        catch (NumberFormatException ex)
        {
            return FormValidation.error("Value must be integer");
        }
        return FormValidation.ok();
    }

    // Form validation
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static FormValidation doTestPersonalAccessTokenConnection(final String username,
                                                               final String personalAccessToken,
                                                               final String tenantId)
    {
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
        testApi = new FodApiConnection(tenantId + "\\" + username, personalAccessToken, baseUrl, apiUrl, FodEnums.GrantType.PASSWORD, "api-tenant");
        return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);

    }

    public static ListBoxModel doFillPolicyFailureBuildResultPreferenceItems() {
        ListBoxModel items = new ListBoxModel();
        for (PolicyFailureBuildResultPreference preferenceType : PolicyFailureBuildResultPreference.values()) {
            items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.getValue())));
        }

        return items;
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
