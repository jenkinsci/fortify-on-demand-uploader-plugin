const DastReleaseSetMode = Object.freeze({
     releaseId: 0,
    releaseSelect: 1
});

class DynamicScanAppAndReleaseSelection {

    constructor() {
        this.api = new Api(instance, descriptor);
    }

    'hide all scan settings controls until the appropriate relase id or application & release id is choosen.'
    hideAllScanSettings() {
        jq('.releaseIdView').hide();
        jq('.appAndReleaseNameView').hide();
        jq('#appAndReleaseNameErrorView').hide();
        jq('.dast-scan-setting').hide();

    }
    onReleaseIdFieldChanged() {
        debugger;
        dispatchEvent('releaseChanged', {releaseId: jq('#releaseIdField').val(), mode: DastReleaseSetMode.releaseId});
    }

    onReleaseMethodSelection() {
        this.hideAllScanSettings();
        const viewChoice = jq('#releaseTypeSelectList').val();

        switch (viewChoice) {
            case "UseReleaseId":
                this.initReleaseId();
                break;
            case "UseAppAndReleaseName":
                this.catchAuthError(
                    () => this.initAppAndReleaseSelection(true),
                    _ => dispatchEvent('releaseChanged', {mode: DastReleaseSetMode.releaseSelect})
                );
                break;
        }
    }

    async initReleaseId() {
        const savedReleaseId = await this.api.getSavedReleaseId();

        if (savedReleaseId) jq('#releaseIdField').val(savedReleaseId);

        jq('.releaseIdView').show();
        this.onReleaseIdFieldChanged();
    }

    async initAppAndReleaseSelection() {
        debugger;
        jq('.appAndReleaseNameView').show();
        jq('#appAndReleaseNameErrorView').hide();
       jq('#releaseSelectView').hide();

        showWithSpinner('#applicationSelectView');

        const currentSession = await this.api.getCurrentUserSession(getAuthInfo());
        console.log(currentSession);
        if (currentSession !== null) {
            if (currentSession.permissions.indexOf('CreateApplications') !== -1) {
                jq('#createAppSection').show();
            } else {
                jq('#createAppSection').hide();
            }

            if (currentSession.permissions.indexOf('ManageApplications') !== -1) {

                jq('#createReleaseSection').show();
            } else {

                jq('#createReleaseSection').hide();
            }

            dispatchEvent('userDetected', {userId: currentSession.userId, username: currentSession.username});
        } else {
            jq('#createAppSection').show();
             jq('#createReleaseSection').show();

            dispatchEvent('userDetected', {userId: null, username: null});
        }

        const savedReleaseId = await this.api.getSavedReleaseId();
        console.log(savedReleaseId);
        const [success, release] = savedReleaseId ? await this.api.getReleaseById(savedReleaseId, getAuthInfo()) : [false, null];

        hideSpinner('#applicationSelectView');

        if (success) {
            jq('#selectedApp')[0].innerText = release.applicationName;
            this.selectApplication(release.applicationId, release.applicationName);
            jq('#releaseSelectView').show();
            this.selectRelease(release.releaseId, release.releaseName);
            dispatchEvent('releaseChanged', {releaseId: release.releaseId, releaseName: release.releaseName, mode: DastReleaseSetMode.releaseSelect});
        } else {
            this.resetSelectApplication();
            this.resetSelectRelease();
            dispatchEvent('releaseChanged', {mode: DastReleaseSetMode.releaseSelect});
        }
    }
    resetSelectApplication() {
        jq('#selectedApp')[0].innerText = 'Select an application';
        jq('[name="userSelectedApplication"]').val(null);
    }

    selectApplication(appId, appName) {
        jq('#selectedApp')[0].innerText = appName;
        jq('[name="userSelectedApplication"]').val(appId);
    }

    resetSelectMicroservice() {
        jq('#selectedMicroservice')[0].innerText = 'Select a microservice';
        jq('[name="userSelectedMicroservice"]').val(null);
    }

    showApiRetrievalError() {
        this.hideAllScanSettings();
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

    onCredsChanged() {
        const viewChoice = jq('#releaseTypeSelectList').val();

        if (viewChoice === "UseAppAndReleaseName") {
            this.catchAuthError(
                () => this.initAppAndReleaseSelection(true),
                _ => dispatchEvent('releaseChanged', {mode: DastReleaseSetMode.releaseSelect})
            );
        }
    }

    onAppSelectedFromDialog(applicationId, applicationName, hasMicroservices) {
        debugger;
        jq('#microserviceSelectView').hide();
        jq('#releaseSelectView').hide();

        this.selectApplication(applicationId, applicationName);

        if (this.resetSelectRelease()) {
            // send empty releaseChanged event if release was selected before
            dispatchEvent('releaseChanged', {mode: DastReleaseSetMode.releaseSelect});
        }

        if (hasMicroservices) {
            jq('#microserviceSelectView').show();
        } else {
            jq('#releaseSelectView').show();
        }
        jq('#releaseSelectView').show();

    }

    onMicroserviceSelectedFromDialog(microserviceId, microserviceName) {
        jq('#releaseSelectView').hide();

        this.selectMicroservice(microserviceId, microserviceName);
        if (this.resetSelectRelease()) {
            dispatchEvent('releaseChanged', {mode: DastReleaseSetMode.releaseSelect});
        }

        jq('#releaseSelectView').show();
    }

    onReleaseSelectedFromDialog(releaseId, releaseName) {
        this.selectRelease(releaseId, releaseName);
        dispatchEvent('releaseChanged', {releaseId, releaseName, mode: DastReleaseSetMode.releaseSelect});
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
        dispatchEvent('releaseChanged', {releaseId, releaseName, mode: DastReleaseSetMode.releaseSelect});
    }

    onMicroserviceCreated(microserviceId, microserviceName) {
        this.selectMicroservice(microserviceId, microserviceName);
        this.resetSelectRelease();
        jq('#releaseSelectView').show();
        dispatchEvent('releaseChanged', {mode: DastReleaseSetMode.releaseSelect});
    }

    onReleaseCreated(releaseId, releaseName) {
        this.selectRelease(releaseId, releaseName);
        dispatchEvent('releaseChanged', {releaseId, releaseName, mode: DastReleaseSetMode.releaseSelect});
    }

    //</editor-fold>

    init() {
        debugger;
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
        debugger;
        const releaseIdRow = closestRow('#releaseIdSection-entry');
        const applicationSelectRow = closestRow('#application-entry');
        const releaseSelectRow = closestRow('#release-entry');
        const dastAssessmentrow = closestRow('#dastAssessmentTypeForm');
        const dastEntitlementrow = closestRow('#dastEntitlementForm');
        const dastScanTyperow = closestRow('#dastScanType');
        const dastSiteUrlRow =closestRow('#dastSiteUrl');

        releaseIdRow.addClass('releaseIdView');
        applicationSelectRow.addClass('appAndReleaseNameView');
        releaseSelectRow.addClass('appAndReleaseNameView');
        applicationSelectRow.prop('id', 'applicationSelectView');
        releaseSelectRow.prop('id', 'releaseSelectView');
        dastEntitlementrow.addClass('dast-scan-setting');
        dastAssessmentrow.addClass('dast-scan-setting');
        dastScanTyperow.addClass('dast-scan-setting');
        dastSiteUrlRow.addClass('dast-scan-setting');
    }
}

const appAndReleaseSelectionInstance = new DynamicScanAppAndReleaseSelection();
spinAndWait(() => jq('#releaseTypeSelectList').val()).then(appAndReleaseSelectionInstance.init.bind(appAndReleaseSelectionInstance));