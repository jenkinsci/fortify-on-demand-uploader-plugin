package org.jenkinsci.plugins.fodupload;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.models.JobModel;
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
import java.util.LinkedList;
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

    private static FodApi api;
    private static JobModel model;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    // Entry point when building
    @DataBoundConstructor
    public FodUploaderPlugin(String bsiUrl, boolean runOpenSourceAnalysis, boolean isExpressScan, boolean isExpressAudit,
                             int pollingInterval, boolean doPrettyLogOutput, boolean includeAllFiles, boolean excludeThirdParty,
                             boolean isRemediationScan) {

        model = new JobModel(bsiUrl,
                runOpenSourceAnalysis,
                isExpressAudit,
                isExpressScan,
                pollingInterval,
                includeAllFiles,
                excludeThirdParty,
                isRemediationScan,
                doPrettyLogOutput);
    }

    // logic run during a build
    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {
            final PrintStream logger = listener.getLogger();
            taskListener.set(listener);
            api = getDescriptor().createFodApi();

            if (api == null) {
                logger.println("Error: Failed to Authenticate with Fortify API.");
                build.setResult(Result.UNSTABLE);
            } else {
                api.authenticate();

                logger.println("Starting FoD Upload.");

                if (model.getBsiUrl().getAssessmentTypeId() == 0) {
                    logger.println("Assessment Type is empty.");
                    build.setResult(Result.FAILURE);
                }

                // zips the file in a temporary location
                File payload = CreateZipFile(workspace);
                if (payload.length() == 0) {
                    logger.println("Source is empty for given Technology Stack and Language Level.");
                    build.setResult(Result.FAILURE);
                }
                logger.println(model.getBsiUrl().ORIGINAL_VALUE);

                model.setPayload(payload);
                boolean success = api.getStaticScanController().startStaticScan(model);
                boolean deleted = payload.delete();
                if (success && deleted) {
                    logger.println("Scan Uploaded Successfully.");
                    if (getDescriptor().getDoPollFortify() && model.getPollingInterval() > 0) {
                        PollStatus /*Amy*/poller = new PollStatus(api, model);
                        success = poller.releaseStatus(model.getBsiUrl().getProjectVersionId());
                    }
                }

                // Success could be true then set to false from polling.
                api.retireToken();
                build.setResult(success ? Result.SUCCESS : Result.UNSTABLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            build.setResult(Result.UNSTABLE);
        }
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
            final Pattern pattern = Pattern.compile(getFileExpressionPatternString(model.getBsiUrl().getTechnologyStack()),
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
    public String getBsiUrl() {
        return model.getBsiUrl().ORIGINAL_VALUE;
    }
    public boolean getRunOpenSourceAnalysis() {
        return model.isRunOpenSourceAnalysis();
    }

    public boolean getIsExpressScan() {
        return model.isExpressScan();
    }

    public boolean getIsExpressAudit() {
        return model.isExpressAudit();
    }

    public boolean getDoPrettyLogOutput() {
        return model.isDoPrettyLogOutput();
    }

    public boolean getIncludeAllFiles() {
        return model.isIncludeAllFiles();
    }

    public boolean getExcludeThirdParty() {
        return model.isExcludeThirdParty();
    }

    public boolean getIsRemediationScan() {
        return model.isRemediationScan();
    }

    public int getPollingInterval() {
        return model.getPollingInterval();
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
        static final String DO_POLL_FORTIFY = "doPollFortify";

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

            save();

            api = createFodApi();
            if (api != null) {
                applications = api.getApplicationController().getApplications();
            }

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

        private List<ReleaseAssessmentTypeDTO> FilterNegativeEntitlements(List<ReleaseAssessmentTypeDTO> assessments) {
            List<ReleaseAssessmentTypeDTO> filtered = new LinkedList<>();
            if (!Utils.isNullOrEmpty(assessments)) {
                for (ReleaseAssessmentTypeDTO assessment : assessments) {
                    if (assessment.getEntitlementId() > 0)
                        filtered.add(assessment);
                }
            }
            return filtered;
        }

        private FodApi createFodApi() {
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


}


