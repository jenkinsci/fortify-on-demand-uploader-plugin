package org.jenkinsci.plugins.fodupload;
import hudson.Extension;
import hudson.Launcher;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.models.JobConfigModel;
import org.jenkinsci.plugins.fodupload.models.response.ApplicationDTO;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseAssessmentTypeDTO;
import org.jenkinsci.plugins.fodupload.models.response.ReleaseDTO;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class FodUploaderPlugin extends Recorder implements SimpleBuildStep {
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
    private static final String TS_VB_SCRIPT_KEY = "VBScript";
    private static final String TS_XML_HTML_KEY = "XML/HTML";

    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();

    private FodApi api;
    private JobConfigModel jobModel;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FodUploaderPlugin(String applicationId, String releaseId, String assessmentTypeId, String technologyStack,
                             String languageLevel, boolean runOpenSourceAnalysis, boolean isExpressScan, boolean isExpressAudit,
                             boolean doPollFortify, boolean doPrettyLogOutput, boolean includeAllFiles, boolean includeThirdParty,
                             boolean isRemediationScan) {
        jobModel = new JobConfigModel(applicationId, releaseId, assessmentTypeId, technologyStack,
                languageLevel, runOpenSourceAnalysis, isExpressScan, isExpressAudit,
                doPollFortify, doPrettyLogOutput, includeAllFiles, includeThirdParty, isRemediationScan);

        api = getDescriptor().getFodApi();

        api.authenticate();

    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException {
        final PrintStream logger = listener.getLogger();
        taskListener.set(listener);

        logger.println("Starting Scan.");
        if (api.isAuthenticated()) {
            logger.println("Authenticated.");
        } else {
            api.authenticate();
        }

        if (getAssessmentTypeId().isEmpty()) {
            logger.println("Assessment Type is empty.");
            build.setResult(Result.FAILURE);
        }

        // zips the file in a temporary location
        File payload = CreateZipFile(workspace);
        if (payload.length() == 0) {
            logger.println("Source is empty for given Technology Stack and Language Level.");
            build.setResult(Result.FAILURE);
        }

        jobModel.setUploadFile(payload);
        boolean success = api.getStaticScanController().StartStaticScan(jobModel);
        payload.delete();
        if (success) {
            logger.println("Scan Uploaded Successfully.");
            //TODO: Polling
        }
    }

    private File CreateZipFile(FilePath workspace) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tempDir);

        File tempZip = File.createTempFile("fodupload", ".zip", dir);
        try(FileOutputStream fos = new FileOutputStream(tempZip)) {
            final Pattern pattern = Pattern.compile(getFileExpressionPatternString(getTechnologyStack()),
                    Pattern.CASE_INSENSITIVE);

            workspace.zip(fos, new RegexFileFilter(pattern));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempZip;
    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    public String getApplicationId() { return String.valueOf(jobModel.getApplicationId()); }
    public String getReleaseId() { return String.valueOf(jobModel.getReleaseId()); }
    public String getAssessmentTypeId() { return String.valueOf(jobModel.getAssessmentTypeId()); }
    public String getTechnologyStack() { return jobModel.getTechnologyStack(); }
    public String getLanguageLevel() { return jobModel.getLanguageLevel(); }
    public boolean getRunOpenSourceAnalysis() { return jobModel.getRunOpenSourceAnalysis(); }
    public boolean getIsExpressScan() { return jobModel.getIsExpressScan(); }
    public boolean getIsExpressAudit() { return jobModel.getIsExpressAudit(); }
    public boolean getDoPollFortify() { return jobModel.getDoPollFortify(); }
    public boolean getDoPrettyLogOutput() { return jobModel.getDoPrettyLogOutput(); }
    public boolean getIncludeAllFiles() { return jobModel.getIncludeAllFiles(); }
    public boolean getIncludeThirdParty() { return jobModel.getIncludeThirdParty(); }
    public boolean getIsRemediationScan() { return jobModel.getIsRemediationScan(); }

    private static String getFileExpressionPatternString(String technologyStack){
        String constantFiles = "|.*\\.html|.*\\.htm|.*\\.js|.*\\.xml|.*\\.xsd|.*\\.xmi|.*\\.wsdd|.*\\.config" +
                "|.*\\.settings|.*\\.cpx|.*\\.xcfg|.*\\.cscfg|.*\\.cscdef|.*\\.wadcfg|.*\\.appxmanifest"
                + "|.*\\.wsdl|.*\\.plist|.*\\.properties|.*\\.ini|.*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb";

        switch (technologyStack) {
            case TS_DOT_NET_KEY:
                return ".*\\.dll|.*\\.pdb|.*\\.cs|.*\\.aspx|.*\\.asp|.*\\.vb|.*\\.vbproj|.*\\.csproj|.*\\.sln" + constantFiles;
            case TS_JAVA_KEY:
                return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.tld" +
                        "|.*\\.jspx|.*\\.xhtml|.*\\.faces|.*\\.jsff|.*\\.properties" + constantFiles;
            case TS_PYTHON_KEY:
                return ".*\\.py" + constantFiles;
            case TS_RUBY_KEY:
                return ".*\\.rb|.*\\.erb" + constantFiles;
            case TS_ASP_KEY:
                return ".*\\.asp" + constantFiles;
            case TS_PHP_KEY:
                return ".*\\.php" + constantFiles;
            case TS_VB6_KEY:
                return ".*\\.vbs|.*\\.bas|.*\\.frm|.*\\.ctl|.*\\.cls" + constantFiles;
            case TS_VB_SCRIPT_KEY:
                return ".*\\.vbscript" + constantFiles;
            case TS_ANDROID_KEY:
                // APK is not normally used for Static analysis but we are collecting in the event it is useful
                return ".*\\.java|.*\\.class|.*\\.ear|.*\\.war|.*\\.jar|.*\\.jsp|.*\\.tag|.*\\.tagx|.*\\.tld" +
                        "|.*\\.jspx|.*\\.xhtml|.*\\.faces|.*\\.jsff|.*\\.properties|.*\\.apk" + constantFiles;
            case TS_XML_HTML_KEY:
                return ".*\\.xml|.*\\.xsd|.*\\.xmi|.*\\.wsdd|.*\\.config|.*\\.cpx|.*\\.xcfg" + constantFiles;
            case TS_PLSQL_TSQL_KEY:
                return ".*\\.sql|.*\\.pks|.*\\.pkh|.*\\.pkb" + constantFiles;
            case TS_ABAP_KEY:
                return ".*\\.abap" + constantFiles;
            case TS_CFML_KEY:
                return ".*\\.cfm|.*\\.cfml|.*\\.cfc" + constantFiles;
            default:
                return ".*";
        }
    }

    public static PrintStream getLogger() {
        return taskListener.get().getLogger();
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() { return (DescriptorImpl)super.getDescriptor(); }

    @Override
    public BuildStepMonitor getRequiredMonitorService() { return BuildStepMonitor.NONE; }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        static final String CLIENT_ID = "clientId";
        static final String CLIENT_SECRET = "clientSecret";
        static final String BASE_URL = "baseUrl";
        static final String APPLICATION_ID = "applicationId";
        static final String RELEASE_ID = "releaseId";
        static final String TECHNOLOGY_STACK = "technologyStack";
        static final String POLLING_INTERVAL = "pollingInterval";

        private FodApi api;
        private int pollingInterval;

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

            pollingInterval = formData.getInt(POLLING_INTERVAL);
            save();
            return super.configure(req,formData);
        }

        FodApi getFodApi() { return api; }

        // NOTE: The following Getters are used to return saved values in the jelly files. Intellij
        // marks them unused, but they actually are used.
        // These getters are also named in the following format: Get<JellyField>.
        public String getDisplayName() { return "Fortify Uploader Plug-in"; }
        @SuppressWarnings("unused")
        public String getClientId() { return api.getKey(); }
        @SuppressWarnings("unused")
        public String getClientSecret() { return api.getSecret(); }
        @SuppressWarnings("unused")
        public String getBaseUrl() { return api.getBaseUrl(); }
        @SuppressWarnings("unused")
        public int getPollingInterval() { return pollingInterval; }
        // NOTE: The following Getters are used to return saved values in the global.jelly. Intellij
        // marks them unused, but they actually are used.
        // These getters are also named in the following format: doFill<JellyField>Items.
        @SuppressWarnings("unused")
        public ListBoxModel doFillApplicationIdItems() {
            api.authenticate();

            ListBoxModel listBox = new ListBoxModel();
            List<ApplicationDTO> apps = api.getApplicationController().getApplications();
            for (ApplicationDTO app : apps) {
                final String value = String.valueOf(app.getApplicationId());
                listBox.add(new ListBoxModel.Option(app.getApplicationName(), value, false));
            }
            return listBox;
        }
        @SuppressWarnings("unused")
        public ListBoxModel doFillReleaseIdItems(@QueryParameter(APPLICATION_ID) final String applicationId) {


            ListBoxModel listBox = new ListBoxModel();
            List<ReleaseDTO> releases = api.getReleaseController().getReleases(applicationId);
            for(ReleaseDTO release : releases) {
                final String value = String.valueOf(release.getReleaseId());
                listBox.add(new ListBoxModel.Option(release.getReleaseName(), value, false));
            }
            return listBox;
        }
        @SuppressWarnings("unused")
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
            items.add(new ListBoxModel.Option(TS_VB_SCRIPT_KEY, TS_VB_SCRIPT_KEY, false));
            items.add(new ListBoxModel.Option(TS_XML_HTML_KEY, TS_XML_HTML_KEY, false));

            return items;
        }
        @SuppressWarnings("unused")
        public ListBoxModel doFillLanguageLevelItems(@QueryParameter(TECHNOLOGY_STACK) String technologyStack) {
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
        @SuppressWarnings("unused")
        public ListBoxModel doFillAssessmentTypeIdItems(@QueryParameter(RELEASE_ID) final String releaseId) {
            ListBoxModel listBox = new ListBoxModel();
            List<ReleaseAssessmentTypeDTO> assessmentTypes = api.getReleaseController().getAssessmentTypeIds(releaseId);
            for (ReleaseAssessmentTypeDTO assessmentType : assessmentTypes) {
                final String value = String.valueOf(assessmentType.getAssessmentTypeId());
                listBox.add(new ListBoxModel.Option(assessmentType.getName(), value, false));
            }
            return listBox;
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        public FormValidation doTestConnection() {
            api.authenticate();
            return !api.getToken().isEmpty() ?
                    FormValidation.ok("Successfully authenticated to Fortify on Demand.") :
                    FormValidation.error("Invalid connection information. Please check your credentials and try again.");
        }
    }
}

