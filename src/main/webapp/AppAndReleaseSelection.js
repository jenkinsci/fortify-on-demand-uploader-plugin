const ReleaseSetMode = Object.freeze({
    bsiToken: 0,
    releaseId: 1,
    releaseSelect: 2
});

const dastScanSetting = 'dast-scan-setting';
const dastWebSiteSetting = 'dast-standard-setting';
const dastWorkFlowSetting = 'dast-workflow-setting';
const dastCommonScopeSetting = 'dast-common-scan-scope';
const nwAuthSetting = 'dast-networkAuth-setting';
const loginAuthSetting = 'dast-login-macro';
const dastApiSetting = 'dast-api-setting';
const dastApiSpecificControls = 'dast-api-specific-controls';

class AppAndReleaseSelection {

    constructor() {
        this.api = new Api(instance, descriptor);
    }

    //<editor-fold desc="Init">

    hideAll() {
        jq('.releaseIdView').hide();
        jq('.bsiTokenView').hide();
        jq('.appAndReleaseNameView').hide();
        jq('#appAndReleaseNameErrorView').hide();
        jq('.openApiSourceControls').hide();
        jq('.graphQLSourceControls').hide();
        jq('.dast-scan-setting').hide();
        jq('.dast-standard-setting').hide();
        jq('.dast-workflow-setting').hide();
        jq('.dast-common-scan-scope').hide();
        jq('.dast-login-macro').hide();
        jq('.dast-networkAuth-setting').hide();
        jq('.dast-api-setting').hide();
        jq('dast-api-specific-controls').hide();
        jq('#requestFalsePositiveRemovalRow').hide();
         jq('#loginMacroFileCreationRow').hide();

    }

    onReleaseIdFieldChanged() {
        dispatchEvent('releaseChanged', {releaseId: jq('#releaseIdField').val(), mode: ReleaseSetMode.releaseId});
    }

    onReleaseMethodSelection() {
        this.hideAll();
        const viewChoice = jq('#releaseTypeSelectList').val();

        switch (viewChoice) {
            case "UseBsiToken":
                this.initBsiToken();
                break;
            case "UseReleaseId":
                this.initReleaseId();
                break;
            case "UseAppAndReleaseName":
                this.catchAuthError(
                    () => this.initAppAndReleaseSelection(true),
                    _ => dispatchEvent('releaseChanged', {mode: ReleaseSetMode.releaseSelect})
                );
                break;
        }
    }

    async initBsiToken() {
        const savedBsiToken = await this.api.getSavedBsiToken();
        if (savedBsiToken) {
            jq('#bsiTokenField').val(savedBsiToken);
        }
        dispatchEvent('releaseChanged', {mode: ReleaseSetMode.bsiToken});
        jq('.bsiTokenView').show();
    }

    async initReleaseId() {
        const savedReleaseId = await this.api.getSavedReleaseId();

        if (savedReleaseId) jq('#releaseIdField').val(savedReleaseId);

        jq('.releaseIdView').show();
        this.onReleaseIdFieldChanged();
    }

    async initAppAndReleaseSelection() {
        jq('.appAndReleaseNameView').show();
        jq('#appAndReleaseNameErrorView').hide();
        jq('#microserviceSelectView').hide();
        jq('#releaseSelectView').hide();

        showWithSpinner('#applicationSelectView');

        const currentSession = await this.api.getCurrentUserSession(getAuthInfo());
        if (currentSession !== null) {
            if (currentSession.permissions.indexOf('CreateApplications') !== -1) {
                jq('#createAppSection').show();
            } else {
                jq('#createAppSection').hide();
            }

            if (currentSession.permissions.indexOf('ManageApplications') !== -1) {
                jq('#createMicroserviceSection').show();
                jq('#createReleaseSection').show();
            } else {
                jq('#createMicroserviceSection').hide();
                jq('#createReleaseSection').hide();
            }

            dispatchEvent('userDetected', {userId: currentSession.userId, username: currentSession.username});
        } else {
            jq('#createAppSection').show();
            jq('#createMicroserviceSection').show();
            jq('#createReleaseSection').show();

            dispatchEvent('userDetected', {userId: null, username: null});
        }

        const savedReleaseId = await this.api.getSavedReleaseId();
        const [success, release] = savedReleaseId ? await this.api.getReleaseById(savedReleaseId, getAuthInfo()) : [false, null];

        hideSpinner('#applicationSelectView');

        if (success) {
            jq('#selectedApp')[0].innerText = release.applicationName;
            this.selectApplication(release.applicationId, release.applicationName);
            if (release.microserviceId) {
                jq('#microserviceSelectView').show();
                this.selectMicroservice(release.microserviceId, release.microserviceName);
            }
            jq('#releaseSelectView').show();
            this.selectRelease(release.releaseId, release.releaseName);
            dispatchEvent('releaseChanged', {
                releaseId: release.releaseId,
                releaseName: release.releaseName,
                mode: ReleaseSetMode.releaseSelect
            });
        } else {
            this.resetSelectApplication();
            this.resetSelectRelease();
            dispatchEvent('releaseChanged', {mode: ReleaseSetMode.releaseSelect});
        }
    }

    //</editor-fold>

    //<editor-fold desc="Helpers">

    resetSelectApplication() {
        jq('#selectedApp')[0].innerText = 'Select an application';
        jq('[name="userSelectedApplication"]').val(null);
    }

    selectApplication(appId, appName) {
        jq('#selectedApp')[0].innerText = appName;
        jq('[name="userSelectedApplication"]').val(appId);
    }

    resetSelectMicroservice() {
        if (jq('#selectedMicroservice') !== undefined && jq('#selectedMicroservice')[0] !== undefined) {
            jq('#selectedMicroservice')[0].innerText = 'Select a microservice';
            jq('[name="userSelectedMicroservice"]').val(null);
        }
    }

    showApiRetrievalError() {
        this.hideAll();
        jq('#appAndReleaseNameErrorView').show();
    }

    selectMicroservice(microserviceId, microserviceName) {
        jq('#selectedMicroservice')[0].innerText = microserviceName;
        jq('[name="userSelectedMicroservice"]').val(microserviceId);
    }

    resetSelectRelease() {
        jq('#selectedRelease')[0].innerText = 'Select a release';
        const prevReleaseId = Number(jq('[name="userSelectedRelease"]').val());
        jq('[name="userSelectedRelease"]').val(null);
        return prevReleaseId;
    }

    selectRelease(releaseId, releaseName) {
        jq('#selectedRelease')[0].innerText = releaseName;
        const prevReleaseId = Number(jq('[name="userSelectedRelease"]').val());
        jq('[name="userSelectedRelease"]').val(releaseId);
        return prevReleaseId;
    }

    async catchAuthError(op, onCatch) {
        try {
            await op();
        } catch (err) {
            if (this.api.isAuthError(err)) {
                this.showApiRetrievalError();
            } else {
                throw err;
            }

        }
    }

    //</editor-fold>

    //<editor-fold desc="Event Handles">

    onCredsChanged() {
        const viewChoice = jq('#releaseTypeSelectList').val();

        if (viewChoice == "UseAppAndReleaseName") {
            this.catchAuthError(
                () => this.initAppAndReleaseSelection(true),
                _ => dispatchEvent('releaseChanged', {mode: ReleaseSetMode.releaseSelect})
            );
        }
    }

    onAppSelectedFromDialog(applicationId, applicationName, hasMicroservices) {
        jq('#microserviceSelectView').hide();
        jq('#releaseSelectView').hide();

        this.selectApplication(applicationId, applicationName);
        this.resetSelectMicroservice();

        if (this.resetSelectRelease()) {
            // send empty releaseChanged event if release was selected before
            dispatchEvent('releaseChanged', {mode: ReleaseSetMode.releaseSelect});
        }

        if (hasMicroservices) {
            jq('#microserviceSelectView').show();
        } else {
            jq('#releaseSelectView').show();
        }
    }

    onMicroserviceSelectedFromDialog(microserviceId, microserviceName) {
        jq('#releaseSelectView').hide();

        this.selectMicroservice(microserviceId, microserviceName);
        if (this.resetSelectRelease()) {
            dispatchEvent('releaseChanged', {mode: ReleaseSetMode.releaseSelect});
        }

        jq('#releaseSelectView').show();
    }

    onReleaseSelectedFromDialog(releaseId, releaseName) {
        this.selectRelease(releaseId, releaseName);
        dispatchEvent('releaseChanged', {releaseId, releaseName, mode: ReleaseSetMode.releaseSelect});
    }

    async onApplicationCreated(applicationId, applicationName, hasMicroservices, microserviceId, microserviceName, releaseId, releaseName) {
        jq('#microserviceSelectView').hide();
        jq('#releaseSelectView').hide();

        this.selectApplication(applicationId, applicationName);
        if (hasMicroservices) {
            jq('#microserviceSelectView').show();
            this.selectMicroservice(microserviceId, microserviceName);
        } else {
            this.resetSelectMicroservice();
        }

        jq('#releaseSelectView').show();

        this.selectRelease(releaseId, releaseName);
        dispatchEvent('releaseChanged', {releaseId, releaseName, mode: ReleaseSetMode.releaseSelect});
    }

    onMicroserviceCreated(microserviceId, microserviceName) {
        this.selectMicroservice(microserviceId, microserviceName);
        this.resetSelectRelease();
        jq('#releaseSelectView').show();
        dispatchEvent('releaseChanged', {mode: ReleaseSetMode.releaseSelect});
    }

    onReleaseCreated(releaseId, releaseName) {
        this.selectRelease(releaseId, releaseName);
        dispatchEvent('releaseChanged', {releaseId, releaseName, mode: ReleaseSetMode.releaseSelect});
    }

    //</editor-fold>

    init() {
        this.fEntriesIdPlacement();
        this.onReleaseMethodSelection();
        jq('#releaseTypeSelectList').off('change').change(() => this.onReleaseMethodSelection());
        jq('#releaseIdField').off('change').change(_ => this.onReleaseIdFieldChanged());

        subscribeToEvent('authInfoChanged', () => this.onCredsChanged());
        subscribeToEvent('dialogSelectedApplication', e => this.onAppSelectedFromDialog(e.detail.applicationId, e.detail.applicationName, e.detail.hasMicroservices));
        subscribeToEvent('dialogSelectedMicroservice', e => this.onMicroserviceSelectedFromDialog(e.detail.microserviceId, e.detail.microserviceName));
        subscribeToEvent('dialogSelectedRelease', e => this.onReleaseSelectedFromDialog(e.detail.releaseId, e.detail.releaseName));
        subscribeToEvent('applicationCreated', e => this.onApplicationCreated(e.detail.applicationId, e.detail.applicationName, e.detail.hasMicroservices, e.detail.microserviceId, e.detail.microserviceName, e.detail.releaseId, e.detail.releaseName));
        subscribeToEvent('microserviceCreated', e => this.onMicroserviceCreated(e.detail.microserviceId, e.detail.microserviceName));
        subscribeToEvent('releaseCreated', e => this.onReleaseCreated(e.detail.releaseId, e.detail.releaseName));
    }

    fEntriesIdPlacement() {
        const releaseIdRow = closestRow('#releaseIdSection-entry');
        const bsiTokenRow = closestRow('#bsiTokenSection-entry');
        const applicationSelectRow = closestRow('#application-entry');
        const microserviceSelectRow = closestRow('#microservice-entry');
        const releaseSelectRow = closestRow('#release-entry');
        const dastAssessmentrow = closestRow('#dastAssessmentTypeForm');
        const dastEntitlementrow = closestRow('#dastEntitlementForm');
        const dastScanTyperow = closestRow('#dastScanType');
        const dastSiteUrlRow = closestRow('#dast-standard-site-url');
        const dastEnv = closestRow('#dast-scan-setting-evn')
        const dastTimeZone = closestRow('#timezone');
        const dastWrkFlowMacroUpload = closestRow('#dast-workflow-macro-upload');
        const dastWrkFlowAllowedHost = closestRow('#listWorkflowDrivenAllowedHostUrl');
        const dastStandardScope = closestRow('#dast-standard-scan-scope');
        const dastExcludeUrl = closestRow('#standardScanTypeExcludeUrlsRow');
        const dastWebSiteTimeBoxScan = closestRow('#dast-timeBox-scan');
        const networkAuth = closestRow('#webSiteNetworkAuthSettingEnabledRow')
        const loginMacro = closestRow('#login-macro-row');
        const commonWebScopeSetting = closestRow('#dast-common-scope');
        const commonWebScopeSettingAttr = closestRow('#dast-common-scope-attr');
        const commonScanPolicy = closestRow('#dast-standard-scan-policy');

        releaseIdRow.addClass('releaseIdView');
        bsiTokenRow.addClass('bsiTokenView');
        applicationSelectRow.addClass('appAndReleaseNameView');
        microserviceSelectRow.addClass('appAndReleaseNameView');
        releaseSelectRow.addClass('appAndReleaseNameView');

        applicationSelectRow.prop('id', 'applicationSelectView');
        microserviceSelectRow.prop('id', 'microserviceSelectView');
        releaseSelectRow.prop('id', 'releaseSelectView');
        dastEntitlementrow.addClass(dastScanSetting);
        dastAssessmentrow.addClass(dastScanSetting);
        dastScanTyperow.addClass(dastScanSetting);
        dastStandardScope.addClass(dastScanSetting);
        dastEnv.addClass(dastScanSetting);
        dastTimeZone.addClass(dastScanSetting);
        networkAuth.addClass(nwAuthSetting);
        loginMacro.addClass(loginAuthSetting);

        <!--Scan Specific Scope Sections-->
        dastWrkFlowMacroUpload.addClass(dastWorkFlowSetting);
        dastWrkFlowAllowedHost.addClass(dastWorkFlowSetting);
        dastExcludeUrl.addClass(dastWebSiteSetting);
        dastSiteUrlRow.addClass(dastWebSiteSetting);
        dastWebSiteTimeBoxScan.addClass(dastWebSiteSetting);
        commonWebScopeSetting.addClass(dastCommonScopeSetting);
        commonWebScopeSettingAttr.addClass(dastCommonScopeSetting);
        commonScanPolicy.addClass(dastCommonScopeSetting);
        <!--Scope sections-->

    }
}

const appAndReleaseSelectionInstance = new AppAndReleaseSelection();
spinAndWait(() => jq('#releaseTypeSelectList').val()).then(appAndReleaseSelectionInstance.init.bind(appAndReleaseSelectionInstance));