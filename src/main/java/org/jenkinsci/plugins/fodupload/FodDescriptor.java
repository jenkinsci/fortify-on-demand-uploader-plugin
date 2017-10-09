package org.jenkinsci.plugins.fodupload;


import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class FodDescriptor extends BuildStepDescriptor<Publisher> {
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String BASE_URL = "baseUrl";

    private FodApi api;
    private String clientId;
    private String clientSecret;
    private String baseUrl;

    // On save.
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        clientId = formData.getString(CLIENT_ID);
        clientSecret = formData.getString(CLIENT_SECRET);
        baseUrl = formData.getString(BASE_URL);

        save();

        api = createFodApi();
        return super.configure(req, formData);
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        // Indicates that this builder can be used with all kinds of project types
        return true;
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
    public ListBoxModel doFillEntitlementPreferenceItems() {
        ListBoxModel items = new ListBoxModel();
        for (FodEnums.EntitlementPreferenceType preferenceType : FodEnums.EntitlementPreferenceType.values()) {
            items.add(new ListBoxModel.Option(preferenceType.toString(), String.valueOf(preferenceType.getValue())));
        }

        return items;
    }

    // Form validation
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
    public FormValidation doTestConnection(@QueryParameter(CLIENT_ID) final String clientId,
                                           @QueryParameter(CLIENT_SECRET) final String clientSecret,
                                           @QueryParameter(BASE_URL) final String baseUrl) {
        if (Utils.isNullOrEmpty(clientId))
            return FormValidation.error("API Key is empty!");
        if (Utils.isNullOrEmpty(clientSecret))
            return FormValidation.error("Secret Key is empty!");
        if (Utils.isNullOrEmpty(baseUrl))
            return FormValidation.error("Fortify on Demand URL is empty!");

        FodApi testApi = new FodApi(clientId, clientSecret, baseUrl);

        testApi.authenticate();
        String token = testApi.getToken();

        if (token == null) {
            return FormValidation.error("Unable to retrieve authentication token.");
        }

        return !token.isEmpty() ?
                FormValidation.ok("Successfully authenticated to Fortify on Demand.") :
                FormValidation.error("Invalid connection information. Please check your credentials and try again.");
    }

    FodApi createFodApi() {
        if (!Utils.isNullOrEmpty(clientId) &&
                !Utils.isNullOrEmpty(clientSecret)&&
                !Utils.isNullOrEmpty(baseUrl)) {
            api = new FodApi(clientId, clientSecret, baseUrl);
            api.authenticate();
            return api;
        }
        return null;
    }
}