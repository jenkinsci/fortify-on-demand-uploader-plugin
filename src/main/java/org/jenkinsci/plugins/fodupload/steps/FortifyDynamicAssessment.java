package org.jenkinsci.plugins.fodupload.steps;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.fodupload.*;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.controllers.*;
import org.jenkinsci.plugins.fodupload.models.*;
import org.jenkinsci.plugins.fodupload.models.response.AssessmentTypeEntitlementsForAutoProv;
import org.jenkinsci.plugins.fodupload.models.response.GetStaticScanSetupResponse;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.Set;

import static org.jenkinsci.plugins.fodupload.models.FodEnums.*;

@SuppressFBWarnings("unused")
public class FortifyDynamicAssessment extends FortifyStep {
    private static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    private final String correlationId = UUID.randomUUID().toString();
    private String releaseId;
    @Deprecated
    private Boolean overrideGlobalConfig;
    private String username;
    private String personalAccessToken;
    private String tenantId;
    private String webSiteUrl;
    private boolean purchaseEntitlements;
    private String entitlementPreference;
    private  String entitlementFreqType;
    private String remediationScanPreferenceType;
    private String inProgressScanActionType;
    private String inProgressBuildResultType;
    private String assessmentType;
    private String entitlementId;
    private String frequencyId;
    private String auditPreference;
    private String applicationName;
    private String applicationType;
    private String releaseName;
    private Integer owner;
    private String attributes;
    private String businessCriticality;
    private String sdlcStatus;
    private boolean enableRedundantPageDetection;
    private String scanTimebox;
    private DastScanSharedBuildStep commonBuildStep;
    @DataBoundConstructor
    public FortifyDynamicAssessment() {
        super();
    }
    public String getSelectedScanType() {
        return selectedScanType;
    }
    @DataBoundSetter
    public void setSelectedScanType(String selectedScanType) {
        this.selectedScanType = selectedScanType;
    }
    public String getScanScope() {
        return scanScope;
    }
    @DataBoundSetter
    public void setScanScope(String scanScope) {
        this.scanScope = scanScope;
    }
    private String scanScope;
    private String selectedScanType;
    public String getWebSiteUrl() {
        return webSiteUrl;
    }
    @DataBoundSetter
    public void setWebSiteUrl(String webSiteUrl) {
        this.webSiteUrl = webSiteUrl;
    }
    public String getEntitlementFreqType() {
        return entitlementFreqType;
    }
    @DataBoundSetter
    public void setEntitlementFreqType(String entitlementFreqType) {
        this.entitlementFreqType = entitlementFreqType;
    }
    public String getWorkflowMacroHosts() {
        return workflowMacroHosts;
    }
    @DataBoundSetter
    public void setWorkflowMacroHosts(String workflowMacroHosts) {
        this.workflowMacroHosts = workflowMacroHosts;
    }
    @Nullable
    private String workflowMacroHosts;
    public String getWorkflowMacroId() {
        return workflowMacroId;
    }
    @DataBoundSetter
    public void setWorkflowMacroId(String workflowMacroId) {
        this.workflowMacroId = workflowMacroId;
    }
    private String workflowMacroId;
    public String getSelectedDynamicTimeZone() {
        return selectedDynamicTimeZone;
    }
    @DataBoundSetter
    public void setSelectedDynamicTimeZone(String selectedDynamicTimeZone) {
        this.selectedDynamicTimeZone = selectedDynamicTimeZone;
    }
    private String selectedDynamicTimeZone;
    public String getDastEnv() {
        return dastEnv;
    }
    @DataBoundSetter
    public void setDastEnv(String dastEnv) {
        this.dastEnv = dastEnv;
    }
    private String dastEnv;
    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    @DataBoundSetter
    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }
    public String getScanPolicy() {
        return scanPolicy;
    }
    @DataBoundSetter
    public void setScanPolicy(String scanPolicy) {
        this.scanPolicy = scanPolicy;
    }
    private String scanPolicy;
    public String getScanTimebox() {
        return scanTimebox;
    }

    @DataBoundSetter
    public void setScanTimebox(String scanTimebox) {
        this.scanTimebox = scanTimebox;
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

//    public String getSrcLocation() {
//        return srcLocation;
//    }
//
//    @DataBoundSetter
//    public void setSrcLocation(String srcLocation) {
//        this.srcLocation = srcLocation != null ? srcLocation.trim() : "";
//    }

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
    public String getAssessmentType() {
        return assessmentType;
    }

    @DataBoundSetter
    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    //@SuppressWarnings("unused")
//    public String getEntitlementId() {
//        return entitlementId;
//    }
//
//    @DataBoundSetter
//    public void setEntitlementId(String entitlementId) {
//        this.entitlementId = entitlementId;
//    }

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

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        PrintStream log = listener.getLogger();

        log.println("Fortify on Demand Dynamic Scan PreBuild Running...");

        // When does this happen? If this only happens in syntax gen, then just use ServerClient
        boolean overrideGlobalAuthConfig = !Utils.isNullOrEmpty(username);
        List<String> errors = null;
        try {
            errors = ValidateAuthModel(overrideGlobalAuthConfig);

            if (errors.isEmpty()) {
                // ToDo: can I construct the api?
                // errors = ValidateModel(api, log);
            }
        } catch (FormValidation e) {
            throw new RuntimeException(e);
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments: Missing or invalid fields for auto provisioning: " + String.join(", ", errors));
        }

        switch (selectedScanType)
        {
            case "Standard":
                // construct the shared build for standard scan.



                break;
            case "Workflow-Driven":
                 break;


        }

//        boolean overrideGlobalConfig, String username,
//                String personalAccessToken, String tenantId,
//                String releaseId, String selectedReleaseType,
//                String webSiteUrl, String dastEnv,
//                String scanTimebox,
//                List<String> standardScanTypeExcludeUrlsRow,
//                String scanPolicyType, boolean scanScope,
//        String selectedScanType, String selectedDynamicTimeZone,
//        boolean webSiteLoginMacroEnabled, boolean webSiteNetworkAuthSettingEnabled,
//        boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
//                String loginMacroId, String workflowMacroId, String allowedHost, String webSiteNetworkAuthPassword,
//                String userSelectedApplication,
//                String userSelectedRelease, String assessmentTypeId,
//                String entitlementId,
//                String entitlementFrequencyType, String userSelectedEntitlement,
//                String selectedDynamicGeoLocation, String selectedNetworkAuthType,
//        boolean timeBoxChecked

//        commonBuildStep = new DastScanSharedBuildStep (
//                overrideGlobalAuthConfig,
//                username,
//                personalAccessToken,
//                tenantId,
//                releaseId,selectedRel, webSiteUrl,dastEnv,scanTimebox,null, scanPolicy,scanScope,selectedScanType,
//               );

        return true;
    }
    private List<String> ValidateAuthModel(boolean overrideGlobalAuth) throws FormValidation {
        List<String> errors = new ArrayList<>();

        // Any have value and any don't have value
        if (overrideGlobalAuth && (Utils.isNullOrEmpty(username) || Utils.isNullOrEmpty(tenantId) || Utils.isNullOrEmpty(personalAccessToken))) {
            errors.add("Personal access token override requires all 3 be provided: username, personalAccessToken, tenantId");
        }

        return errors;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution (this, context);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, IllegalArgumentException {
        PrintStream log = listener.getLogger();
//        log.println("Fortify on Demand Upload Running...");
//        build.addAction(new CrossBuildAction());
//        try {
//            build.save();
//        } catch (IOException ex) {
//            log.println("Error saving settings. Error message: " + ex.toString());
//        }
//        boolean overrideGlobalAuthConfig = !Utils.isNullOrEmpty(username);
//        List<String> errors = null;
//
//        try {
//            errors = ValidateAuthModel(overrideGlobalAuthConfig);
//
//            if (errors.isEmpty()) {
//                AuthenticationModel authModel = new AuthenticationModel(overrideGlobalAuthConfig,
//                        username,
//                        personalAccessToken,
//                        tenantId);
//                errors = ValidateModel(ApiConnectionFactory.createApiConnection(authModel, workspace.isRemote(), launcher, log), log);
//            }
//        } catch (FormValidation e) {
//            throw new RuntimeException(e);
//        }
//
//        if (!errors.isEmpty()) {
//            throw new IllegalArgumentException("Invalid arguments:\n\t" + String.join("\n\t", errors));
//        }
//
//        commonBuildStep = new SharedUploadBuildStep(releaseId,
//                bsiToken,
//                overrideGlobalAuthConfig,
//                username,
//                personalAccessToken,
//                tenantId,
//                purchaseEntitlements,
//                entitlementPreference,
//                srcLocation,
//                remediationScanPreferenceType,
//                inProgressScanActionType,
//                inProgressBuildResultType,
//                scanCentral,
//                scanCentralSkipBuild != null && scanCentralSkipBuild.equalsIgnoreCase("true"),
//                scanCentralBuildCommand,
//                scanCentralBuildFile,
//                scanCentralBuildToolVersion,
//                scanCentralVirtualEnv,
//                scanCentralRequirementFile,
//                assessmentType,
//                entitlementId,
//                frequencyId,
//                auditPreference,
//                technologyStack,
//                languageLevel,
//                openSourceScan,
//                !Utils.isNullOrEmpty(applicationName),
//                applicationName,
//                applicationType,
//                releaseName,
//                owner,
//                attributes,
//                businessCriticality,
//                sdlcStatus,
//                microserviceName,
//                isMicroservice);
//
//        commonBuildStep.perform(build, workspace, launcher, listener, correlationId);
//        CrossBuildAction crossBuildAction = build.getAction(CrossBuildAction.class);
//        crossBuildAction.setPreviousStepBuildResult(build.getResult());
//        if (Result.SUCCESS.equals(crossBuildAction.getPreviousStepBuildResult())) {
//            crossBuildAction.setScanId(commonBuildStep.getScanId());
//            crossBuildAction.setCorrelationId(correlationId);
//        }
//        try {
//            build.save();
//        } catch (IOException ex) {
//            log.println("Error saving settings. Error message: " + ex.toString());
//        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getDisplayName() {
            return "Run Fortify on Demand Upload";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "fodDynamicAssessment";
        }

        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        @POST
        public FormValidation doTestPersonalAccessTokenConnection(@QueryParameter("usernameStaplerOnly") final String username,
                                                                  @QueryParameter("personalAccessTokenSelect") final String personalAccessToken,
                                                                  @QueryParameter("tenantIdStaplerOnly") final String tenantId,
                                                                  @AncestorInPath Job job) throws FormValidation {
            job.checkPermission(Item.CONFIGURE);
            return SharedUploadBuildStep.doTestPersonalAccessTokenConnection(username, personalAccessToken, tenantId, job);

        }

        public static ListBoxModel doFillDastEnvItems() {
            return DastScanSharedBuildStep.doFillDastEnvItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillEntitlementPreferenceItems() {
            return DastScanSharedBuildStep.doFillEntitlementPreferenceItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillUsernameItems(@AncestorInPath Job job) {
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillPersonalAccessTokenSelectItems(@AncestorInPath Job job) {
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTenantIdItems(@AncestorInPath Job job) {
            return DastScanSharedBuildStep.doFillStringCredentialsItems(job);
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressScanActionTypeItems() {
            return DastScanSharedBuildStep.doFillInProgressScanActionTypeItems();
        }

        @SuppressWarnings("unused")
        public static ListBoxModel doFillScanPolicyItems() {
            return DastScanSharedBuildStep.doFillScanPolicyItems();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillInProgressBuildResultTypeItems() {
            return DastScanSharedBuildStep.doFillInProgressBuildResultTypeItems();
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveCurrentUserSession(JSONObject authModelObject) {
            try {

                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                UsersController usersController = new UsersController(apiConnection, null, Utils.createCorrelationId());
                return Utils.createResponseViewModel(usersController.getCurrentUserSession());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private static <T extends Enum<T>> ListBoxModel doFillFromEnum(Class<T> enumClass) {
            ListBoxModel items = new ListBoxModel();
            for (T selected : EnumSet.allOf(enumClass)) {
                items.add(new ListBoxModel.Option(selected.toString(), selected.name()));
            }
            return items;
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlements(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                AssessmentTypesController assessmentTypesController = new AssessmentTypesController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(assessmentTypesController.getDynamicAssessmentTypeEntitlements(false));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveAssessmentTypeEntitlementsForAutoProv(String appName, String relName, Boolean isMicroservice, String microserviceName, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
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

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveDynamicScanSettings(Integer releaseId, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                DastScanController dastScanController = new DastScanController(apiConnection, null, Utils.createCorrelationId());
                return Utils.createResponseViewModel(dastScanController.getDastScanSettings(releaseId));

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveAuditPreferences(Integer releaseId, Integer assessmentType, Integer frequencyType, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                ReleaseController releaseController = new ReleaseController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(releaseController.getAuditPreferences(releaseId, assessmentType, frequencyType));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @SuppressWarnings("unused")
        @JavaScriptMethod
        public String retrieveLookupItems(String type, JSONObject authModelObject) {
            try {
                AuthenticationModel authModel = Utils.getAuthModelFromObject(authModelObject);
                FodApiConnection apiConnection = ApiConnectionFactory.createApiConnection(authModel, false, null, null);
                LookupItemsController lookupItemsController = new LookupItemsController(apiConnection, null, Utils.createCorrelationId());

                return Utils.createResponseViewModel(lookupItemsController.getLookupItems(APILookupItemTypes.valueOf(type)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    private static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;
        private transient FortifyDynamicAssessment upload;

        protected Execution(FortifyDynamicAssessment upload, StepContext context) {
            super(context);
            this.upload = upload;
        }

        @Override
        protected Void run() throws Exception {
            getContext().get(TaskListener.class).getLogger().println("Running fodDynamicAssessment step");
            upload.perform(getContext().get(Run.class), getContext().get(FilePath.class),
                    getContext().get(Launcher.class), getContext().get(TaskListener.class));

            return null;
        }
    }
}
