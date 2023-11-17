package org.jenkinsci.plugins.fodupload;

import hudson.model.*;
import org.jenkinsci.plugins.fodupload.models.*;

import java.util.List;

public class DastSharedBuildStepUpdate {


    private final DastScanJobModel model;
    private final AuthenticationModel authModel;

    public static final ThreadLocal<TaskListener> taskListener = new ThreadLocal<>();
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String USERNAME = "username";
    public static final String PERSONAL_ACCESS_TOKEN = "personalAccessToken";
    public static final String TENANT_ID = "tenantId";

    private int scanId;

    public DastSharedBuildStepUpdate(DastScanJobModel model, AuthenticationModel authModel) {
        this.model = model;
        this.authModel = authModel;
    }

    public DastSharedBuildStepUpdate(boolean overrideGlobalConfig, String username,
                                   String personalAccessToken, String tenantId,
                                   String releaseId, String selectedReleaseType,
                                   String webSiteUrl, String dastEnv,
                                   String scanTimebox,
                                   List<String> standardScanTypeExcludeUrlsRow,
                                   String scanPolicyType, boolean scanScope,
                                   String selectedScanType, String selectedDynamicTimeZone,
                                   boolean webSiteLoginMacroEnabled, boolean webSiteNetworkAuthSettingEnabled,
                                   boolean enableRedundantPageDetection, String webSiteNetworkAuthUserName,
                                   String loginMacroId, String workflowMacroId, String allowedHost, String webSiteNetworkAuthPassword,
                                   String userSelectedApplication,
                                   String userSelectedRelease, String assessmentTypeId,
                                   String entitlementId,
                                   String entitlementFrequencyType, String userSelectedEntitlement,
                                   String selectedDynamicGeoLocation, String selectedNetworkAuthType,
                                   boolean timeBoxChecked) {

        authModel = new AuthenticationModel(overrideGlobalConfig, username, personalAccessToken, tenantId);


        model = new DastScanJobModel(overrideGlobalConfig, username, personalAccessToken, tenantId,
                releaseId, selectedReleaseType, webSiteUrl
                , dastEnv, scanTimebox, standardScanTypeExcludeUrlsRow, scanPolicyType, scanScope, selectedScanType
                , selectedDynamicTimeZone, webSiteLoginMacroEnabled,
                webSiteNetworkAuthSettingEnabled, enableRedundantPageDetection,
                webSiteNetworkAuthUserName, loginMacroId, workflowMacroId, allowedHost
                , webSiteNetworkAuthPassword, userSelectedApplication,
                userSelectedRelease, assessmentTypeId, entitlementId,
                entitlementFrequencyType, userSelectedEntitlement,
                selectedDynamicGeoLocation, selectedNetworkAuthType);

    }
}
