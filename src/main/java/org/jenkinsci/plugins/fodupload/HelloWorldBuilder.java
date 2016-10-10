package org.jenkinsci.plugins.fodupload;
import hudson.Extension;
import hudson.Launcher;
import hudson.FilePath;
import hudson.RelativePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.Models.ApplicationDTO;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jenkinsci.plugins.fodupload.FodApi.BASE_URL;
import static org.jenkinsci.plugins.fodupload.FodApi.CLIENT_ID;
import static org.jenkinsci.plugins.fodupload.FodApi.CLIENT_SECRET;

public class HelloWorldBuilder extends Recorder implements SimpleBuildStep {
    private FodApi api;
    private String applicationName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(String applicationName) {
        api = getDescriptor().getFodApi();
        api.authenticate();
        this.applicationName = applicationName;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if (api.isAuthenticated())
            listener.getLogger().println("Authenticated");
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private FodApi api;
        private String applicationId;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            api = new FodApi(formData.getString(CLIENT_ID), formData.getString(CLIENT_SECRET), formData.getString(BASE_URL));
            api.authenticate();
            applicationId = formData.getString("applicationName");

            save();
            return super.configure(req,formData);
        }

        public FodApi getFodApi() { return api; }

        // NOTE: The following Getters are used to return saved values in the jelly files. Intellij
        // marks them unused, but they actually are.
        // These getters are also named in the following format: Get<JellyField>.
        public String getDisplayName() { return "Fortify Uploader Plug-in"; }
        public String getClientId() { return api.getKey(); }
        public String getClientSecret() { return api.getSecret(); }
        public String getBaseUrl() { return api.getBaseUrl(); }
        public String getApplicationId() { return String.valueOf(applicationId); }

        // NOTE: The following Getters are used to return saved values in the jelly files. Intellij
        // marks them unused, but they actually are.
        // These getters are also named in the following format: doFill<JellyField>Items.
        public ListBoxModel doFillApplicationNameItems() {
            if (!api.isAuthenticated()) {
                api.authenticate();
            }

            ListBoxModel listBox = new ListBoxModel();
            List<ApplicationDTO> apps = api.getApplications();
            for (ApplicationDTO app : apps) {
                final String value = String.valueOf(app.getApplicationId());
                listBox.add(new ListBoxModel.Option(app.getApplicationName(), value, false));
            }
            return listBox;
        }
    }
}

