package org.jenkinsci.plugins.fodupload;

import java.io.IOException;
import java.io.PrintStream;

import com.cloudbees.plugins.credentials.CredentialsProvider;

import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.BsiToken;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.response.ScanSummaryDTO;
import org.jenkinsci.plugins.fodupload.polling.PollReleaseStatusResult;
import org.jenkinsci.plugins.fodupload.polling.ScanStatusPoller;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.Launcher;
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

import static org.jenkinsci.plugins.fodupload.Utils.FOD_URL_ERROR_MESSAGE;
import static org.jenkinsci.plugins.fodupload.Utils.isValidUrl;


public class SharedPollingBuildStep {

    public static final BsiTokenParser tokenParser = new BsiTokenParser();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private String releaseId;
    private String bsiToken;
    private int pollingInterval;
    private int scanId;
    private String correlationId;

    private int policyFailureBuildResultPreference;

    private AuthenticationModel authModel;

    public SharedPollingBuildStep(String releaseId,
                                  String bsiToken,
                                  boolean overrideGlobalConfig,
                                  int pollingInterval,
                                  int policyFailureBuildResultPreference,
                                  String clientId,
                                  String clientSecret,
                                  String username,
                                  String personalAccessToken,
                                  String tenantId) {

        this.releaseId = releaseId;
        this.bsiToken = bsiToken;
        this.pollingInterval = pollingInterval;
        this.policyFailureBuildResultPreference = policyFailureBuildResultPreference;
        this.scanId = -1;
        this.correlationId = "";
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

    public static FormValidation doCheckPollingInterval(String pollingInterval) {
        if (Utils.isNullOrEmpty(pollingInterval))
            return FormValidation.error("Polling interval is required to perform this step.");
        try {
            int pollingIntervalNumeric = Integer.parseInt(pollingInterval);
            if (pollingIntervalNumeric <= 0)
                return FormValidation.error("Value must be greater than 0");
        } catch (NumberFormatException ex) {
            return FormValidation.error("Value must be integer");
        }
        return FormValidation.ok();
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
            return FormValidation.error("Personal Access Token is empty or needs to be resaved!");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");

        testApi = new FodApiConnection(tenantId + "\\" + username, plainTextPersonalAccessToken, baseUrl, apiUrl, FodEnums.GrantType.PASSWORD, "api-tenant", false, null, null);
        return GlobalConfiguration.all().get(FodGlobalDescriptor.class).testConnection(testApi);
    }

    public static ListBoxModel doFillPolicyFailureBuildResultPreferenceItems() {
        ListBoxModel items = new ListBoxModel();
        for (PolicyFailureBuildResultPreference preferenceType : PolicyFailureBuildResultPreference.values()) {
            items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.getValue())));
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

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> run,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener taskListener) throws InterruptedException, IOException {

        final PrintStream logger = taskListener.getLogger();
        boolean isRemoteAgent = workspace.isRemote();

        // check to see if sensitive fields are encrypte. If not halt scan and recommend encryption.
        if (authModel != null) {
            if (authModel.getOverrideGlobalConfig() == true) {
                if (!Utils.isCredential(authModel.getPersonalAccessToken())) {
                    run.setResult(Result.UNSTABLE);
                    logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates");
                    return;
                }
            } else {
                if (GlobalConfiguration.all().get(FodGlobalDescriptor.class).getAuthTypeIsApiKey()) {
                    if (!Utils.isCredential(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalClientSecret())) {
                        run.setResult(Result.UNSTABLE);
                        logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates");
                        return;
                    }
                } else {
                    if (!Utils.isCredential(GlobalConfiguration.all().get(FodGlobalDescriptor.class).getOriginalPersonalAccessToken())) {
                        run.setResult(Result.UNSTABLE);
                        logger.println("Credentials must be re-entered for security purposes. Please update on the global configuration and/or post-build actions and then save your updates.");
                        return;
                    }
                }
            }
        }

        Result currentResult = run.getResult();
        if (Result.FAILURE.equals(currentResult)
                || Result.ABORTED.equals(currentResult)
                || Result.UNSTABLE.equals(currentResult)) {

            logger.println("Error: Build Failed or Unstable.  No reason to poll Fortify on Demand for results.");
            return;
        }

        // Magic numbers are awful but this eliminates most null errors.
        if (scanId == -1) {
            logger.println("Error: Unable to retrieve scan ID. Exiting FOD scan.");
            run.setResult(Result.UNSTABLE);
            return;
        }

        if (this.getPollingInterval() <= 0) {
            logger.println("Error: Invalid polling interval (" + this.getPollingInterval() + " minutes)");
            run.setResult(Result.UNSTABLE);
            return;
        }

        FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(getAuthModel(), isRemoteAgent, launcher, logger);

        try {

            Integer releaseIdNum = 0;
            if (releaseId != null && !releaseId.isEmpty()) {
                try {
                    releaseIdNum = Integer.parseInt(releaseId);
                } catch (NumberFormatException ex) {
                }
            }

            if (releaseIdNum == 0 && (this.getBsiToken() == null || this.getBsiToken().isEmpty())) {
                run.setResult(Result.FAILURE);
                logger.println("Invalid release ID or BSI Token");
                return;
            }

            if (releaseIdNum > 0 && this.getBsiToken() != null && !this.getBsiToken().isEmpty()) {
                logger.println("Warning: The BSI Token will be ignored since Release ID was entered.");
            }

            BsiToken token = releaseIdNum == 0 ? tokenParser.parseBsiToken(this.getBsiToken()) : null;
            if (apiConnection != null) {
                ScanStatusPoller poller = new ScanStatusPoller(apiConnection, this.getPollingInterval(), logger);
                PollReleaseStatusResult result = poller.pollReleaseStatus(releaseIdNum == 0 ? token.getReleaseId() : releaseIdNum, scanId, correlationId);

                // if the polling fails, crash the build
                if (!result.isPollingSuccessful()) {
                    if (result.isScanUploadAccepted()) {
                        run.setResult(Result.UNSTABLE);
                    } else {
                        run.setResult(Result.FAILURE);
                    }

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
                ScanSummaryDTO summaryDTO = poller.getScanSummary(Integer.parseInt(releaseId), scanId);
                poller.printScanSummary(summaryDTO);
            } else {
                logger.println("Failed to authenticate");
                run.setResult(Result.FAILURE);
            }
        } finally {
            if (apiConnection != null) {
                apiConnection.retireToken();
            }
        }
    }

    public String getReleaseId() {
        return releaseId;
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

    public int getUploadScanId() {
        return scanId;
    }

    public void setUploadScanId(int uploadScanId) {
        this.scanId = uploadScanId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public AuthenticationModel getAuthModel() {
        AuthenticationModel displayModel = new AuthenticationModel(authModel.getOverrideGlobalConfig(),
                authModel.getUsername(),
                authModel.getPersonalAccessToken(),
                authModel.getTenantId());

        return displayModel;
    }

    public enum PolicyFailureBuildResultPreference {
        None(0),
        MarkUnstable(1),
        MarkFailure(2);

        private final int _val;

        PolicyFailureBuildResultPreference(int val) {
            this._val = val;
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
    }
}
