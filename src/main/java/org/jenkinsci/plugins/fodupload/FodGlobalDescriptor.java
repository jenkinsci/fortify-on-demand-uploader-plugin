package org.jenkinsci.plugins.fodupload;

import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

@Extension
public class FodGlobalDescriptor extends GlobalConfiguration {
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String BASE_URL = "baseUrl";
    private static final String API_URL = "apiUrl";

    private String clientId;
    private String clientSecret;
    private String baseUrl;
    private String apiUrl;

    public FodGlobalDescriptor() {
        load();
    }

    // On save.
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        clientId = formData.getString(CLIENT_ID);
        clientSecret = formData.getString(CLIENT_SECRET);
        baseUrl = formData.getString(BASE_URL);
        apiUrl = formData.getString(API_URL);

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
    public String getClientId() {
        return clientId;
    }

    @SuppressWarnings("unused")
    public String getClientSecret() {
        return clientSecret;
    }

    @SuppressWarnings("unused")
    public String getBaseUrl() {
        return baseUrl;
    }

    @SuppressWarnings("unused")
    public String getApiUrl() {
        return apiUrl;
    }

    // Form validation
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
    public FormValidation doTestConnection(@QueryParameter(CLIENT_ID) final String clientId,
                                           @QueryParameter(CLIENT_SECRET) final String clientSecret,
                                           @QueryParameter(BASE_URL) final String baseUrl,
                                           @QueryParameter(API_URL) final String apiUrl) {
        if (Utils.isNullOrEmpty(clientId))
            return FormValidation.error("API Key is empty!");
        if (Utils.isNullOrEmpty(clientSecret))
            return FormValidation.error("Secret Key is empty!");
        if (Utils.isNullOrEmpty(baseUrl))
            return FormValidation.error("Fortify on Demand URL is empty!");
        if (Utils.isNullOrEmpty(apiUrl))
            return FormValidation.error("Fortify on Demand API URL is empty!");

        FodApiConnection testApi = new FodApiConnection(clientId, clientSecret, baseUrl, apiUrl);

        try {
            testApi.authenticate();
        } catch (IOException e) {
            return FormValidation.error("Unable to authenticate with Fortify on Demand");
        }

        String token = testApi.getToken();

        if (token == null) {
            return FormValidation.error("Unable to retrieve authentication token.");
        }

        return !token.isEmpty() ?
                FormValidation.ok("Successfully authenticated to Fortify on Demand.") :
                FormValidation.error("Invalid connection information. Please check your credentials and try again.");
    }

    FodApiConnection createFodApiConnection() {

        if (Utils.isNullOrEmpty(clientId))
            throw new NullPointerException("Client ID is null.");
        if (Utils.isNullOrEmpty(clientSecret))
            throw new NullPointerException("Client Secret is null.");
        if (Utils.isNullOrEmpty(baseUrl))
            throw new NullPointerException("Base URL is null.");
        if (Utils.isNullOrEmpty(apiUrl))
            throw new NullPointerException("Api URL is null.");

        return new FodApiConnection(clientId, clientSecret, baseUrl, apiUrl);
    }
}
