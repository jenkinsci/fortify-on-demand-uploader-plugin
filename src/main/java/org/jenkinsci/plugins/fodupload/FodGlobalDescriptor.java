package org.jenkinsci.plugins.fodupload;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.FodEnums.GrantType;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import java.io.IOException;
import java.io.PrintStream;

import static org.jenkinsci.plugins.fodupload.Utils.FOD_BASEURL_ERROR_MESSAGE;
import static org.jenkinsci.plugins.fodupload.Utils.FOD_APIURL_ERROR_MESSAGE;
import static org.jenkinsci.plugins.fodupload.Utils.isValidUrl;


@Extension
@Symbol("FodGlobal")
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

    @DataBoundConstructor
    public FodGlobalDescriptor() {
        super();
        load();
    }

    /****************************************************
     *   Maura E. Ardden: 09/15/2022
     *   To enable configuration-as-code a new group of setters has been provided
     *   NOTE: Following setters persist data to the global configuration json Object and jelly files.
     *         All setters use the naming convention: set JellyField.
     *         All getters use the naming convention: get JellyField.
     */

    // Maura E. Ardden: 09/15/2022
    // Added setters (all @DataBound) to support Stapler
    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = isValidUrl(baseUrl);
    }

    @DataBoundSetter
    public void setApiUrl(String apiUrl) {
        this.apiUrl = isValidUrl(apiUrl);
    }

    @DataBoundSetter
    public void setGlobalAuthType(String globalAuthType) {
        this.globalAuthType = Util.fixEmptyAndTrim(globalAuthType);
    }

    @DataBoundSetter
    public void setClientId(String clientId) {
        this.clientId = Util.fixEmptyAndTrim(clientId);
    }

    @DataBoundSetter
    public void setClientSecret(String clientSecret) {
        this.clientSecret = Util.fixEmptyAndTrim(clientSecret);
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = Util.fixEmptyAndTrim(username);
    }

    @DataBoundSetter
    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = Util.fixEmptyAndTrim(personalAccessToken);
    }

    @DataBoundSetter
    public void setTenantId(String tenantId) {
        this.tenantId = Util.fixEmptyAndTrim(tenantId);
    }

    @DataBoundSetter
    public void setScanCentralPath(String scanCentralPath) {
        this.scanCentralPath = Util.fixEmptyAndTrim(scanCentralPath);
    }


    public boolean getAuthTypeIsApiKey() {
        if (globalAuthType == null) return false;
        return globalAuthType.equals("apiKeyType");
    }

    public boolean getAuthTypeIsPersonalToken() {
        if (globalAuthType == null) return false;
        return globalAuthType.equals("personalAccessTokenType");
    }

    public String getGlobalAuthType() {
        return globalAuthType;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getUsername() {
        return username;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getScanCentralPath() {
        return scanCentralPath;
    }

    @Override
    public String getDisplayName() {
        return "Fortify On Demand Uploader Plugin";
    }

    @SuppressWarnings("unused")
    public String getOriginalClientId() {
        return clientId;
    }

    @SuppressWarnings("unused")
    public String getOriginalClientSecret() {
        return clientSecret;
    }

    @SuppressWarnings("unused")
    public String getOriginalUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public String getOriginalPersonalAccessToken() {
        return personalAccessToken;
    }

    @SuppressWarnings("unused")
    public String getOriginalTenantId() {
        return tenantId;
    }

    /*
        Maura E. Ardden: 09/15/2022

        In the configure method, replaced "return super.configure(req,formData)" with "return true"
        After enabling jCasC, super.configure(Stapler request, JSONObject formData) throws:

        "org.kohsuke.stapler.NoStaplerConstructorException:
            There's no @DataBoundConstructor on any constructor of class java.lang.String"

        JSONObject data persistence is part of jCasC feature (key/value pairs exist in jCasC yml file).
        The file updates org.jenkinsci.plugins.fodupload.FodGlobalDescriptor.xml configuration
        artifact when imported.

        The Stapler request update occurs upon invocation of the save() method.

        Ideally, the plugin would use the Stapler request's bindJSON for a bulk update, but
        to upset the existing code the minimum, having the configure method return "true"
        was the least intrusive approach.
    */

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

        //return super.configure(req, formData);
        return true;
    }


    /*
        Maura E. Ardden: 09/15/2022

        Added URL validation using org.jenkinsci.plugins.fodupload.Utils.isValidUrl(url) on
           doTestApiKeyConnection
           doTestPersonalAccessTokenConnection

        Depending on the URL, the form displays a custom error message:
        FOD_APIURL_ERROR_MESSAGE or FOD_APIURL_ERROR_MESSAGE
    */


    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
    @POST
    public FormValidation doTestApiKeyConnection(@QueryParameter(CLIENT_ID) final String clientId,
                                                 @QueryParameter(CLIENT_SECRET) final String clientSecret,
                                                 @QueryParameter(BASE_URL) final String baseUrl,
                                                 @QueryParameter(API_URL) final String apiUrl) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);
        FodApiConnection testApi;
        String plainTextClientSecret = Utils.retrieveSecretDecryptedValue(clientSecret);

        if (Utils.isNullOrEmpty(isValidUrl(baseUrl)))
            return FormValidation.error(FOD_BASEURL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(isValidUrl(apiUrl)))
            return FormValidation.error(FOD_APIURL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(clientId))
            return FormValidation.error("API Key is empty!");
        if (!Utils.isCredential(clientSecret))
            return FormValidation.error("Secret Key is empty or needs to be resaved!");

        testApi = new FodApiConnection(clientId, plainTextClientSecret, baseUrl, apiUrl, GrantType.CLIENT_CREDENTIALS, "api-tenant", false, null, null);
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
        /*

         */
        if (Utils.isNullOrEmpty(isValidUrl(baseUrl)))
            return FormValidation.error(FOD_BASEURL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(isValidUrl(apiUrl)))
            return FormValidation.error(FOD_APIURL_ERROR_MESSAGE);
        if (Utils.isNullOrEmpty(username))
            return FormValidation.error("Username is empty!");
        if (!Utils.isCredential(personalAccessToken))
            return FormValidation.error("Personal Access Token is empty! Please update and save credentials.");
        if (Utils.isNullOrEmpty(tenantId))
            return FormValidation.error("Tenant ID is null.");

        testApi = new FodApiConnection(tenantId + "\\" + username, plainTextPersonalAccessToken, baseUrl, apiUrl, GrantType.PASSWORD, "api-tenant", false, null, null);
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

    FodApiConnection createFodApiConnection(boolean executeOnRemoteAgent, Launcher launcher, PrintStream logger) {

        if (!Utils.isNullOrEmpty(globalAuthType)) {

            if (Utils.isNullOrEmpty(isValidUrl(baseUrl)))
                throw new IllegalArgumentException(FOD_BASEURL_ERROR_MESSAGE);
            if (Utils.isNullOrEmpty(isValidUrl(apiUrl)))
                throw new IllegalArgumentException(FOD_APIURL_ERROR_MESSAGE);
            if (globalAuthType.equals("apiKeyType")) {
                if (Utils.isNullOrEmpty(clientId))
                    throw new IllegalArgumentException("Client ID is null.");
                if (Utils.isNullOrEmpty(clientSecret))
                    throw new IllegalArgumentException("Client Secret is null.");
                return new FodApiConnection(clientId, Utils.retrieveSecretDecryptedValue(clientSecret), baseUrl, apiUrl, GrantType.CLIENT_CREDENTIALS, "api-tenant", executeOnRemoteAgent, launcher, logger);
            } else if (globalAuthType.equals("personalAccessTokenType")) {
                if (Utils.isNullOrEmpty(username))
                    throw new IllegalArgumentException("Username is null.");
                if (Utils.isNullOrEmpty(personalAccessToken))
                    throw new IllegalArgumentException("Personal Access Token is null.");
                if (Utils.isNullOrEmpty(tenantId))
                    throw new IllegalArgumentException("Tenant ID is null.");
                return new FodApiConnection(tenantId + "\\" + username, Utils.retrieveSecretDecryptedValue(personalAccessToken), baseUrl, apiUrl, GrantType.PASSWORD, "api-tenant", executeOnRemoteAgent, launcher, logger);
            } else {
                throw new IllegalArgumentException("Invalid authentication type");
            }

        } else {
            throw new IllegalArgumentException("No authentication method configured");
        }

    }

    public FormValidation testConnection(FodApiConnection testApi) {
        try {
            String error = testApi.testConnection();

            if (error != null && !error.isEmpty()) return FormValidation.error(error);
            return FormValidation.ok("Successfully authenticated to Fortify on Demand.");
        } catch (IOException e) {
            return FormValidation.error("Unable to authenticate with Fortify on Demand. Error Message: " + e.getMessage());
        }
    }

    private ListBoxModel doFillStringCredentialsItems() {
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