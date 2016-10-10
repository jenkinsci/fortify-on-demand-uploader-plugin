package org.jenkinsci.plugins.fodupload;
import hudson.Extension;
import hudson.Launcher;
import hudson.FilePath;
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

import static org.jenkinsci.plugins.fodupload.FodApi.BASE_URL;
import static org.jenkinsci.plugins.fodupload.FodApi.CLIENT_ID;
import static org.jenkinsci.plugins.fodupload.FodApi.CLIENT_SECRET;

public class HelloWorldBuilder extends Recorder implements SimpleBuildStep {
    private FodApi api;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder() {

    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        api = getDescriptor().getFodApi();
        api.authenticate();

        if (api.isAuthenticated())
            listener.getLogger().println("Authenticated");

        List<ApplicationDTO> apps = api.getApplications(listener);
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

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() { return "Fortify Uploader Plug-in"; }
        public String getClientId() { return api.getKey(); }
        public String getClientSecret() { return api.getSecret(); }
        public String getBaseUrl() { return api.getBaseUrl(); }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            api = new FodApi(formData.getString(CLIENT_ID), formData.getString(CLIENT_SECRET), formData.getString(BASE_URL));;

            save();
            return super.configure(req,formData);
        }

        public ListBoxModel doFillApplicationNameItems() {
            return new ListBoxModel(
                    new ListBoxModel.Option("Green", "00ff00", true),
                    new ListBoxModel.Option("Yellow", "ffff00", false),
                    new ListBoxModel.Option("Red", "ff0000", false));
        }

        public FodApi getFodApi() {
            return api;
        }
    }
}

