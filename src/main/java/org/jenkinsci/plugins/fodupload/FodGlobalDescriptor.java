package org.jenkinsci.plugins.fodupload;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

import jenkins.model.Jenkins;
import org.jenkinsci.plugins.fodupload.models.FodEnums.GrantType;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.verb.POST;

@Extension
public class FodGlobalDescriptor extends GlobalConfiguration {
    private static final String CLIENT_ID = "clientId";
    private static final String GLOBAL_AUTH_TYPE = "globalAuthType";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String USERNAME = "username";
    private static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    private static final String TENANT_ID = "tenantId";
    private static final String BASE_URL = "baseUrl";
    private static final String API_URL = "apiUrl";
    private static final String SCANCENTRAL_PATH = "scanCentralPath";

    private String globalAuthType;
    private String clientId;
    private String clientSecret;
    private String username;
    private String personalAccessToken;
    private String tenantId;
    private String baseUrl;
    private String apiUrl;
    private String scanCentralPath;

    public FodGlobalDescriptor() {
        load();
    }

    // On save.
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        JSONObject globalAuthTypeObject = formData.getJSONObject(GLOBAL_AUTH_TYPE);
        if (globalAuthTypeObject.size() > 0) {
            globalAuthType = globalAuthTypeObject.getString("value");
            if (globalAuthType.equals("apiKeyType")) {
                clientId = globalAuthTypeObject.getString(CLIENT_ID);
                clientSecret = globalAuthTypeObject.getString(CLIENT_SECRET);
            } else if (globalAuthType.equals("personalAccessTokenType")) {
                username = globalAuthTypeObject.getString(USERNAME);
                personalAccessToken = globalAuthTypeObject.getString(PERSONAL_ACCESS_TOKEN);
                tenantId = globalAuthTypeObject.getString(TENANT_ID);
            }
        }
        baseUrl = formData.getString(BASE_URL);
        apiUrl = formData.getString(API_URL);
        scanCentralPath = formData.getString(SCANCENTRAL_PATH);

        save();

        return super.configure(req, formData);
    }

    // NOTE: The following Getters are used to return saved values in the jelly files. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    public String getDisplayName() {
        return "Fortify Uploader Plugin";
    }

    @SuppressWarnings("unused")
    public String getGlobalAuthType() {
        return globalAuthType;
    }

    @SuppressWarnings("unused")
    public String getClientId() {
        return clientId;
    }

    public String getOriginalClientId() {
        return clientId;
    }
    
    @SuppressWarnings("unused")
    public String getClientSecret() {
        return clientSecret;
    }

    public String getOriginalClientSecret() {
        return clientSecret;
    }
    
    @SuppressWarnings("unused")
    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public String getOriginalUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    @SuppressWarnings("unused")
    public String getOriginalPersonalAccessToken() {
        return personalAccessToken;
    }
    
    @SuppressWarnings("unused")
    public String getTenantId() {
        return tenantId;
    }

    @SuppressWarnings("unused")
    public String getOriginalTenantId() {
        return tenantId;
    }

    @SuppressWarnings("unused")
    public String getBaseUrl() {
        return baseUrl;
    }

    @SuppressWarnings("unused")
    public String getApiUrl() {
        return apiUrl;
    }

    @SuppressWarnings("unused")
    public String getScanCentralPath() {
        return scanCentralPath;
    }

    public boolean getAuthTypeIsApiKey() {
        if(globalAuthType == null) return false;
        return globalAuthType.equals("apiKeyType") ;
    }

    public boolean getAuthTypeIsPersonalToken() {
        if(globalAuthType == null) return false;
        return globalAuthType.equals("personalAccessTokenType");
    }

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
    @POST
    public FormValidation doTestApiKeyConnection(@QueryParameter(CLIENT_ID) final String clientId,
                                                 @QueryParameter(CLIENT_SECRET) final String clientSecret,
                                                 @QueryParameter(BASE_URL) final String baseUrl,
                                                 @QueryParameter(API_URL) final String apiUrl) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        FodApiConnection testApi;
        String plainTextClientSecret= Utils.retrieveSecretDecryptedValue(clientSecret);
        if (Utils.isNullOrEmpty(baseUrl))
            return FormValidation.error("Fortify on Demand URL is empty!");
        if (Utils.isNullOrEmpty(apiUrl))
            return FormValidation.error("Fortify on Demand API URL is empty!");
        if (Utils.isNullOrEmpty(clientId))
            return FormValidation.error("API Key is empty!");
        if (!Utils.isCredential(clientSecret))
            return FormValidation.error("Secret Key is empty or needs to be resaved!");
        testApi = new FodApiConnection(clientId, plainTextClientSecret, baseUrl, apiUrl, GrantType.CLIENT_CREDENTIALS, "api-tenant");
        return testConnection(testApi);
    }

    // Form validation
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
    @POST
    public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter(USERNAME) final String username,
                                                              @QueryParameter(PERSONAL_ACCESS_TOKEN) final String personalAccessToken,
                                                              @QueryParameter(TENANT_ID) final String tenantId,
                                                              @QueryParameter(BASE_URL) final String baseUrl,
                                                              @QueryParameter(API_URL) final String apiUrl) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        FodApiConnection testApi;
        String plainTextPersonalAccessToken = Utils.retrieveSecretDecryptedValue(personalAccessToken);
        if (Utils.isNullOrEmpty(baseUrl))
            return FormValidation.error("Fortify on Demand URL is empty!");
        if (Utils.isNullOrEmpty(apiUrl))
            return FormValidation.error("Fortify on Demand API URL is empty!");
        if (Utils.isNullOrEmpty(username))
            return FormValidation.error("Username is empty!");
        if (!Utils.isCredential(personalAccessToken))
            return FormValidation.error("Personal Access Token is empty! Please update and save credentials.");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");
        testApi = new FodApiConnection(tenantId + "\\" + username, plainTextPersonalAccessToken, baseUrl, apiUrl, GrantType.PASSWORD, "api-tenant");
        return testConnection(testApi);

    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillClientIdItems() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return doFillStringCredentialsItems();
    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillClientSecretItems() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return doFillStringCredentialsItems();
    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillUsernameItems() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return doFillStringCredentialsItems();
    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillPersonalAccessTokenItems() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return doFillStringCredentialsItems();
    }

    @SuppressWarnings("unused")
    public ListBoxModel doFillTenantIdItems() {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        return doFillStringCredentialsItems();
    }

    FodApiConnection createFodApiConnection() {

        if (!Utils.isNullOrEmpty(globalAuthType)) {

            if (Utils.isNullOrEmpty(baseUrl))
                throw new IllegalArgumentException("Base URL is null.");
            if (Utils.isNullOrEmpty(apiUrl))
                throw new IllegalArgumentException("Api URL is null.");

            if (globalAuthType.equals("apiKeyType")) {
                if (Utils.isNullOrEmpty(clientId))
                    throw new IllegalArgumentException("Client ID is null.");
                if (Utils.isNullOrEmpty(clientSecret))
                    throw new IllegalArgumentException("Client Secret is null.");
                return new FodApiConnection(clientId, Utils.retrieveSecretDecryptedValue(clientSecret), baseUrl, apiUrl, GrantType.CLIENT_CREDENTIALS, "api-tenant");
            } else if (globalAuthType.equals("personalAccessTokenType")) {
                if (Utils.isNullOrEmpty(username))
                    throw new IllegalArgumentException("Username is null.");
                if (Utils.isNullOrEmpty(personalAccessToken))
                    throw new IllegalArgumentException("Personal Access Token is null.");
                if (Utils.isNullOrEmpty(tenantId))
                    throw new IllegalArgumentException("Tenant ID is null.");
                return new FodApiConnection(tenantId + "\\" + username, Utils.retrieveSecretDecryptedValue(personalAccessToken), baseUrl, apiUrl, GrantType.PASSWORD, "api-tenant");
            } else {
                throw new IllegalArgumentException("Invalid authentication type");
            }

        } else {
            throw new IllegalArgumentException("No authentication method configured");
        }

    }

    public FormValidation testConnection(FodApiConnection testApi) {
        try {
            testApi.authenticate();
        } catch (IOException e) {
            return FormValidation.error("Unable to authenticate with Fortify on Demand. Error Message: " + e.getMessage());
        }

        String token = testApi.getToken();

        if (token == null) {
            return FormValidation.error("Unable to retrieve authentication token.");
        }

            return !token.isEmpty() ?
                FormValidation.ok("Successfully authenticated to Fortify on Demand.") :
                FormValidation.error("Invalid connection information. Please check your credentials and try again.");
    }

    private ListBoxModel doFillStringCredentialsItems(){
        ListBoxModel items = CredentialsProvider.listCredentials(
                StringCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                null,
                null
                );
        return items;
    }
    
    
}
