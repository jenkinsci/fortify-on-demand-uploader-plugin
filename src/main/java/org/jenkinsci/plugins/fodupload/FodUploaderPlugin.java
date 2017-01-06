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
import org.jenkinsci.plugins.fodupload.models.FodEnums.EntitlementFrequencyType;
import org.jenkinsci.plugins.fodupload.models.JobConfigModel;
import org.jenkinsci.plugins.fodupload.models.response.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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

    private static FodApi api;
    private JobConfigModel jobModel;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Entry point when building
    @DataBoundConstructor
    public FodUploaderPlugin(String applicationId, String releaseId, String assessmentTypeId, String technologyStack,
                             String languageLevel, boolean runOpenSourceAnalysis, boolean isExpressScan, boolean isExpressAudit,
                             int pollingInterval, boolean doPrettyLogOutput, boolean includeAllFiles, boolean excludeThirdParty,
                             boolean isRemediationScan, int entitlementId) {
        int frequencyType = 0;
        for (ReleaseAssessmentTypeDTO assessment : getDescriptor().assessments) {
            if (assessment.getEntitlementId() == entitlementId) {
                frequencyType = assessment.getFrequencyTypeId();
            }
        }
        // load job model
        jobModel = new JobConfigModel(applicationId, releaseId, assessmentTypeId, technologyStack,
                languageLevel, runOpenSourceAnalysis, isExpressScan, isExpressAudit,
                pollingInterval, doPrettyLogOutput, includeAllFiles, excludeThirdParty, isRemediationScan,
                entitlementId, frequencyType);
    }

    // logic run during a build
    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException {
        api.authenticate();

        final PrintStream logger = listener.getLogger();
        taskListener.set(listener);

        logger.println("Starting FoD Upload.");

        if (getAssessmentTypeId() == 0) {
            logger.println("Assessment Type is empty.");
            build.setResult(Result.FAILURE);
        }

        // zips the file in a temporary location
        File payload = CreateZipFile(workspace);
        if (payload.length() == 0) {
            logger.println("Source is empty for given Technology Stack and Language Level.");
            build.setResult(Result.FAILURE);
        }
        logger.println(jobModel.toString());

        jobModel.setUploadFile(payload);
        boolean success = api.getStaticScanController().startStaticScan(jobModel);
        boolean deleted = payload.delete();
        if (success && deleted) {
            logger.println("Scan Uploaded Successfully.");
            if (getDescriptor().getDoPollFortify() && jobModel.getPollingInterval() > 0) {
                PollStatus /*Amy*/poller = new PollStatus(api, jobModel);
                success = poller.releaseStatus(getReleaseId());
            }
        }

        // Success could be true then set to false from polling.
        api.retireToken();
        build.setResult(success ? Result.SUCCESS : Result.UNSTABLE);
    }

    /**
     * Zips a folder, stores it in a temp location and returns the object
     *
     * @param workspace location of the files to zip
     * @return a File object
     * @throws IOException no files
     */
    private File CreateZipFile(FilePath workspace) throws IOException {
        getLogger().println("Begin Create Zip.");

        String tempDir = System.getProperty("java.io.tmpdir");
        File dir = new File(tempDir);


        File tempZip = File.createTempFile("fodupload", ".zip", dir);
        try (FileOutputStream fos = new FileOutputStream(tempZip)) {
            final Pattern pattern = Pattern.compile(getFileExpressionPatternString(getTechnologyStack()),
                    Pattern.CASE_INSENSITIVE);

            workspace.zip(fos, new RegexFileFilter(pattern));
            getLogger().println("Temporary file created at: " + tempZip.getAbsolutePath());

        } catch (Exception e) {
            getLogger().println(e.getMessage());
        }
        getLogger().println("End Create Zip.");
        return tempZip;
    }

    // NOTE: The following Getters are used to return saved values in the config.jelly. Intellij
    // marks them unused, but they actually are used.
    // These getters are also named in the following format: Get<JellyField>.
    public int getApplicationId() {
        return jobModel.getApplicationId();
    }

    public int getReleaseId() {
        return jobModel.getReleaseId();
    }

    public int getAssessmentTypeId() {
        return jobModel.getAssessmentTypeId();
    }

    public String getTechnologyStack() {
        return jobModel.getTechnologyStack();
    }

    public String getLanguageLevel() {
        return jobModel.getLanguageLevel();
    }

    public boolean getRunOpenSourceAnalysis() {
        return jobModel.getRunOpenSourceAnalysis();
    }

    public boolean getIsExpressScan() {
        return jobModel.getIsExpressScan();
    }

    public boolean getIsExpressAudit() {
        return jobModel.getIsExpressAudit();
    }

    public boolean getDoPrettyLogOutput() {
        return jobModel.getDoPrettyLogOutput();
    }

    public boolean getIncludeAllFiles() {
        return jobModel.getIncludeAllFiles();
    }

    public boolean getExcludeThirdParty() {
        return jobModel.getExcludeThirdParty();
    }

    public boolean getIsRemediationScan() {
        return jobModel.getIsRemediationScan();
    }

    public int getEntitlementId() {
        return jobModel.getEntitlementId();
    }

    public int getPollingInterval() {
        return jobModel.getPollingInterval();
    }

    private static String getFileExpressionPatternString(String technologyStack) {
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

    /**
     * Gets the out for the build console output
     *
     * @return Task Listener object
     */
    public static PrintStream getLogger() {
        return taskListener.get().getLogger();
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        static final String CLIENT_ID = "clientId";
        static final String CLIENT_SECRET = "clientSecret";
        static final String BASE_URL = "baseUrl";
        static final String APPLICATION_ID = "applicationId";
        static final String RELEASE_ID = "releaseId";
        static final String TECHNOLOGY_STACK = "technologyStack";
        static final String DO_POLL_FORTIFY = "doPollFortify";
        static final String ASSESSMENT_TYPE_ID = "assessmentTypeId";

        private String clientId;
        private String clientSecret;
        private String baseUrl;
        private boolean doPollFortify;
        private String defaultTechStack;
        private List<ApplicationDTO> applications;
        private List<ReleaseDTO> releases;
        private List<ReleaseAssessmentTypeDTO> assessments;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        // Entry point when accessing global configuration
        public DescriptorImpl() {
            load();
            loadPluginOptions();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        // On save.
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            clientId = formData.getString(CLIENT_ID);
            clientSecret = formData.getString(CLIENT_SECRET);
            baseUrl = formData.getString(BASE_URL);
            doPollFortify = formData.getBoolean(DO_POLL_FORTIFY);

            loadPluginOptions();
            save();
            return super.configure(req, formData);
        }

        // NOTE: The following Getters are used to return saved values in the jelly files. Intellij
        // marks them unused, but they actually are used.
        // These getters are also named in the following format: Get<JellyField>.
        public String getDisplayName() {
            return "Fortify Uploader Plugin";
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        @SuppressWarnings("unused")
        public boolean getDoPollFortify() {
            return doPollFortify;
        }

        // NOTE: The following Getters are used to return saved values in the global.jelly. Intellij
        // marks them unused, but they actually are used.
        // These getters are also named in the following format: doFill<JellyField>Items.
        @SuppressWarnings("unused")
        public ListBoxModel doFillApplicationIdItems() {
            ListBoxModel listBox = new ListBoxModel();
            for (ApplicationDTO app : applications) {
                final String value = String.valueOf(app.getApplicationId());
                listBox.add(new ListBoxModel.Option(app.getApplicationName(), value, false));
            }
            return listBox;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillReleaseIdItems(@QueryParameter(APPLICATION_ID) int applicationId) {
            ListBoxModel listBox = new ListBoxModel();
            api.authenticate();
            releases = api.getReleaseController().getReleases(applicationId);
            for (ReleaseDTO release : releases) {
                final String value = String.valueOf(release.getReleaseId());
                listBox.add(new ListBoxModel.Option(release.getReleaseName(), value, false));
            }
            return listBox;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTechnologyStackItems() {
            ListBoxModel items = new ListBoxModel();

            items.add(new ListBoxModel.Option(TS_DOT_NET_KEY, TS_DOT_NET_KEY, false));
            items.add(new ListBoxModel.Option(TS_ABAP_KEY, TS_ABAP_KEY, false));
            items.add(new ListBoxModel.Option(TS_ASP_KEY, TS_ASP_KEY, false));
            items.add(new ListBoxModel.Option(TS_ANDROID_KEY, TS_ANDROID_KEY, false));
            items.add(new ListBoxModel.Option(TS_CFML_KEY, TS_ABAP_KEY, false));
            items.add(new ListBoxModel.Option(TS_COBOL_KEY, TS_COBOL_KEY, false));
            items.add(new ListBoxModel.Option(TS_JAVA_KEY, TS_JAVA_KEY, false));
            items.add(new ListBoxModel.Option(TS_OBJECTIVE_C_KEY, TS_OBJECTIVE_C_KEY, false));
            items.add(new ListBoxModel.Option(TS_PHP_KEY, TS_PHP_KEY, false));
            items.add(new ListBoxModel.Option(TS_PLSQL_TSQL_KEY, TS_PLSQL_TSQL_KEY, false));
            items.add(new ListBoxModel.Option(TS_PYTHON_KEY, TS_PYTHON_KEY, false));
            items.add(new ListBoxModel.Option(TS_RUBY_KEY, TS_RUBY_KEY, false));
            items.add(new ListBoxModel.Option(TS_VB6_KEY, TS_VB6_KEY, false));
            items.add(new ListBoxModel.Option(TS_VB_SCRIPT_KEY, TS_VB_SCRIPT_KEY, false));
            items.add(new ListBoxModel.Option(TS_XML_HTML_KEY, TS_XML_HTML_KEY, false));

            defaultTechStack = items.get(0).name;
            return items;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillLanguageLevelItems(@QueryParameter(TECHNOLOGY_STACK) String technologyStack) {
            ListBoxModel items = new ListBoxModel();

            if (technologyStack == null || technologyStack.isEmpty())
                technologyStack = defaultTechStack;
            switch (technologyStack) {
                case TS_JAVA_KEY:
                    items.add(new ListBoxModel.Option("1.2", "1.2", false));
                    items.add(new ListBoxModel.Option("1.3", "1.3", false));
                    items.add(new ListBoxModel.Option("1.4", "1.4", false));
                    items.add(new ListBoxModel.Option("1.5", "1.5", false));
                    items.add(new ListBoxModel.Option("1.6", "1.6", false));
                    items.add(new ListBoxModel.Option("1.7", "1.7", false));
                    items.add(new ListBoxModel.Option("1.8", "1.8", false));
                    break;
                case TS_DOT_NET_KEY:
                    items.add(new ListBoxModel.Option("1.0", "1.0", false));
                    items.add(new ListBoxModel.Option("1.1", "1.1", false));
                    items.add(new ListBoxModel.Option("2.0", "2.0", false));
                    items.add(new ListBoxModel.Option("3.0", "3.0", false));
                    items.add(new ListBoxModel.Option("3.5", "3.5", false));
                    items.add(new ListBoxModel.Option("4.0", "4.0", false));
                    items.add(new ListBoxModel.Option("4.5", "4.5", false));
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
        public ListBoxModel doFillAssessmentTypeIdItems(@QueryParameter(RELEASE_ID) int releaseId) {
            ListBoxModel listBox = new ListBoxModel();
            api.authenticate();
            assessments = FilterNegativeEntitlements(api.getReleaseController().getAssessmentTypeIds(releaseId));
            for (ReleaseAssessmentTypeDTO assessmentType : assessments) {
                final String value = String.valueOf(assessmentType.getAssessmentTypeId());
                String infoText;
                if (assessmentType.getFrequencyTypeId() == EntitlementFrequencyType.Subscription.getValue()) {
                    infoText = "Subscription";
                } else {
                    infoText = String.format("Single Scan: %s Unit(s) left", assessmentType.getUnitsAvailable());
                }
                final String name = String.format("%s (%s)", assessmentType.getName(), infoText);
                listBox.add(new ListBoxModel.Option(name, value, false));
            }
            return listBox;
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementIdItems(@QueryParameter(ASSESSMENT_TYPE_ID) final String assessmentTypeId) {
            // Get entitlements on load
            ListBoxModel listBox = new ListBoxModel();
            Set<ReleaseAssessmentTypeDTO> applicableAssessments = new HashSet<>();

            for (ReleaseAssessmentTypeDTO assessment : assessments) {
                int parsedAssessmentId = Utils.tryParseInt(assessmentTypeId);
                if (assessment.getAssessmentTypeId() == parsedAssessmentId && parsedAssessmentId > 0)
                    applicableAssessments.add(assessment);
            }

            for (ReleaseAssessmentTypeDTO entitlement : applicableAssessments) {
                String val = String.valueOf(entitlement.getEntitlementId());
                boolean addIt = true;
                for (ListBoxModel.Option option : listBox) {
                    addIt = !option.value.equals(val);
                }
                if (addIt)
                    listBox.add(new ListBoxModel.Option(val, val, false));
            }
            return listBox;
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        public FormValidation doTestConnection(@QueryParameter(CLIENT_ID) final String clientId,
                                               @QueryParameter(CLIENT_SECRET) final String clientSecret,
                                               @QueryParameter(BASE_URL) final String baseUrl) {
            if (clientId == null || clientId.isEmpty())
                return FormValidation.error("API Key is empty!");
            if (clientSecret == null || clientSecret.isEmpty())
                return FormValidation.error("Secret Key is empty!");
            if (baseUrl == null || baseUrl.isEmpty())
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

        private void loadPluginOptions() {
            if (clientId != null && clientSecret != null && baseUrl != null) {
                api = new FodApi(clientId, clientSecret, baseUrl);
                api.authenticate();
                applications = api.getApplicationController().getApplications();
                if (!applications.isEmpty()) {
                    releases = api.getReleaseController().getReleases(applications.get(0).getApplicationId());
                    assessments = FilterNegativeEntitlements(
                        api.getReleaseController().getAssessmentTypeIds(releases.get(0).getReleaseId()));
                }
            }
        }

        private List<ReleaseAssessmentTypeDTO> FilterNegativeEntitlements(List<ReleaseAssessmentTypeDTO> assessments) {
            List<ReleaseAssessmentTypeDTO> filtered = new LinkedList<>();
            for (ReleaseAssessmentTypeDTO assessment : assessments) {
                if (assessment.getEntitlementId() > 0)
                    filtered.add(assessment);
            }
            return filtered;
        }
    }
}

