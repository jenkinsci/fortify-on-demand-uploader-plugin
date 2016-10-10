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
import org.jenkinsci.plugins.fodupload.Models.ReleaseAssessmentTypeDTO;
import org.jenkinsci.plugins.fodupload.Models.ReleaseDTO;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

import static org.jenkinsci.plugins.fodupload.FodApi.BASE_URL;
import static org.jenkinsci.plugins.fodupload.FodApi.CLIENT_ID;
import static org.jenkinsci.plugins.fodupload.FodApi.CLIENT_SECRET;

public class FodUploaderPlugin extends Recorder implements SimpleBuildStep {
    private FodApi api;
    private String applicationId;
    private String releaseId;
    private String assessmentTypeId;
    private String technologyStack;
    private String languageLevel;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FodUploaderPlugin(String applicationId, String releaseId, String assessmentTypeId, String technologyStack,
                             String languageLevel) {
        api = getDescriptor().getFodApi();
        api.authenticate();

        this.applicationId = applicationId;
        this.releaseId = releaseId;
        this.assessmentTypeId = assessmentTypeId;
        this.technologyStack = technologyStack;
        this.languageLevel = languageLevel;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        if (api.isAuthenticated())
            listener.getLogger().println("Authenticated");
    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    public String getApplicationId() { return applicationId; }
    public String getReleaseId() { return releaseId; }
    public String getAssessmentTypeId() { return assessmentTypeId; }
    public String getTechnologyStack() { return technologyStack; }
    public String getLanguageLevel() { return languageLevel; }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() { return (DescriptorImpl)super.getDescriptor(); }

    @Override
    public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        //TODO: Create Lookup endpoint for this info.
        private static final String TS_DOT_NET_KEY = ".NET";
        private static final String TS_JAVA_KEY = "JAVA/J2EE";
        private static final String TS_RUBY_KEY = "Ruby";
        private static final String TS_PYTHON_KEY = "Python";
        private static final String TS_OBJECTIVE_C_KEY = "Objective-C";
        private static final String TS_ABAP_KEY = "ABAP";
        private static final String TS_ASP_KEY = "ASP";
        private static final String TS_CFML_KEY = "CFML";
        private static final String TS_COBOL_KEY = "COBOL";
        private static final String TS_ANDROID_KEY = "Android";
        private static final String TS_PHP_KEY = "PHP";
        private static final String TS_PLSQL_TSQL_KEY = "PL/SQL & T-SQL";
        private static final String TS_VB6_KEY = "VB6";
        private static final String TS_VBSCRIPT_KEY = "VBScript";
        private static final String TS_XML_HTML_KEY = "XML/HTML";

        private FodApi api;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
            api.authenticate();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            api = new FodApi(formData.getString(CLIENT_ID), formData.getString(CLIENT_SECRET), formData.getString(BASE_URL));
            api.authenticate();

            save();
            return super.configure(req,formData);
        }

        public FodApi getFodApi() { return api; }

        // NOTE: The following Getters are used to return saved values in the jelly files. Intellij
        // marks them unused, but they actually are used.
        // These getters are also named in the following format: Get<JellyField>.
        public String getDisplayName() { return "Fortify Uploader Plug-in"; }
        public String getClientId() { return api.getKey(); }
        public String getClientSecret() { return api.getSecret(); }
        public String getBaseUrl() { return api.getBaseUrl(); }

        // NOTE: The following Getters are used to return saved values in the global.jelly. Intellij
        // marks them unused, but they actually are used.
        // These getters are also named in the following format: doFill<JellyField>Items.
        public ListBoxModel doFillApplicationIdItems() {
            ListBoxModel listBox = new ListBoxModel();
            List<ApplicationDTO> apps = api.getApplications();
            for (ApplicationDTO app : apps) {
                final String value = String.valueOf(app.getApplicationId());
                listBox.add(new ListBoxModel.Option(app.getApplicationName(), value, false));
            }
            return listBox;
        }
        public ListBoxModel doFillReleaseIdItems(@QueryParameter("applicationId") final String applicationId) {
            ListBoxModel listBox = new ListBoxModel();
            List<ReleaseDTO> releases = api.getReleases(applicationId);
            for(ReleaseDTO release : releases) {
                final String value = String.valueOf(release.getReleaseId());
                listBox.add(new ListBoxModel.Option(release.getReleaseName(), value, false));
            }
            return listBox;
        }
        public ListBoxModel doFillTechnologyStackItems() {
            ListBoxModel items = new ListBoxModel();

            items.add(new ListBoxModel.Option(TS_DOT_NET_KEY, TS_DOT_NET_KEY,false));
            items.add(new ListBoxModel.Option(TS_ABAP_KEY, TS_ABAP_KEY, false));
            items.add(new ListBoxModel.Option(TS_ASP_KEY, TS_ASP_KEY, false));
            items.add(new ListBoxModel.Option(TS_ANDROID_KEY, TS_ANDROID_KEY, false));
            items.add(new ListBoxModel.Option(TS_CFML_KEY, TS_ABAP_KEY, false));
            items.add(new ListBoxModel.Option(TS_COBOL_KEY, TS_COBOL_KEY, false));
            items.add(new ListBoxModel.Option(TS_JAVA_KEY, TS_JAVA_KEY,false));
            items.add(new ListBoxModel.Option(TS_OBJECTIVE_C_KEY, TS_OBJECTIVE_C_KEY, false));
            items.add(new ListBoxModel.Option(TS_PHP_KEY, TS_PHP_KEY, false));
            items.add(new ListBoxModel.Option(TS_PLSQL_TSQL_KEY, TS_PLSQL_TSQL_KEY, false));
            items.add(new ListBoxModel.Option(TS_PYTHON_KEY, TS_PYTHON_KEY,false));
            items.add(new ListBoxModel.Option(TS_RUBY_KEY, TS_RUBY_KEY,false));
            items.add(new ListBoxModel.Option(TS_VB6_KEY, TS_VB6_KEY, false));
            items.add(new ListBoxModel.Option(TS_VBSCRIPT_KEY, TS_VBSCRIPT_KEY, false));
            items.add(new ListBoxModel.Option(TS_XML_HTML_KEY, TS_XML_HTML_KEY, false));

            return items;
        }
        public ListBoxModel doFillLanguageLevelItems(@QueryParameter("technologyStack") String technologyStack) {
            ListBoxModel items = new ListBoxModel();

            switch (technologyStack) {
                case TS_JAVA_KEY:
                    items.add(new ListBoxModel.Option("1.2", "1.2",false));
                    items.add(new ListBoxModel.Option("1.3", "1.3",false));
                    items.add(new ListBoxModel.Option("1.4", "1.4",false));
                    items.add(new ListBoxModel.Option("1.5", "1.5",false));
                    items.add(new ListBoxModel.Option("1.6", "1.6",false));
                    items.add(new ListBoxModel.Option("1.7", "1.7",false));
                    items.add(new ListBoxModel.Option("1.8", "1.8",false));
                    break;
                case TS_DOT_NET_KEY:
                    items.add(new ListBoxModel.Option("1.0", "1.0",false));
                    items.add(new ListBoxModel.Option("1.1", "1.1",false));
                    items.add(new ListBoxModel.Option("2.0", "2.0",false));
                    items.add(new ListBoxModel.Option("3.0", "3.0",false));
                    items.add(new ListBoxModel.Option("3.5", "3.5",false));
                    items.add(new ListBoxModel.Option("4.0", "4.0",false));
                    items.add(new ListBoxModel.Option("4.5", "4.5",false));
                    items.add(new ListBoxModel.Option("4.5.1", "4.5.1",false));
                    items.add(new ListBoxModel.Option("4.5.2", "4.5.2",false));
                    items.add(new ListBoxModel.Option("4.6", "4.6",false));
                    items.add(new ListBoxModel.Option("4.6.1", "4.6.1",false));
                    break;
                case TS_PYTHON_KEY:
                    items.add(new ListBoxModel.Option("Standard Python", "Standard Python", false));
                    items.add(new ListBoxModel.Option("Django", "Django", false));
                    break;
                default:
                    //support for no language level, must be null for correct API call
                    items.add(new ListBoxModel.Option("N/A", null, false));
                    break;
            }

            return items;
        }
        public ListBoxModel doFillAssessmentTypeIdItems(@QueryParameter("releaseId") final String releaseId) {
            ListBoxModel listBox = new ListBoxModel();
            List<ReleaseAssessmentTypeDTO> assessmentTypes = api.getAssessmentTypeIds(releaseId);
            for (ReleaseAssessmentTypeDTO assessmentType : assessmentTypes) {
                final String value = String.valueOf(assessmentType.getAssessmentTypeId());
                listBox.add(new ListBoxModel.Option(assessmentType.getName(), value, false));
            }
            return listBox;
        }
    }
}

