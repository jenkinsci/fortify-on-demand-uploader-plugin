package org.jenkinsci.plugins.fodupload.steps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import com.google.common.collect.ImmutableSet;

import groovy.lang.Tuple2;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.*;
import org.jenkinsci.plugins.fodupload.actions.CrossBuildAction;
import org.jenkinsci.plugins.fodupload.controllers.*;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.AssessmentTypeEntitlementsForAutoProv;
import org.jenkinsci.plugins.fodupload.models.response.GetStaticScanSetupResponse;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;


@SuppressFBWarnings("unused")
public class FortifyStaticAssessment extends FortifyStep {

    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();

    private final String correlationId = UUID.randomUUID().toString();

    private String releaseId;
    private String bsiToken;

    @Deprecated
    private Boolean overrideGlobalConfig;
    private String username;
    private String personalAccessToken;
    private String tenantId;

    private boolean purchaseEntitlements;
    private String entitlementPreference;
    private String srcLocation;
    private String remediationScanPreferenceType;
    private String inProgressScanActionType;
    private String inProgressBuildResultType;

    private String assessmentType;
    private String entitlementId;
    private String frequencyId;
    private String auditPreference;
    private String technologyStack;
    private String languageLevel;
    private String openSourceScan;

    private String scanCentral;
    private String scanCentralIncludeTests;
    private String scanCentralSkipBuild;
    private String scanCentralBuildCommand;
    private String scanCentralBuildFile;
    private String scanCentralBuildToolVersion;
    private String scanCentralVirtualEnv;
    private String scanCentralRequirementFile;

    private String applicationName;
    private String applicationType;
    private String releaseName;
    private Integer owner;
    private String attributes;
    private String businessCriticality;
    private String sdlcStatus;
    private String microserviceName;
    private Boolean isMicroservice;

    private SharedUploadBuildStep commonBuildStep;

    @DataBoundConstructor
    public FortifyStaticAssessment() {
        super();
    }

    public String getBsiToken() {
        return bsiToken;
    }

    @DataBoundSetter
    public void setBsiToken(String bsiToken) {
        this.bsiToken = bsiToken.trim();
    }

    public String getReleaseId() {
        return releaseId;
    }

    @DataBoundSetter
    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId.trim();
    }

    @Deprecated
    public Boolean getOverrideGlobalConfig() {
        return overrideGlobalConfig;
    }

    @Deprecated
    @DataBoundSetter
    public void setOverrideGlobalConfig(Boolean overrideGlobalConfig) {
        this.overrideGlobalConfig = overrideGlobalConfig;
    }

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }

    @DataBoundSetter
    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getTenantId() {
        return tenantId;
    }

    @DataBoundSetter
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean getPurchaseEntitlements() {
        return purchaseEntitlements;
    }

    @DataBoundSetter
    public void setPurchaseEntitlements(boolean purchaseEntitlements) {
        this.purchaseEntitlements = purchaseEntitlements;
    }

    public String getEntitlementPreference() {
        return entitlementPreference;
    }

    @DataBoundSetter
    public void setEntitlementPreference(String entitlementPreference) {
        this.entitlementPreference = entitlementPreference;
    }

    public String getSrcLocation() {
        return srcLocation;
    }

    @DataBoundSetter
    public void setSrcLocation(String srcLocation) {
        this.srcLocation = srcLocation != null ? srcLocation.trim() : "";
    }

    public String getRemediationScanPreferenceType() {
        return remediationScanPreferenceType;
    }

    @DataBoundSetter
    public void setRemediationScanPreferenceType(String remediationScanPreferenceType) {
        this.remediationScanPreferenceType = remediationScanPreferenceType;
    }

    public String getInProgressScanActionType() {
        return inProgressScanActionType;
    }

    @DataBoundSetter
    public void setInProgressScanActionType(String inProgressScanActionType) {
        this.inProgressScanActionType = inProgressScanActionType;
    }

    public String getInProgressBuildResultType() {
        return inProgressBuildResultType;
    }

    @DataBoundSetter
    public void setInProgressBuildResultType(String inProgressBuildResultType) {
        this.inProgressBuildResultType = inProgressBuildResultType;
    }

    @SuppressWarnings("unused")
    public String getScanCentral() {
        return scanCentral;
    }

    @DataBoundSetter
    public void setScanCentral(String scanCentral) {
        this.scanCentral = scanCentral;
    }

    @SuppressWarnings("unused")
    public String getScanCentralIncludeTests() {
        return scanCentralIncludeTests;
    }

    @DataBoundSetter
    public void setScanCentralIncludeTests(String scanCentralIncludeTests) {
        this.scanCentralIncludeTests = scanCentralIncludeTests;
    }

    @SuppressWarnings("unused")
    public String getScanCentralSkipBuild() {
        return scanCentralSkipBuild;
    }

    @DataBoundSetter
    public void setScanCentralSkipBuild(String scanCentralSkipBuild) {
        this.scanCentralSkipBuild = scanCentralSkipBuild;
    }

    @SuppressWarnings("unused")
    public String getScanCentralBuildCommand() {
        return scanCentralBuildCommand;
    }

    @DataBoundSetter
    public void setScanCentralBuildCommand(String scanCentralBuildCommand) {
        this.scanCentralBuildCommand = scanCentralBuildCommand;
    }

    @SuppressWarnings("unused")
    public String getScanCentralBuildFile() {
        return scanCentralBuildFile;
    }

    @DataBoundSetter
    public void setScanCentralBuildFile(String scanCentralBuildFile) {
        this.scanCentralBuildFile = scanCentralBuildFile;
    }

    @SuppressWarnings("unused")
    public String getScanCentralBuildToolVersion() {
        return scanCentralBuildToolVersion;
    }

    @DataBoundSetter
    public void setScanCentralBuildToolVersion(String scanCentralBuildToolVersion) {
        this.scanCentralBuildToolVersion = scanCentralBuildToolVersion;
    }

    @SuppressWarnings("unused")
    public String getScanCentralVirtualEnv() {
        return scanCentralVirtualEnv;
    }

    @DataBoundSetter
    public void setScanCentralVirtualEnv(String scanCentralVirtualEnv) {
        this.scanCentralVirtualEnv = scanCentralVirtualEnv;
    }

    @SuppressWarnings("unused")
    public String getScanCentralRequirementFile() {
        return scanCentralRequirementFile;
    }

    @DataBoundSetter
    public void setScanCentralRequirementFile(String scanCentralRequirementFile) {
        this.scanCentralRequirementFile = scanCentralRequirementFile;
    }

    @SuppressWarnings("unused")
    public String getApplicationName() {
        return applicationName;
    }

    @DataBoundSetter
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @SuppressWarnings("unused")
    public String getApplicationType() {
        return applicationType;
    }

    @DataBoundSetter
    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    @SuppressWarnings("unused")
    public String getReleaseName() {
        return releaseName;
    }

    @DataBoundSetter
    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    @SuppressWarnings("unused")
    public Integer getOwner() {
        return owner;
    }

    @DataBoundSetter
    public void setOwner(Integer owner) {
        this.owner = owner;
    }

    @SuppressWarnings("unused")
    public String getAttributes() {
        return attributes;
    }

    @DataBoundSetter
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    @SuppressWarnings("unused")
    public String getBusinessCriticality() {
        return businessCriticality;
    }

    @DataBoundSetter
    public void setBusinessCriticality(String businessCriticality) {
        this.businessCriticality = businessCriticality;
    }

    @SuppressWarnings("unused")
    public String getSdlcStatus() {
        return sdlcStatus;
    }

    @DataBoundSetter
    public void setSdlcStatus(String sdlcStatus) {
        this.sdlcStatus = sdlcStatus;
    }

    @SuppressWarnings("unused")
    public String getMicroserviceName() {
        return microserviceName;
    }

    @DataBoundSetter
    public void setMicroserviceName(String microserviceName) {
        this.microserviceName = microserviceName;
    }

    @SuppressWarnings("unused")
    public Boolean getIsMicroservice() {
        return isMicroservice;
    }

    @DataBoundSetter
    public void setIsMicroservice(Boolean isMicroservice) {
        this.isMicroservice = isMicroservice;
    }


    @SuppressWarnings("unused")
    public String getAssessmentType() {
        return assessmentType;
    }

    @DataBoundSetter
    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    @SuppressWarnings("unused")
    public String getEntitlementId() {
        return entitlementId;
    }

    @DataBoundSetter
    public void setEntitlementId(String entitlementId) {
        this.entitlementId = entitlementId;
    }

    @SuppressWarnings("unused")
    public String getFrequencyId() {
        return frequencyId;
    }

    @DataBoundSetter
    public void setFrequencyId(String frequencyId) {
        this.frequencyId = frequencyId;
    }

    @SuppressWarnings("unused")
    public String getAuditPreference() {
        return auditPreference;
    }

    @DataBoundSetter
    public void setAuditPreference(String auditPreference) {
        this.auditPreference = auditPreference;
    }

    @SuppressWarnings("unused")
    public String getTechnologyStack() {
        return technologyStack;
    }

    @DataBoundSetter
    public void setTechnologyStack(String technologyStack) {
        this.technologyStack = technologyStack;
    }

    @SuppressWarnings("unused")
    public String getLanguageLevel() {
        return languageLevel;
    }

    @DataBoundSetter
    public void setLanguageLevel(String languageLevel) {
        this.languageLevel = languageLevel;
    }

    @SuppressWarnings("unused")
    public String getOpenSourceScan() {
        return openSourceScan;
    }

    @DataBoundSetter
    public void setOpenSourceScan(String openSourceScan) {
        this.openSourceScan = openSourceScan;
    }


    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();

        log.println("Fortify on Demand Upload PreBuild Running...");

        boolean overrideGlobalAuthConfig = !Utils.isNullOrEmpty(username);
        List<String> errors = ValidateModel(overrideGlobalAuthConfig, log);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments: Missing or invalid fields for auto provisioning: " + String.join(", ", errors));
        }

        commonBuildStep = new SharedUploadBuildStep(releaseId,
                bsiToken,
                overrideGlobalAuthConfig,
                username,
                personalAccessToken,
                tenantId,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType,
                scanCentral,
                scanCentralSkipBuild != null && scanCentralSkipBuild.equalsIgnoreCase("true"),
                scanCentralBuildCommand,
                scanCentralBuildFile,
                scanCentralBuildToolVersion,
                scanCentralVirtualEnv,
                scanCentralRequirementFile,
                assessmentType,
                entitlementId,
                frequencyId,
                auditPreference,
                technologyStack,
                languageLevel,
                openSourceScan,
                !Utils.isNullOrEmpty(applicationName),
                applicationName,
                applicationType,
                releaseName,
                owner,
                attributes,
                businessCriticality,
                sdlcStatus,
                microserviceName,
                isMicroservice);

        return true;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(this, context);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, IllegalArgumentException {
        PrintStream log = listener.getLogger();
        log.println("Fortify on Demand Upload Running...");
        build.addAction(new CrossBuildAction());
        try {
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex.toString());
        }
        boolean overrideGlobalAuthConfig = !Utils.isNullOrEmpty(username);
        List<String> errors = ValidateModel(overrideGlobalAuthConfig, log);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments:\n\t" + String.join("\n\t", errors));
        }

        commonBuildStep = new SharedUploadBuildStep(releaseId,
                bsiToken,
                overrideGlobalAuthConfig,
                username,
                personalAccessToken,
                tenantId,
                purchaseEntitlements,
                entitlementPreference,
                srcLocation,
                remediationScanPreferenceType,
                inProgressScanActionType,
                inProgressBuildResultType,
                scanCentral,
                scanCentralSkipBuild != null && scanCentralSkipBuild.equalsIgnoreCase("true"),
                scanCentralBuildCommand,
                scanCentralBuildFile,
                scanCentralBuildToolVersion,
                scanCentralVirtualEnv,
                scanCentralRequirementFile,
                assessmentType,
                entitlementId,
                frequencyId,
                auditPreference,
                technologyStack,
                languageLevel,
                openSourceScan,
                !Utils.isNullOrEmpty(applicationName),
                applicationName,
                applicationType,
                releaseName,
                owner,
                attributes,
                businessCriticality,
                sdlcStatus,
                microserviceName,
                isMicroservice);

        commonBuildStep.perform(build, workspace, launcher, listener, correlationId);
        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
        crossBuildAction.setPreviousStepBuildResult(build.getResult());
        if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
            crossBuildAction.setScanId(commonBuildStep.getScanId());
            crossBuildAction.setCorrelationId(correlationId);
        }
        try {
            build.save();
        } catch (IOException ex) {
            log.println("Error saving settings. Error message: " + ex.toString());
        }
    }

    private List<String> ValidateModel(boolean overrideGlobalAuth, PrintStream logger) {
        List<String> errors = new ArrayList<>();
        boolean anyV7Params = false;

        remediationScanPreferenceType = remediationScanPreferenceType != null ? remediationScanPreferenceType : FodEnums.RemediationScanPreferenceType.RemediationScanIfAvailable.getValue();
        inProgressScanActionType = inProgressScanActionType != null ? inProgressScanActionType : FodEnums.InProgressScanActionType.DoNotStartScan.getValue();
        inProgressBuildResultType = inProgressBuildResultType != null ? inProgressBuildResultType : FodEnums.InProgressBuildResultType.FailBuild.getValue();
        scanCentral = scanCentral == null ? "" : scanCentral;
        srcLocation = Utils.isNullOrEmpty(srcLocation) ? "./" : srcLocation;

        // Any have value and any don't have value
        if (overrideGlobalAuth && (Utils.isNullOrEmpty(username) || Utils.isNullOrEmpty(tenantId) || Utils.isNullOrEmpty(personalAccessToken))) {
            errors.add("Personal access token override requires all 3 be provided: username, personalAccessToken, tenantId");
        }
        Integer releaseIdInt = Utils.tryParseInt(releaseId, null);
        int techStack = Utils.tryParseInt(technologyStack);

        if (releaseIdInt == null && Utils.isNullOrEmpty(bsiToken)) {
            if (!Utils.isNullOrEmpty(applicationName) || Utils.isNullOrEmpty(releaseName)) {
                anyV7Params = true;
                List<String> aperrors = new ArrayList<>();

                if (Utils.isNullOrEmpty(applicationName)) aperrors.add("applicationName");
                if (!Utils.isValidEnumValue(BusinessCriticalityType.class, businessCriticality)) aperrors.add("businessCriticality");
                if (!Utils.isValidEnumValue(ApplicationType.class, applicationType)) aperrors.add("applicationType");
                if (isMicroservice && Utils.isNullOrEmpty(microserviceName)) aperrors.add("microserviceName");
                if (Utils.isNullOrEmpty(releaseName)) errors.add("releaseName");
                if (!Utils.isValidEnumValue(SDLCStatusType.class, sdlcStatus)) aperrors.add("sdlcStatus");
                if (owner == null || owner <= 0) aperrors.add("owner");
                if (techStack < 1) aperrors.add("technologyStack");

                if (!aperrors.isEmpty()) errors.add("Missing or invalid fields for auto provisioning" + String.join(", ", aperrors));
            } else errors.add("releaseId, bsiToken, or auto provision must be provided");
        }

        if (Utils.isNullOrEmpty(bsiToken)) {

            // Check new params to V7 other than auto provision which is handled above
            if (!Utils.isNullOrEmpty(assessmentType) ||
                    !Utils.isNullOrEmpty(auditPreference) ||
                    !Utils.isNullOrEmpty(entitlementId) ||
                    !Utils.isNullOrEmpty(frequencyId) ||
                    !Utils.isNullOrEmpty(languageLevel) ||
                    !Utils.isNullOrEmpty(openSourceScan) ||
                    !Utils.isNullOrEmpty(scanCentral) ||
                    !Utils.isNullOrEmpty(scanCentralBuildCommand) ||
                    !Utils.isNullOrEmpty(scanCentralBuildFile) ||
                    !Utils.isNullOrEmpty(scanCentralBuildToolVersion) ||
                    !Utils.isNullOrEmpty(scanCentralIncludeTests) ||
                    !Utils.isNullOrEmpty(scanCentralRequirementFile) ||
                    !Utils.isNullOrEmpty(scanCentralSkipBuild) ||
                    !Utils.isNullOrEmpty(scanCentralVirtualEnv) ||
                    !Utils.isNullOrEmpty(technologyStack)) {
                anyV7Params = true;
            }

            if (anyV7Params) {
                ValidationUtils.ScanCentralValidationResult vres = ValidationUtils.isValidScanCentralAndTechStack(scanCentral, techStack);
                boolean scValidationFailed = false;

                if (vres == ValidationUtils.ScanCentralValidationResult.Mismatched) {
                    Tuple2<String, Integer> t = resolveMismatch(scanCentral, techStack);

                    if (t == null) {
                        errors.add(String.format("scanCentral %s doesn't support technologyStack %s", scanCentral, techStack));
                        scValidationFailed = true;
                    } else {
                        scanCentral = t.getFirst();
                        techStack = t.getSecond();
                    }
                } else if (vres == ValidationUtils.ScanCentralValidationResult.ScanCentralRequired)
                    scanCentral = getScanCentralForTechStack(techStack);
                else if (vres == ValidationUtils.ScanCentralValidationResult.NoSelection) {
                    if (releaseIdInt != null) {
                        String noTechStackMsg = "techStack not provided";
                        AuthenticationModel authModel = new AuthenticationModel(overrideGlobalAuth,
                                username,
                                personalAccessToken,
                                tenantId);
                        StaticScanController staticScanController = new StaticScanController(ApiConnectionFactory.createApiConnection(authModel), logger, correlationId);
                        try {
                            GetStaticScanSetupResponse staticScanSetup = staticScanController.getStaticScanSettings(releaseIdInt);

                            if (staticScanSetup == null || Utils.isNullOrEmpty(staticScanSetup.getTechnologyStack())) {
                                errors.add(noTechStackMsg + " and no scan settings defined for release " + releaseId);
                                scValidationFailed = true;
                            } else {
                                techStack = staticScanSetup.getTechnologyStackId();
                                languageLevel = staticScanSetup.getLanguageLevel();
                                technologyStack = String.valueOf(techStack);
                                scanCentral = getScanCentralForTechStack(techStack);
                            }
                        } catch (IOException ex) {
                            errors.add(noTechStackMsg + " and failed to retrieve scan settings for release " + releaseId);
                            scValidationFailed = true;
                        }
                    }
                }

                if (!scValidationFailed) {
                    vres = ValidationUtils.isValidScanCentralAndTechStack(scanCentral, techStack);

                    switch (vres) {
                        case Mismatched:
                            Tuple2<String, Integer> t = resolveMismatch(scanCentral, techStack);

                            if (t == null) errors.add(String.format("scanCentral %s doesn't support techStack %s", scanCentral, techStack));
                            else {
                                scanCentral = t.getFirst();
                                techStack = t.getSecond();
                            }
                        case ScanCentralRequired:
                            scanCentral = getScanCentralForTechStack(techStack);
                        case NoSelection:
                            errors.add("scanCentral and/or techStack not provided");
                    }
                }
            }
        }

        if (openSourceScan != null && openSourceScan.equalsIgnoreCase("true") && techStack > 0) {
            if (ValidationUtils.isSonatypeScanNotAllowedForTechStack(techStack)) {
                errors.add("Open Source scans are not allowed for the selected Technology Type.");
            }
        }

        // ToDo: add more validation

        return errors;
    }

    private Tuple2<String, Integer> resolveMismatch(String scanCentral, int techStack) {
        switch (scanCentral) {
            case "Gradle":
            case "Maven":
                return new Tuple2<>(scanCentral, 7);
            case "MSBuild":
                return null;
            case "PHP":
                return new Tuple2<>(scanCentral, 9);
            case "Python":
                return new Tuple2<>(scanCentral, 10);
            default:
                scanCentral = getScanCentralForTechStack(techStack);

                if (scanCentral == "None") return null;

                return new Tuple2<>(scanCentral, techStack);
        }
    }

    private String getScanCentralForTechStack(int techStack) {
        switch (techStack) {
            case 1:
            case 23:
                return "MSBuild";
            case 7:
                return "Maven";
            case 9:
                return "PHP";
            case 10:
                return "Python";
            default:
                return "None";
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getDisplayName() {
            return "Run Fortify on Demand Upload";
        }

        @Override
        public String getFunctionName() {
            return "fodStaticAssessment";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        // Form validation
        @SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused"})
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter("usernameStaplerOnly") final String username,
                                                                  @QueryParameter("personalAccessTokenSelect") final String personalAccessToken,
                                                                  @QueryParameter("tenantIdStaplerOnly") final String tenantId,
                                                                  @AncestorInPath Job job) {
            job.checkPermission(Item.CONFIGURE);
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);


        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            return SharedUploadBuildStep.doFillEntitlementPreferenceItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillRemediationScanPreferenceTypeItems() {
            return SharedUploadBuildStep.doFillRemediationScanPreferenceTypeItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillUsernameItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPersonalAccessTokenSelectItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTenantIdItems(@AncestorInPath Job job) {
            return SharedUploadBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressScanActionTypeItems() {
            return SharedUploadBuildStep.doFillInProgressScanActionTypeItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressBuildResultTypeItems() {
            return SharedUploadBuildStep.doFillInProgressBuildResultTypeItems();
        }

        @JavaScriptMethod
        public String retrieveCurrentUserSession(JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
                UsersController usersController = new UsersController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(usersController.getCurrentUserSession());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlements(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
                AssessmentTypesController assessmentTypesController = new AssessmentTypesController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(assessmentTypesController.getStaticAssessmentTypeEntitlements(releaseId));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlementsForAutoProv(String appName, String relName, Boolean isMicroservice, String microserviceName, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
                ReleaseController releases = new ReleaseController(apiConnection, null, Utils.createCorrelationId());
                AssessmentTypesController assessments = new AssessmentTypesController(apiConnection, null, Utils.createCorrelationId());
                Integer relId = releases.getReleaseIdByName(appName.trim(), relName.trim(), isMicroservice, microserviceName);
                AssessmentTypeEntitlementsForAutoProv result = null;

                if (relId == null) {
                    result = new AssessmentTypeEntitlementsForAutoProv(null, assessments.getStaticAssessmentTypeEntitlements(isMicroservice), null);
                } else {
                    StaticScanController staticScanController = new StaticScanController(apiConnection, null, Utils.createCorrelationId());
                    GetStaticScanSetupResponse settings = staticScanController.getStaticScanSettings(relId);

                    result = new AssessmentTypeEntitlementsForAutoProv(relId, assessments.getStaticAssessmentTypeEntitlements(relId), settings);
                }

                return Utils.createResponseViewModel(result);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveStaticScanSettings(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
                StaticScanController staticScanController = new StaticScanController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(staticScanController.getStaticScanSettings(releaseId));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveAuditPreferences(Integer releaseId, Integer assessmentType, Integer frequencyType, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
                ReleaseController releaseController = new ReleaseController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(releaseController.getAuditPreferences(releaseId, assessmentType, frequencyType));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @JavaScriptMethod
        public String retrieveLookupItems(String type, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel);
                LookupItemsController lookupItemsController = new LookupItemsController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(lookupItemsController.getLookupItems(FodEnums.APILookupItemTypes.valueOf(type)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
        private transient FortifyStaticAssessment upload;

        protected Execution(FortifyStaticAssessment upload, StepContext context) {
            super(context);
            this.upload = upload;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running fodStaticAssessment step");
            upload.perform(getContext().get(Run.class), getContext().get(FilePath.class),
                    getContext().get(Launcher.class), getContext().get(TaskListener.class));

            return null;
        }
    }
}
