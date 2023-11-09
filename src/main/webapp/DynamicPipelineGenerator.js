const fodpRowSelector = '.fodp-field-row, .fodp-field-row-verr';
const fodpOverrideRowsSelector = '.fodp-row-relid-ovr';
const appAttributeKeyDelimiter = ';';
const appAttributeKeyValueDelimiter = ':';
const dastScanTypes = [{"value": 'Standard', "text": 'Standard'}, {
    "value": 'Workflow-driven',
    "text": 'Workflow-driven'
}];
const dastScanPolicyDefaultValues =
    {
        "WebSiteScan": {"ScanPolicy": "standard"},
        "WorkflowDrivenScan": {"ScanPolicy": "standard"},
        "ApiScan": {"ScanPolicy": "API Scan"}
    }


const dastScanSetting = 'dast-scan-setting';
const dastWebSiteSetting = 'dast-standard-setting';
const dastWorkFlowSetting = 'dast-workflow-setting';
const dastCommonScopeSetting = 'dast-common-scan-scope';
const nwAuthSetting = 'dast-networkAuth-setting';
const loginAuthSetting = 'dast-login-macro'

class DynamicPipelineGenerator {
    constructor() {
        this.api = new Api(null, descriptor);
        this.currentSession = null
        this.appAttributes = {};
        this.autoProvMode = false;
        this.overrideServerSettings = false;
        this.overrideAuth = false;
        this.releaseId = null;
        this.uiLoaded = false;
    }

    async preinit() {
        debugger;
        jq('#fodp-authUser > input').attr('id', 'usernameField');
        jq('#fodp-authPAT select.credentials-select').attr('id', 'patField');
        jq('#fodp-authTenant > input').attr('id', 'tenantIdField');

        jq('.fodp-field')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.addClass('fodp-field-row');
                let vtr = getValidationErrRow(tr);

                if (vtr) vtr.addClass('fodp-field-row-verr');
            });
        jq('.fodp-row-hidden')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.hide();
                let vtr = getValidationErrRow(tr);

                if (vtr) vtr.hide();
            });
        jq(fodpRowSelector).hide();

        // Move css classes prefixed with fodp-row to the row element
        jq('.fodp-field')
            .each((i, e) => {
                let jqe = jq(e);
                let classes = jqe.attr('class').split(' ');

                for (let c of classes) {
                    if (c.startsWith('fodp-row-')) {
                        jqe.removeClass(c);
                        let tr = jqe.closest('.fodp-field-row');

                        tr.addClass(c)

                        // Copy Scan Central and BSI css classes to validation-error-area and help-area rows
                        if (c.startsWith('fodp-row-sc') ||
                            c.startsWith('fodp-row-autoProv') ||
                            c === 'fodp-row-all' ||
                            c === 'fodp-row-relid') {
                            let vtr = getValidationErrRow(tr);
                            let htr = getHelpRow(tr);

                            if (vtr) vtr.addClass(c);
                            if (htr) htr.addClass(c);
                        }
                    }
                }
            });
        jq('.fodp-row-all').show();

        // &nbsp; breaks Jelly compilation
        jq('#releaseLookup').parent().append('&nbsp;');

        jq('[name="overrideGlobalConfig"]')
            .change(_ => {
                this.overrideAuth = jq('[name="overrideGlobalConfig"]').prop('checked');
                this.onAuthChanged();
            });

        jq('#usernameField')
            .change(_ => this.onAuthChanged());

        jq('#patField')
            .change(_ => this.onAuthChanged());

        jq('#tenantIdField')
            .change(_ => this.onAuthChanged());

        jq('.fodp-row-relid-ovr')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.addClass('fodp-row-relid-ovr');
                tr.hide();

                let vtr = getValidationErrRow(tr);

                if (vtr) {
                    vtr.addClass('fode-field-row-verr');
                    vtr.hide();
                }
            });
        await this.init();
    }

    hideMessages() {
        jq('#fodp-error').hide();
        jq('#fodp-msg').hide();
    }

    showMessage(msg, isError) {
        let msgElem;
        if (isError) msgElem = jq('#fodp-error');
        else msgElem = jq('#fodp-msg');
        msgElem.text(msg);
        msgElem.show();
    }

    async loadAuditPrefOptions(assessmentType, frequencyId) {
        let apSel = jq('#auditPreferenceSelect');
        let curVal = apSel.val();
        let apCont = jq('#auditPreferenceForm');
        let setSpinner = apCont.hasClass('spinner') === false;
        if (setSpinner) apCont.addClass('spinner');
        apSel.find('option').remove();
        assessmentType = numberOrNull(assessmentType);
        frequencyId = numberOrNull(frequencyId);
        if (!this.autoProvMode && this.releaseId && assessmentType && frequencyId) {
            try {
                let prefs = await this.api.getAuditPreferences(this.releaseId, assessmentType, frequencyId, getAuthInfo());

                if (prefs.automated) apSel.append(_auditPrefOption.automated);
                if (prefs.manual) apSel.append(_auditPrefOption.manual);
            } catch (e) {
                apSel.hide();
                apCont.removeClass('spinner');
                jq('#auditPreferenceForm > div').append('<div class="fode-error">Fatal error loading tenant data, please refresh</div>');
                return;
            }
        } else if (this.autoProvMode) {
            apSel.append(_auditPrefOption.automated);
            if (!jq('#autoProvIsMicro').prop('checked')) apSel.append(_auditPrefOption.manual);
        }

        let optCnt = apSel.find('option').length;

        if (optCnt < 1) {
            apSel.append(_auditPrefOption.automated);
            apSel.prop('disabled', true);

            if (setSpinner) apCont.removeClass('spinner');
        } else if (optCnt === 1) {
            // For some reason selection wouldn't work without setTimeout
            setTimeout(_ => {
                apSel.find('option').first().prop('selected', true);
                apSel.prop('disabled', true);

                if (setSpinner) apCont.removeClass('spinner');
            }, 50);
        } else {
            // For some reason selection wouldn't work without setTimeout
            setTimeout(_ => {
                if (curVal) apSel.val(curVal);
                else apSel.find('option').first().prop('selected', true);
                apSel.prop('disabled', false);

                if (setSpinner) apCont.removeClass('spinner');
            }, 50);
        }
    }

    async retrieveCurrentSession() {
        if (this.currentSession === null) {
            try {
                this.currentSession = await this.api.getCurrentUserSession(getAuthInfo());
                return this.currentSession !== null;
            } catch (e) {
                return false;
            }
        }
        return true;
    }

    async init() {
        debugger;
        try {
            this.hideMessages();
            this.showMessage('Set a release id');
            jq('#autoProvAppName')
                .change(_ => this.loadEntitlementOptions());
            jq('#autoProvBussCrit')
                .change(_ => this.populateHiddenFields());
            jq('#autoProvAppType')
                .change(_ => this.populateHiddenFields());
            jq('#scanTypeList')
                .change(_ => this.onScanTypeChanged());
            jq('#autoProvRelName')
                .change(_ => this.loadEntitlementOptions());
            jq('#autoProvSdlc')
                .change(_ => this.populateHiddenFields());
            jq('#autoProvOwner')
                .change(_ => this.populateHiddenFields());
            jq('#releaseSelection')
                .change(_ => this.onReleaseSelectionChanged());
            jq('#auditPreferenceSelect')
                .change(_ => this.populateHiddenFields());
            jq('#entitlementPref')
                .change(_ => this.populateHiddenFields());
            jq('#releaseSelectionValue')
                .change(_ => this.onReleaseIdChanged());
            jq('#assessmentTypeSelect')
                .change(_ => this.onAssessmentChanged());
            jq('#entitlementSelect')
                .change(_ => this.onEntitlementChanged());
            // jq('#autoProvAttrAdd')
            //     .click(_ => this.onAddAppAttribute());
            jq('#autoProvAttrKey')
                .keypress(e => {
                    if (this.isEnterPressed(e)) this.onAddAppAttribute();
                });
            jq('#autoProvAttrValue')
                .keypress(e => {
                    if (this.isEnterPressed(e)) this.onAddAppAttribute();
                });
            jq('#autoProvAppType')
                .change(_ => this.onIsApplicationTypeChanged());
            jq('#autoProvOwnerAssignMe')
                .click(e => {
                    e.preventDefault();
                    this.onAssignMeClick();
                });
            jq('#overrideReleaseSettings')
                .change(_ => this.loadEntitlementOptions());



            await this.onReleaseSelectionChanged();
            jq('.fodp-row-screc').hide();

            this.uiLoaded = true;
        } catch (Error) {
            console.log(Error);
        }
    }

    onAssignMeClick() {
        if (this.currentSession) jq('#autoProvOwner').val(this.currentSession.userId);
    }

    async onEntitlementChanged(skipAuditPref) {
        let val = jq('#entitlementSelect').val();
        let {entitlementId, frequencyId} = parseEntitlementDropdownValue(val);

        if (skipAuditPref !== true) await this.loadAuditPrefOptions(jq('#assessmentTypeSelect').val(), frequencyId);
        this.populateHiddenFields();
    }

    async setAssessmentsAndSettings() {
        let assmt = null;
        let entl = null;
        let freq = null;
        let audit = null;

        if (this.scanSettings) {
            assmt = this.scanSettings.assessmentTypeId;
            entl = this.scanSettings.entitlementId;
            freq = this.scanSettings.entitlementFrequencyType;
            audit = this.scanSettings.auditPreferenceType;
        }
        this.populateAssessmentsDropdown();
        jq('#assessmentTypeSelect').val(assmt);
        await this.onAssessmentChanged(true);
        jq('#entitlementSelect').val(freq && entl ? getEntitlementDropdownValue(entl, freq) : '');
        await this.onEntitlementChanged(false);
        jq('#auditPreferenceSelect').val(audit);
    }

    populateAssessmentsDropdown() {
        let atsel = jq(`#assessmentTypeSelect`);

        atsel.find('option').remove();
        jq(`#entitlementSelect`).find('option').remove();

        if (this.assessments) {
            for (let k of Object.keys(this.assessments)) {
                let at = this.assessments[k];

                atsel.append(`<option value="${at.id}">${at.name}</option>`);
            }
        }
    }

    async onReleaseSelectionChanged() {
        debugger;
        let rs = jq('#releaseSelection').val();

        jq('.fodp-row-relid').hide();
        jq('.fodp-row-relid-ovr').hide();
        jq('.fodp-row-bsi').hide();
        jq('.fodp-row-autoProv').hide();
        jq('#releaseLookup').hide();

        jq('#releaseSelectionValue').show();
        this.autoProvMode = false;

        if (rs === '1') {
            jq('#overrideReleaseSettings').prop('checked', false);
            jq('.fodp-row-bsi').show();
        } else if (rs === '2') {
            this.hideMessages();
            if (!await this.retrieveCurrentSession()) {
                this.showMessage('Failed to retrieve auth data');
                return;
            }

            if (this.currentSession && this.currentSession.userId) jq('#autoProvOwnerAssignMe').show();
            else jq('#autoProvOwnerAssignMe').hide();

            jq('#overrideReleaseSettings').prop('checked', false);
            jq('#releaseSelectionValue').hide();
            this.autoProvMode = true;
            jq('.fodp-row-autoProv').show();

        } else {
            jq('.fodp-row-relid').show();
            jq('#releaseLookup').show();
        }

        this.loadEntitlementOptions();
    }

    onReleaseIdChanged() {
        debugger;
        if (this.overrideServerSettings) {
            this.releaseId = numberOrNull(jq('#releaseSelectionValue').val());

            if (this.releaseId < 1) this.releaseId = null;

            this.loadReleaseEntitlementSettings();
        } else {
            this.onAssessmentChanged();
            this.hideMessages();
            jq(fodpOverrideRowsSelector).hide();
        }
    }

    async loadReleaseEntitlementSettings() {
        debugger;
        let rows = jq(fodpOverrideRowsSelector);

        this.hideMessages();

        if (!this.releaseId) {
            rows.hide();
            this.showMessage('Enter release id', true);
            return;
        }

        let fields = jq('.fodp-field.spinner-container');

        fields.addClass('spinner');
        rows.show();
        // ToDo: deal with overlapping calls
        let ssp = this.api.getReleaseEntitlementSettings(this.releaseId, getAuthInfo(), true)
            .then(r => this.scanSettings = r);
        let entp = this.api.getAssessmentTypeEntitlements(this.releaseId, getAuthInfo())
            .then(r => this.assessments = r);
        let tzs = this.api.getTimeZoneStacks(getAuthInfo())
            .then(r => this.timeZones = r).catch(
                (err) => {
                    console.error("timezone api failed: " + err)
                    throw err;
                }
            );

        let networkAuthTypes = this.api.getNetworkAuthType(getAuthInfo()).then(
            r => this.networkAuthTypes = r
        ).catch((err) => {
            console.error(err);
            throw err;
        });
        if (this.releaseId > 0)
            await Promise.all([ssp, entp, tzs, networkAuthTypes])
                .then(async () => {
                    debugger;
                    this.scanTypeUserControlVisibility('allTypes', false);
                    if (this.scanSettings && this.assessments) {
                        await this.setAssessmentsAndSettings();
                        //Set Entitlement
                        this.setSelectedEntitlementValue(entp);
                        jq('#entitlementFreqType').val(this.scanSettings.entitlementFrequencyType);
                        //Set timezone
                        let timeZoneId = this.scanSettings.timeZone;
                        jq('#timeZoneStackSelectList').val(timeZoneId);
                        this.onLoadTimeZone();
                        /*'set the scan type based on the scan setting get response'*/
                        this.setScanType();
                        this.onScanTypeChanged();
                        //Set scan policy from the response.
                        this.setScanPolicy();
                        //Set the Website assessment scan type specific settings.
                        if (!Object.is(this.scanSettings.websiteAssessment, undefined)) {
                            jq('#dast-standard-site-url').find('input').val(this.scanSettings.websiteAssessment.urls[0]);
                        }
                        this.setWorkflowDrivenScanSetting();
                        /*Set restrict scan value from response to UI */
                        this.setRestrictScan();

                        /*Set network settings from response. */
                        jq('#ddlNetworkAuthType').val(networkAuthTypes);
                        this.onNetworkAuthTypeLoad();
                        this.setNetworkSettings();
                        //Set the PatchUploadManifest File's fileId from get response.
                        this.setPatchUploadFileId();
                        //Enable scan Type right after assessment Type drop populated.
                        this.scanSettingsVisibility(true);
                        this.scanTypeVisibility(true);

                    } else {
                        await this.onAssessmentChanged(false);
                        this.showMessage('Failed to retrieve scan settings from API', true);
                        rows.hide();
                    }
                })
                .catch((reason) => {
                    console.error("error in scan settings: " + reason);
                });
        else {
            this.onAssessmentChanged();
            this.showMessage('Failed to retrieve scan settings from API', true);
            rows.hide();
        }
        fields.removeClass('spinner');
    }

    setNetworkSettings() {

        if (!Object.is(this.scanSettings.networkAuthenticationSettings, null)
            && !Object.is(this.scanSettings.networkAuthenticationSettings, undefined)) {
            jq('#networkUsernameRow').find('input').val(this.scanSettings.networkAuthenticationSettings.userName);
            jq('#networkPasswordRow').find('input').val(this.scanSettings.networkAuthenticationSettings.password);
            jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').trigger('click');
            let np = jq('#networkPasswordRow').find('input');
            np.attr('type', 'password');
        }
    }

    scanTypeVisibility(isVisible) {
        if ((isVisible === undefined || null) || isVisible === false) {

            jq('.dast-scan-type').each((iterator, element) => {
                let currentElement = jq(element);
                let tr = closestRow(currentElement);
                tr.addClass('dast-scan-type');
            });
            let ddlScanType = jq('.dast-scan-type');
            ddlScanType.hide();
        } else {
            jq('.dast-scan-type').show();
        }
    }

    setPatchUploadFileId() {

        if (!Object.is(this.scanSettings.loginMacroFileId, null) &&
            this.scanSettings.loginMacroFileId !== undefined && this.scanSettings.loginMacroFileId > 0) {
            jq('#loginMacroId').val(this.scanSettings.loginMacroFileId);
        }
        if (!Object.is(this.scanSettings.workflowdrivenAssessment, null)
            && this.scanSettings.workflowdrivenAssessment !== undefined) {

            if (this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro.fileId > 0)
                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro.fileId);
        }
    }

    setSelectedEntitlementValue(entitlements) {

        let currValSelected = false;
        let curVal = getEntitlementDropdownValue(this.scanSettings.entitlementId, this.scanSettings.entitlementFrequencyType);
        let entitlement = jq('#entitlementSelect');
        for (let ts of Object.keys(entitlements)) {
            let at = this.entp[ts];
            if (curVal !== undefined && at.value !== undefined && curVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                entitlement.append(`<option value="${at.text}" selected>${at.text}</option>`);
            } else {
                entitlement.append(`<option value="${at.text}">${at.text}</option>`);
            }
        }
    }

    setScanType() {


        if (this.scanSettings !== undefined && this.scanSettings !== null) {
            let selectedScanType;
            if (this.scanSettings.websiteAssessment !== null && this.scanSettings.websiteAssessment !== undefined) {
                selectedScanType = dastScanTypes.find(v => v.value === "Standard");
            } else if (this.scanSettings.workflowdrivenAssessment !== null && this.scanSettings.workflowdrivenAssessment !== undefined) {
                selectedScanType = dastScanTypes.find(v => v.value === "Workflow-driven")
            }
            // Check for API Type

            //Set other scan type values in the dropdown.
            let scanSel = jq('#scanTypeList');
            let currValSelected = false;
            scanSel.find('option').not(':first').remove();
            scanSel.find('option').first().prop('selected', true);

            for (let s of Object.keys(dastScanTypes)) {
                let at = dastScanTypes[s];
                if (selectedScanType !== undefined
                    && at.text !== undefined && (selectedScanType.value.toLowerCase() === at.text.toLowerCase())) {
                    currValSelected = true;
                    scanSel.append(`<option value="${at.value}" selected>${at.text}</option>`);
                } else {
                    scanSel.append(`<option value="${at.value}">${at.text}</option>`);
                }
            }
        }

    }

    setWorkflowDrivenScanSetting() {
        //only single file upload is allowed from FOD. Todo Iterate the array
        if (!Object.is(this.scanSettings.workflowdrivenAssessment, undefined)) {
            if (!Object.is(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro, undefined)) {
                jq('#lisWorkflowDrivenAllowedHostUrl').empty();
                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].fileId);

                this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].allowedHosts.forEach((item, index, arr) => {
                        console.log(item);
                        let ident = arr[index];
                        jq('#lisWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox' id=' " + ident + " ' name='" + ident + "'>" + arr[index] + "</li>")
                    }
                )
            }
        }
    }

    scanTypeUserControlVisibility(scanType, isVisible) {

        if ((isVisible !== undefined || null)) {
            this.commonScopeSettingVisibility(false);
            this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-standard-scan-policy")
            switch (scanType) {

                case "Standard": {
                    this.websiteScanSettingsVisibility(isVisible);
                    this.workflowScanSettingVisibility(false);
                    this.networkAuthSettingVisibility(true);
                    this.commonScopeSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(isVisible);
                    this.loginMacroSettingsVisibility(isVisible);

                    break;
                }
                case "API": {
                    this.apiScanSettingVisibility(isVisible);
                    break;
                }
                case "Workflow-driven":
                    this.workflowScanSettingVisibility(isVisible);
                    this.websiteScanSettingsVisibility(false);
                    this.commonScopeSettingVisibility(isVisible);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(false);

                    break;
                default:
                    //hide all scan type settings.
                    this.websiteScanSettingsVisibility(false);
                    this.apiScanSettingVisibility(false);
                    this.workflowScanSettingVisibility(false);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(false);
                    break;
            }
        }
    }

    resetAuthSettings() {
        this.resetNetworkSettings();
        this.resetLoginMacroSettings();

    }

    resetLoginMacroSettings() {
        jq('#loginMacroId').val(undefined);
        jq('#webSiteLoginMacroEnabled').find('input:checkbox:first').trigger('click');
    }

    loginMacroSettingsVisibility(isVisible) {
        let loginMacroSetting = jq('.' + loginAuthSetting);
        if ((isVisible === undefined || null) || isVisible === false) {
            loginMacroSetting.hide();
        } else {
            loginMacroSetting.show();
        }
    }

    networkAuthSettingVisibility(isVisible) {
        let networkAuth = jq('.' + nwAuthSetting);
        if ((isVisible === undefined) || isVisible === false) {
            networkAuth.hide();
            //reset the value here so on changing the scan type the hidden n/w values don't retain the values.
            this.resetNetworkSettings();
        } else {
            networkAuth.show();
        }
    }

    websiteScanSettingsVisibility(isVisible) {

        jq('.' + dastWebSiteSetting).each((iterator, element) => {
            let currentElement = jq(element);
            let tr = closestRow(currentElement);
            tr.addClass(dastWebSiteSetting);
        });

        let standardScanSettingRows = jq('.' + dastWebSiteSetting);
        if ((isVisible === undefined) || isVisible === false) {
            standardScanSettingRows.hide();
        } else {
            standardScanSettingRows.show();
        }
    }

    commonScopeSettingVisibility(isVisible) {
        let commonScopeRows = jq('.' + dastCommonScopeSetting);
        if ((isVisible === undefined) || isVisible === false) {
            commonScopeRows.hide();
        } else {
            commonScopeRows.show();
        }
    }


    setDefaultValuesForSelectBasedOnScanType(scanType, selectControl) {
        switch (scanType) {
            case "Standard":
                jq('#' + selectControl).val(dastScanPolicyDefaultValues.WebSiteScan.ScanPolicy);
                break;
            case "Workflow-driven":
                jq('#' + selectControl).val(dastScanPolicyDefaultValues.WorkflowDrivenScan.ScanPolicy);
                break;
        }

    }

    apiScanSettingVisibility(isVisible) {
        jq('.dast-api-scan').each((iterator, element) => {
            let currentElement = jq(element);
            let tr = closestRow(currentElement);
            tr.addClass(fodApiScanTypeClassIdr);
        });
        let apiScanSettingRows = jq('.dast-api-scan');
        if ((isVisible === undefined || null) || isVisible === false) {

            apiScanSettingRows.hide();
        } else {
            apiScanSettingRows.show();
        }
    }

    workflowScanSettingVisibility(isVisible) {
        jq('.' + dastWorkFlowSetting).each((iterator, element) => {
            let currentElement = jq(element);
            let tr = closestRow(currentElement);
            tr.addClass(dastWorkFlowSetting);
        });

        let workflowScanSettingRows = jq('.' + dastWorkFlowSetting);
        if ((isVisible === undefined || null) || isVisible === false) {
            workflowScanSettingRows.hide();
        } else {
            workflowScanSettingRows.show();
        }
    }

    scanSettingsVisibility(isVisible) {
        if ((isVisible === undefined || null) || isVisible === false) {
            let scanSettingsRows = jq('.' + dastWebSiteSetting);
            scanSettingsRows.hide();
        } else {
            jq('.dast-scan-setting').show();
        }
    }

    resetNetworkSettings() {

        jq('#networkUsernameRow').find('input').val(undefined);
        jq('#networkPasswordRow').find('input').val(undefined);
        jq('#ddlNetworkAuthType').prop('selectedIndex', 0);
        jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').trigger('click');
    }

    onLoadTimeZone() {

        let tsSel = jq('#timeZoneStackSelectList');
        let currVal = this.scanSettings.timeZone;
        let currValSelected = false;
        tsSel.find('option').not(':first').remove();
        tsSel.find('option').first().prop('selected', true);
        for (let ts of Object.keys(this.timeZones)) {
            let at = this.timeZones[ts];
            if (currVal !== undefined && at.value !== undefined && currVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                tsSel.append(`<option value="${at.value}" selected>${at.text}</option>`);
            } else {
                tsSel.append(`<option value="${at.value}">${at.text}</option>`);
            }
        }
    }

    onNetworkAuthTypeLoad() {
        let networkAuthTypeSel = jq('#ddlNetworkAuthType');
        let currVal;
        if (this.scanSettings.networkAuthenticationSettings !== undefined &&
            this.scanSettings.networkAuthenticationSettings.networkAuthenticationType !== null) {
            currVal = this.scanSettings.networkAuthenticationSettings.networkAuthenticationType;
        }

        let currValSelected = false;
        networkAuthTypeSel.find('option').not(':first').remove();
        networkAuthTypeSel.find('option').first().prop('selected', true);

        for (let ts of Object.keys(this.networkAuthTypes)) {
            let at = this.networkAuthTypes[ts];
            if (currVal !== undefined && at.value !== undefined && currVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                networkAuthTypeSel.append(`<option value="${at.text}" selected>${at.text}</option>`);
            } else {
                networkAuthTypeSel.append(`<option value="${at.text}">${at.text}</option>`);
            }
        }
    }

    onScanTypeChanged() {
        debugger;

        this.resetAuthSettings();
        let selectedScanTypeValue = jq('#scanTypeList').val();

        if (selectedScanTypeValue === null || undefined) {
            //Reset All ScanTypes Controls
            this.scanTypeUserControlVisibility('allTypes', false);
        } else {

            this.scanTypeUserControlVisibility(selectedScanTypeValue, true);
        }
    }

    setRestrictScan() {
        if (this.scanSettings !== null && this.scanSettings.restrictToDirectoryAndSubdirectories !== null) {
            {
                jq('#restrictScan').prop('checked', this.scanSettings.restrictToDirectoryAndSubdirectories);
            }
        }
    }


    setScanPolicy() {
        debugger;
        if (this.scanSettings !== undefined && this.scanSettings.policy !== null || undefined) {
            let selectedScanPolicyType = this.scanSettings.policy;
            let ScanPolicy = ["Standard", "Critical and high", "Passive", "API Scan"]
            let scanPolicySel = jq('#dast-standard-scan-policy').find('select');
            let currValSelected = false;
            scanPolicySel.find('option').not(':first').remove();
            scanPolicySel.find('option').first().prop('selected', true);

            for (let p of ScanPolicy) {

                if (selectedScanPolicyType !== undefined && selectedScanPolicyType.toLowerCase() === p.toLowerCase()) {
                    currValSelected = true;
                    scanPolicySel.append(`<option value="${p}" selected>${p}</option>`);
                } else {
                    scanPolicySel.append(`<option value="${p}">${p}</option>`);
                }
            }
        }
    }

    onLoginMacroFileUpload() {
        jq('#webSiteLoginMacro').val(true);
        let loginMacroFile = document.getElementById('loginFileMacro').files[0];
        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), loginMacroFile, dastManifestLoginFileUpload).then(res => {

                console.log("File upload success " + res);
                let response = res;
                jq('#loginMacroId').val(res)
            }
        ).catch((err) => {
                console.log(err);
            }
        );
    }

    onWorkflowMacroFileUpload() {

        let workFlowMacroFile = document.getElementById('workflowMacroFile').files[0];

        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), workFlowMacroFile, dastManifestWorkflowMacroFileUpload).then(res => {

                //Todo: - check
                console.log("File upload success " + res);
                if (res.fileId > 0) {
                    jq('#workflowMacroId').val(res.fileId)
                } else {
                    throw new Exception("Illegal argument exception,FileId not valid");
                }
                if (!Object.is(res.hosts, undefined) && !Object.is(res.hosts, null)) {
                    let hosts = undefined;
                    res.hosts.forEach(hostIterator);

                    function hostIterator(item, index, arr) {
                        if (arr !== undefined) {
                            if (hosts !== undefined)
                                hosts = hosts + "," + arr[index];
                            else
                                hosts = arr[index];
                        }
                    }

                    jq('#workflowMacroHosts').val(hosts);
                    jq('#lisWorkflowDrivenAllowedHostUrl').empty();

                    //set the allowed hosts  html list value
                    if (hosts !== undefined) {
                        let host = hosts.split(',');
                        host.forEach((item) => {
                            jq('#lisWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox'>" + item + "</li>")
                        })
                    }
                } else
                    throw Error("Invalid hosts info");
            }
        ).catch((err) => {
                console.log('err' + err);
            }
        );
    }

    directoryAndSubdirectoriesScopeVisibility(isVisible) {
        if (isVisible)
            jq('#dast-standard-scan-scope').show();
        else
            jq('#dast-standard-scan-scope').hide();
    }

    onWorkflowDrivenHostChecked(event) {

        let allowedHost = jq('#workflowMacroHosts').val();
        if (event.target.checked) {
            let hostToAdd = event.target.name; //name point to the host returned from FOD Patch API

            if (allowedHost !== undefined || null) {
                if (allowedHost.length > 0) {
                    allowedHost = allowedHost + "," + hostToAdd;
                } else
                    allowedHost = hostToAdd;
                jq('#workflowMacroHosts').val(allowedHost);
            }
        } else {
            if (allowedHost !== undefined || null) {
                let hosts = allowedHost.split(',');
                hosts.forEach((entry) => {
                    if (entry === event.target.name) {
                        let index = hosts.lastIndexOf(entry);
                        //remove
                        if (index > -1) {
                            hosts.splice(index, 1);
                        }
                    }
                });
                jq('#workflowMacroHosts').val(hosts.flat());
            }
        }
    }

    onExcludeUrlBtnClick(event, args) {
        //  alert(jq('#standardScanExcludedUrlText').val())
        let excludedUrl = jq('#standardScanExcludedUrlText').val();
        //Add to exclude list
        // jq('#listStandardScanTypeExcludedUrl');
        // jq('#listStandardScanTypeExcludedUrl').append("<li>" +  excludedUrl + "</li>");
        // jq('#listStandardScanTypeExcludedUrl').show();
    }

    async onAssessmentChanged(skipAuditPref) {
        let atval = jq('#assessmentTypeSelect').val();
        let entsel = jq('#entitlementSelect');
        let at = this.assessments ? this.assessments[atval] : null;

        entsel.find('option,optgroup').remove();

        if (at) {
            let available = at.entitlementsSorted.filter(e => e.id > 0);

            for (let e of available) {
                entsel.append(`<option value="${getEntitlementDropdownValue(e.id, e.frequencyId)}">${e.description}</option>`);
            }
        }

        await this.onEntitlementChanged(skipAuditPref);
    }

    async onAuthChanged() {
        if (!this.uiLoaded) await this.init();
        else {
            this.currentSession = null;
            await this.onReleaseSelectionChanged();
        }
        this.populateHiddenFields();
    }

    getHiddenFieldSelectValue(sel) {
        let v = numberOrNull(jq(sel).val());

        return v && v > 0 ? v : '';
    }

    async loadEntitlementOptions() {
        debugger;
        if (this.autoProvMode) {
            this.hideMessages();

            let appName = jq('#autoProvAppName').val();
            let relName = jq('#autoProvRelName').val();

            if (isNullOrEmpty(appName) || isNullOrEmpty(relName)) {
                this.showMessage('Enter Application and Release names', true);
                this.fodOverrideRowsVisibility(false);
            } else if (await this.loadAutoProvEntitlementSettings(appName, relName)) {
                this.fodOverrideRowsVisibility(true);
            } else {
                this.fodOverrideRowsVisibility(false);
            }
        } else {
            this.overrideServerSettings = jq('#overrideReleaseSettings').prop('checked');
            this.fodOverrideRowsVisibility(true);
            this.onReleaseIdChanged();
        }
        this.populateHiddenFields();
    }

    async loadAutoProvEntitlementSettings(appName, relName, isMicro, microName) {
        let fields = jq('.fodp-field.spinner-container');

        fields.addClass('spinner');

        let assessments = await this.api.getAssessmentTypeEntitlementsForAutoProv(appName, relName, isMicro, microName, getAuthInfo());
        let fail = () => {
            fields.removeClass('spinner');
            this.onAssessmentChanged();
            this.showMessage('Failed to retrieve available entitlements from API', true);
            return false;
        };

        if (assessments == null) return fail();

        this.assessments = assessments.assessments;

        if (this.assessments) {
            this.scanSettings = assessments.settings;
            this.releaseId = assessments.releaseId;
        } else {
            this.scanSettings = null;
            this.releaseId = null;
        }

        await this.setAssessmentsAndSettings();

        fields.removeClass('spinner');

        if (this.assessments) return true;
        else return fail();
    }

    fodOverrideRowsVisibility(isVisible) {
        jq(fodpOverrideRowsSelector)
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.addClass(fodpOverrideRowsSelector);
                let vtr = getValidationErrRow(tr);
                if (vtr) vtr.addClass('fode-field-row-verr');
            });
        if (isVisible) {

            jq(fodpOverrideRowsSelector).show();
        } else
            jq(fodpOverrideRowsSelector).hide();
    }

    populateHiddenFields() {

        // Auth
        let un = '';
        let pat = '';
        let tid = '';

        // Release Selection
        let relId = '';
        let bsi = '';
        let entPref = '';
        let relVal = jq('#releaseSelectionValue').val();
        let relSel = jq('#releaseSelection').val();

        // Entitlement Options
        let entId = '';
        let freqId = '';
        let at = '';
        let ap = '';
        let ts = '';
        let ll = '';
        let son = '';

        // Auto Provision
        let app = '';
        let bcrit = '';
        let appT = '';
        let attr = '';
        let ismic = '';
        let mic = '';
        let rel = '';
        let sdlc = '';
        let own = '';

        //DAST Scan Settings
        let spo='';
        let sty ='';
        let tz =''
        let scanUrl ='';
        let scanExUrl =[];
        let wkMacroFileId ='';
        let loginMacroFileId =''
        let pgRed =false;
        let sEvn ='';
        let tmBox ='';


        if (this.overrideAuth) {
            un = jq('#usernameField').val();
            pat = jq('#patField').val();
            tid = jq('#tenantIdField').val();
        }

        if (relSel === '1') {
            bsi = relVal;
            entPref = jq('#entitlementPref').val();
        } else if (relSel === '2') {
            app = jq('#autoProvAppName').val();
            bcrit = jq('#autoProvBussCrit').val();
            appT = jq('#autoProvAppType').val();

            for (let k of Object.keys(this.appAttributes)) {
                let a = this.appAttributes[k];

                if (a.length > 0) {
                    if (attr) attr += appAttributeKeyDelimiter;
                    attr += k + appAttributeKeyValueDelimiter + a;
                }
            }
            if (jq('#autoProvAppType').val() != 2) {
                ismic = jq('#autoProvIsMicro').prop('checked');
                if (ismic) mic = jq('#autoProvMicroName').val();
            }
            rel = jq('#autoProvRelName').val();
            sdlc = jq('#autoProvSdlc').val();
            own = numberOrNull(jq('#autoProvOwner').val());
            let {entitlementId, frequencyId} = parseEntitlementDropdownValue(jq('#entitlementSelect').val());
            at = jq('#assessmentTypeSelect').val();
            entId = entitlementId;
            freqId = frequencyId;
            ap = this.getHiddenFieldSelectValue('#auditPreferenceSelect');

        } else if (this.overrideServerSettings) {
            relId = relVal;

            let entVal = jq('#entitlementSelect').val();
            let {entitlementId, frequencyId} = parseEntitlementDropdownValue(entVal);
            entId = entitlementId;
            freqId = frequencyId;
            let atVal = jq('#assessmentTypeSelect').val();
            at = this.assessments && this.assessments[atVal] ? atVal : '';
            ap = this.getHiddenFieldSelectValue('#auditPreferenceSelect');

        } else relId = relVal;

        // Auth
        jq('#username').val(un);
        jq('#personalAccessToken').val(pat);
        jq('#tenantId').val(tid);

        // Release Selection
        jq('#releaseId').val(relId);
        jq('#entitlementPreference').val(entPref);

        // Entitlement Options
        jq('#entitlementId').val(entId);
        jq('#frequencyId').val(freqId);
        jq('#assessmentType').val(at);
        jq('#auditPreference').val(ap);

        // Auto Provision
        jq('#applicationName').val(app);
        jq('#businessCriticality').val(bcrit);
        jq('#applicationType').val(appT);
        jq('#attributes').val(attr);
        jq('#releaseName').val(rel);
        jq('#sdlcStatus').val(sdlc);
        jq('#owner').val(own);

        //scan settings
    }
}

const dynamicPipelineGenerator = new DynamicPipelineGenerator();
spinAndWait(() => jq('#releaseSelection').val() !== undefined)
    .then(dynamicPipelineGenerator.preinit.bind(dynamicPipelineGenerator));
