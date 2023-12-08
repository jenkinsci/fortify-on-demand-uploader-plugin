const fodpRowSelector = '.fodp-field-row, .fodp-field-row-verr';
const fodpOverrideRowsSelector = '.fodp-row-relid-ovr';
const appAttributeKeyDelimiter = ';';
const appAttributeKeyValueDelimiter = ':';
const dastScanTypes = [{"value": 'Standard', "text": 'Standard'}, {
    "value": 'Workflow-driven',
    "text": 'Workflow-driven'
}, {"value": 'API', "text": 'API'}];
const dastScanSetting = 'dast-scan-setting';
const dastWebSiteSetting = 'dast-standard-setting';
const dastWorkFlowSetting = 'dast-workflow-setting';
const dastCommonScopeSetting = 'dast-common-scan-scope';
const nwAuthSetting = 'dast-networkAuth-setting';
const loginAuthSetting = 'dast-login-macro'

const dastApiSpecificControls = 'dast-api-specific-controls';

const dastScanPolicyDefaultValues =
    {
        "WebSiteScan": {"ScanPolicy": "standard"},
        "WorkflowDrivenScan": {"ScanPolicy": "standard"},
        "ApiScan": {"ScanPolicy": "API Scan"}
    }
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

class DastPipelineGenerator {
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

    async loadAuditPrefOptions(assessmentType, frequencyType) {
        let apSel = jq('#auditPreferenceSelect');
        let curVal = apSel.val();
        let apCont = jq('#auditPreferenceForm');
        let setSpinner = apCont.hasClass('spinner') === false;
        if (setSpinner) apCont.addClass('spinner');
        apSel.find('option').remove();
        assessmentType = numberOrNull(assessmentType);
        if (!this.autoProvMode && this.releaseId && assessmentType && frequencyType) {
            try {
                let prefs = await this.api.getAuditPreferences(this.releaseId, assessmentType, frequencyType, getAuthInfo());

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

            jq('#btnAddExcludeUrl').click(_ => this.onExcludeUrlBtnClick());

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

            jq('#apiTypeList').change(_ => this.onApiTypeChanged());

            jq('#openApiInputFile, #openApiInputUrl, #graphQlInputFile, #graphQlInputUrl').change(_ => this.onSourceChange(event.target.id));

            jq('#btnUploadPostmanFile, #btnUploadOpenApiFile, #btnUploadgraphQLFile').click(_ => this.onFileUpload(event));

            jq('#listWorkflowDrivenAllowedHostUrl').click(_ => this.onWorkflowDrivenHostChecked(event));

            await this.onReleaseSelectionChanged();

            jq('.fodp-row-screc').hide();

            jq('#timeZoneStackSelectList').change(_ => this.onTimeZoneChanged());

            jq('#ddlNetworkAuthType').change(_ => this.onNetworkAuthTypeChanged());

            setOnblurEventForPipeline();

            //  jq('#workflowMacroFilePath').blur(_ => this.onWorkflowFlowMacroFilePathChange());

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
        let {frequencyType} = parseEntitlementDropdownValue(val);
        this.populateHiddenFields();
    }

    async setAssessmentsAndSettings() {
        let assmt = null;
        let entl = null;
        let freq = null;
        debugger;
        if (this.scanSettings) {
            assmt = this.scanSettings.assessmentTypeId;
            entl = this.scanSettings.entitlementId;
            freq = this.scanSettings.entitlementFrequencyType;

        }
        this.populateAssessmentsDropdown();
        jq('#assessmentTypeSelect').val(assmt);
        await this.onAssessmentChanged(true);
        jq('#entitlementSelect').val(freq && entl ? getEntitlementDropdownValue(entl, freq) : '');
    }

    populateAssessmentsDropdown() {
        let atsel = jq(`#assessmentTypeSelect`);

        atsel.find('option').remove();
        jq(`#entitlementSelect`).find('option').remove();

        if (this.assessments) {
            for (let k of Object.keys(this.assessments)) {
                let at = this.assessments[k];
                if (at !== null){
                if(at.assessmentCategory == 'DAST_Automated'){
                    atsel.append(`<option value="${at.id}">${at.name}</option>`);
                    }
            }
            }
        }
    }

    async onReleaseSelectionChanged() {
        debugger;
        let rs = jq('#releaseSelection').val();

        jq('.fodp-row-relid').hide();
        jq('.fodp-row-relid-ovr').hide();
        jq('.fodp-row-autoProv').hide();
        jq('#releaseSelectionValue').show();
        this.autoProvMode = false;

        if (rs === '1') {
            jq('#overrideReleaseSettings').prop('checked', false);

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
        this.releaseId = numberOrNull(jq('#releaseSelectionValue').val());
        if (this.overrideServerSettings) {
            if (this.releaseId < 1) {
                this.releaseId = null;
                return;
            } else {
                this.loadReleaseEntitlementSettings().then(
                    () => {
                        this.fodOverrideRowsVisibility(true);
                    }
                );
            }
        } else {
            if ((this.releaseId !== null) && this.releaseId > 1) {
                this.loadReleaseEntitlementSettings().then(
                    () => {
                        this.hideMessages();
                        this.fodOverrideRowsVisibility(false);
                    }
                );

            } else {
                this.hideMessages();
                this.fodOverrideRowsVisibility(false);
            }
        }

    }

    //     debugger;
    //     this.releaseId = numberOrNull(jq('#releaseSelectionValue').val());
    //     if (this.overrideServerSettings) {
    //         if (this.releaseId < 1) {
    //             this.releaseId = null
    //         } else
    //             this.loadReleaseEntitlementSettings().then(() => {
    //                     this.fodOverrideRowsVisibility(true);
    //                 }
    //             );
    //     } else {
    //         this.loadReleaseEntitlementSettings().then(
    //             () => {
    //                 this.hideMessages();
    //                // this.fodOverrideRowsVisibility(false);
    //             }
    //         );
    //     }
    // }

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
        handleSpinner('#releaseSelectioForm',false);
        jq('#dastScanDetails').hide();
        //fields.addClass('spinner');
        rows.show();
        // ToDo: deal with overlapping calls
        let ssp = this.api.getReleaseEntitlementSettings(this.releaseId, getAuthInfo(), true)
            .then(r => this.scanSettings = r);
        let entp = this.api.getAssessmentTypeEntitlements(this.releaseId, getAuthInfo())
            .then(r => this.assessments = r);
        let tzs = this.api.getTimeZoneStacks(getAuthInfo())
            .then(r => this.timeZones = r);
        let networkAuthTypes = this.api.getNetworkAuthType(getAuthInfo()).then(
            r => this.networkAuthTypes = r
        );
        if (this.releaseId > 0)
            await Promise.all([ssp, entp, tzs, networkAuthTypes])
                .then(async () => {
                    debugger;
                    this.scanTypeUserControlVisibility('allTypes', false);
                    if (this.scanSettings && this.assessments) {
                        await this.setAssessmentsAndSettings();
                        //Set Entitlement
                        this.setSelectedEntitlementValue(entp);
                        jq('#entitlementFrequency').val(this.scanSettings.entitlementFrequencyType);
                        //Set timezone
                        let timeZoneId = this.scanSettings.timeZone;
                        jq('#timeZoneStackSelectList').val(timeZoneId);
                        this.onLoadTimeZone();
                        this.onTimeZoneChanged();
                        /*'set the scan type based on the scan setting get response'*/
                        this.setScanType();
                        this.onScanTypeChanged();
                        //Set scan policy from the response.
                        this.setScanPolicy();
                        //Set the Website assessment scan type specific settings.
                        this.setWebSiteScanSetting();
                        //set timebox scan
                        this.setTimeBoxScan();
                        this.setWorkflowDrivenScanSetting();
                        this.setApiScanSetting();
                        /*Set restrict scan value from response to UI */
                        this.setRestrictScan();
                        /*Set network settings from response. */
                        jq('#ddlNetworkAuthType').val(networkAuthTypes);
                        this.onNetworkAuthTypeLoad();
                        this.onNetworkAuthTypeChanged();
                        this.setNetworkSettings();
                        //Set the PatchUploadManifest File's fileId from get response.
                        this.setPatchUploadFileId();
                        //Enable scan Type right after assessment Type drop populated.
                        this.scanSettingsVisibility(true);
                        this.scanTypeVisibility(true);

                        validateRequiredFields(requiredFieldsPipeline);

                    } else {
                        await this.onAssessmentChanged(false);
                        this.showMessage('Failed to retrieve scan settings from API', true);
                        rows.hide();
                    }
                    debugger;
                    this.populateHiddenFields();
                })
                .catch((reason) => {
                    console.error("error in scan settings: " + reason);
                });
        else {
            this.showMessage('Failed to retrieve scan settings from API', true);
            rows.hide();
        }
        handleSpinner('#releaseSelectioForm',true);
        jq('#dastScanDetails').show();
        //fields.removeClass('spinner');
    }

    setNetworkSettings() {
        if (!Object.is(this.scanSettings.networkAuthenticationSettings, null)
            && !Object.is(this.scanSettings.networkAuthenticationSettings, undefined)) {
            jq('#networkUsernameRow').find('input').val(this.scanSettings.networkAuthenticationSettings.userName);
            jq('#networkPasswordRow').find('input').val(this.scanSettings.networkAuthenticationSettings.password);
            debugger;
            if (jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').prop('checked') === false) {
                jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').trigger('click');
            }
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
                alert('found');
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
            } else if (this.scanSettings.apiAssessment !== null && this.scanSettings.apiAssessment !== undefined) {
                selectedScanType = dastScanTypes.find(v => v.value === "API");
            }

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

    setWebSiteScanSetting() {
        debugger;
        if (!Object.is(this.scanSettings.websiteAssessment, undefined)) {
            jq('#dast-standard-site-url').find('input').val(this.scanSettings.websiteAssessment.dynamicSiteUrl);
            jq('#loginMacroFileId').val(this.scanSettings.loginMacroFileId);

        }


    }

    setWorkflowDrivenScanSetting() {

        debugger;
        //only single file upload is allowed from FOD. Todo Iterate the array
        if (!Object.is(this.scanSettings.workflowdrivenAssessment, undefined)) {
            if (!Object.is(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro, undefined)) {
                debugger;
                jq('#listWorkflowDrivenAllowedHostUrl').empty();
                jq('#workflowMacroHosts').val(undefined);

                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].fileId);

                this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].allowedHosts.forEach((item, index, arr) => {

                        console.log(item);
                        if (arr[index] !== undefined || null) {

                            jq('#listWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox' id=' " + arr[index] +
                                " ' checked='checked' name='" + arr[index] + "'>" + arr[index] + "</li>");

                            if (jq('#workflowMacroHosts').val() === null || undefined) {
                                jq('#workflowMacroHosts').val(arr[index]);
                            } else {
                                debugger;
                                let host = jq('#workflowMacroHosts').val();
                                if (host !== null || undefined) {

                                    if (host !== '')
                                        host = host + "," + arr[index];
                                    else
                                        host = arr[index];

                                    jq('#workflowMacroHosts').val(host);

                                }
                            }
                        }

                    }
                );

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
                    this.apiScanSettingVisibility(false);
                    this.networkAuthSettingVisibility(isVisible);
                    this.commonScopeSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(isVisible);
                    this.loginMacroSettingsVisibility(isVisible);
                    this.timeboxScanVisibility(isVisible);
                    this.excludeUrlVisibility(isVisible);
                    break;
                }
                case "API": {
                    jq('#dast-standard-scan-policy').hide();
                    jq('#dast-api-scan-policy-apiType').show();
                    jq('.redundantPageDetection').hide();
                    this.apiScanSettingVisibility(isVisible);
                    this.commonScopeSettingVisibility(isVisible);
                    this.networkAuthSettingVisibility(true);
                    this.loginMacroSettingsVisibility(false);
                    this.workflowScanSettingVisibility(false);
                    this.websiteScanSettingsVisibility(false);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.timeboxScanVisibility(isVisible);
                    break;
                }
                case "Workflow-driven":
                    this.workflowScanSettingVisibility(isVisible);
                    this.websiteScanSettingsVisibility(false);
                    this.apiScanSettingVisibility(false);
                    this.commonScopeSettingVisibility(isVisible);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.timeboxScanVisibility(false);
                    this.excludeUrlVisibility(false);
                    break;

                case "allTypes":
                default:
                    //hide all scan type settings.
                    this.websiteScanSettingsVisibility(false);
                    this.workflowScanSettingVisibility(false)
                    this.apiScanSettingVisibility(false);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(false);
                    this.commonScopeSettingVisibility(false);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.loginMacroSettingsVisibility(false);
                    this.timeboxScanVisibility(false);
                    this.excludeUrlVisibility(false);
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
        let loginMacroSetting = jq('#login-macro-row');
        let tr = closestRow('#login-macro-row');
        if ((isVisible === undefined || null) || isVisible === false) {
            loginMacroSetting.hide();
            tr.hide();
        } else {
            loginMacroSetting.show();
            tr.show();
        }
    }

    excludeUrlVisibility(isVisible) {
        if ((isVisible === undefined) || isVisible === false) {
            jq('#standardScanTypeExcludeUrlsRow').hide();
            let tr = closestRow('#standardScanTypeExcludeUrlsRow');
            tr.hide();

        } else {
            jq('#standardScanTypeExcludeUrlsRow').show();
            let tr = closestRow('#standardScanTypeExcludeUrlsRow');
            tr.show();
        }
    }

    timeboxScanVisibility(isVisible) {
        if ((isVisible === undefined) || isVisible === false) {
            jq('#dast-timeBox-scan').hide();
        } else {
            jq('#dast-timeBox-scan').show();
        }
    }

    networkAuthSettingVisibility(isVisible) {
        let networkAuth = jq('.dast-networkAuth-setting');
        if ((isVisible === undefined) || isVisible === false) {
            networkAuth.hide();
            //reset the value here so on changing the scan type the hidden n/w values don't retain the values.
            this.resetNetworkSettings();
        } else {
            networkAuth.show();
        }
    }

    websiteScanSettingsVisibility(isVisible) {
        let standardScanSettingRows = jq('.dast-standard-setting');
        if ((isVisible === undefined) || isVisible === false) {
            standardScanSettingRows.hide();
            this.excludeUrlVisibility(false);
            this.resetWebSiteSettingsValues();
        } else {
            this.excludeUrlVisibility(true);
            standardScanSettingRows.show();
        }
    }

    resetWorkFlowSettingSValues() {
        jq('[name=workflowMacroFilePath]').val('');
        jq('[name=workflowMacroHosts]').val('');
        jq('[name=workflowMacroId]').val('');


    }

    resetWebSiteSettingsValues() {
        jq('[name=webSiteUrl]').val(null);
        jq('[name=scanTimeBox]').val(null); //timebox not supported for workflow

    }


    commonScopeSettingVisibility(isVisible) {
        let commonScopeRows = jq('.dast-common-scan-scope');
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
        let workflowScanSettingRows = jq('.dast-workflow-setting');
        if ((isVisible === undefined || null) || isVisible === false) {
            workflowScanSettingRows.hide();
            jq('#dast-workflow-macro-upload').hide();
            this.resetWorkFlowSettingSValues();
        } else {
            workflowScanSettingRows.show();
            jq('#dast-workflow-macro-upload').show();

        }
    }

    scanSettingsVisibility(isVisible) {
        if ((isVisible === undefined || null) || isVisible === false) {
            let scanSettingsRows = jq('.dast-standard-setting');
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
            validateDropdown('#scanTypeList');
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

                    jq('#workflowMacroHost').val(hosts);
                    jq('#listWorkflowDrivenAllowedHostUrl').empty();

                    //set the allowed hosts  html list value
                    if (hosts !== undefined) {
                        let host = hosts.split(',');
                        host.forEach((item) => {
                            jq('#listWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox'>" + item + "</li>")
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
        debugger;
        let allowedHost = jq('#workflowMacroHosts').val();

        if (event.target.checked) {

            debugger;
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
                debugger;
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
        // alert(jq('#standardScanExcludedUrlText').val())
        let excludedUrl = jq('#standardScanExcludedUrlText').val();
        //Add to exclude list
        jq('#listStandardScanTypeExcludedUrl');
        // jq('#listStandardScanTypeExcludedUrl').append("<li>" + "<input type='checkbox' id= " + excludedUrl + "checked='checked'> + excludedUrl + </li>");
        jq('#listStandardScanTypeExcludedUrl').append("<li>" + excludedUrl + "</li>");
        jq('#listStandardScanTypeExcludedUrl').show();
    }

    setTimeBoxScan() {
        if (this.scanSettings.timeBoxInHours !== undefined) {
            debugger;
            jq('#dast-timeBox-scan').find('input:text:first').val(this.scanSettings.timeBoxInHours);
            if (jq('#dast-timeBox-scan').find('input:checkbox:first').prop('checked') === false) {
                jq('#dast-timeBox-scan').find('input:checkbox:first').trigger('click');
            }

        }
    }

    onExcludeUrlItemSelect() {

        alert(this);
    }

    async onAssessmentChanged(skipAuditPref) {
        debugger;
        let atval = jq('#assessmentTypeSelect').val();
        let entsel = jq('#entitlementSelect');
        let at = this.assessments ? this.assessments[atval] : null;

        entsel.find('option,optgroup').remove();

        if (at) {
            let available = at.entitlementsSorted.filter(e => e.id > 0);

            for (let e of available) {
                entsel.append(`<option value="${getEntitlementDropdownValue(e.id, e.frequency)}">${e.description}</option>`);
            }

        }
        validateDropdown('#assessmentTypeSelect');
        await this.onEntitlementChanged(skipAuditPref);
        // ToDo: set to unselected if selected value doesn't exist
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
                jq(fodpOverrideRowsSelector).hide();
            } else {
                await this.loadAutoProvEntitlementSettings(appName, relName, false, "")
            }

        } else {
            this.overrideServerSettings = jq('#overrideReleaseSettings').prop('checked');
            jq(fodpOverrideRowsSelector).show();
            this.onReleaseIdChanged();
        }
        this.populateHiddenFields();
    }

    async loadAutoProvEntitlementSettings(appName, relName) {
        let fields = jq('.fodp-field.spinner-container');
        fields.addClass('spinner');
        debugger;
        let assessments = await this.api.getAssessmentTypeEntitlementsForAutoProv(appName, relName, false, "", getAuthInfo());
        let fail = () => {
            fields.removeClass('spinner');
            this.onAssessmentChanged();
            this.showMessage('Failed to retrieve available entitlements from API', true);
            return false;
        };

        if (assessments == null) return fail();
        this.assessments = assessments;
        if (this.assessments) {
            this.scanSettings = assessments.settings;
            this.releaseId = assessments.releaseId;
        } else {
            this.scanSettings = null;
            this.releaseId = null;
        }
        await this.setAssessmentsAndSettings();
        fields.removeClass('spinner');
        if (this.assessments) {
            debugger;
            this.scanTypeUserControlVisibility('allTypes', false)

            return true;
        } else return fail();
    }

    fodOverrideRowsVisibility(isVisible) {
        debugger;
        jq(fodpOverrideRowsSelector)
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);
                tr.addClass('fodp-row-relid-ovr');
                let vtr = getValidationErrRow(tr);
                if (vtr) vtr.addClass('fodp-row-relid-ovr');
            });
        if (isVisible) {
            jq(fodpOverrideRowsSelector).show();
        } else
            jq(fodpOverrideRowsSelector).hide();
    }

    setApiScanSetting() {

        if (!Object.is(this.scanSettings.apiAssessment, undefined)) {
            if (!Object.is(this.scanSettings.apiAssessment.openAPI, undefined)) {
                this.setOpenApiSettings(this.scanSettings.apiAssessment.openAPI);
            } else if (!Object.is(this.scanSettings.apiAssessment.graphQL, undefined)) {
                this.setGraphQlSettings(this.scanSettings.apiAssessment.graphQL);
            } else if (!Object.is(this.scanSettings.apiAssessment.gRPC, undefined)) {
                this.setGrpcSettings(this.scanSettings.apiAssessment.gRPC);
            } else if (!Object.is(this.scanSettings.apiAssessment.postman, undefined)) {
                this.setPostmanSettings(this.scanSettings.apiAssessment.postman);
            }

        }
    }

    setOpenApiSettings(openApiSettings) {

        jq('#apiTypeList').val('openApi');
        var inputId = openApiSettings.sourceType == 'Url' ? 'openApiInputUrl' : 'openApiInputFile';
        this.onApiTypeChanged();
        jq('#' + inputId).trigger('click');
        jq('#dast-openApi-api-key input').val(openApiSettings.apiKey);
        if (openApiSettings.sourceType == 'Url') {
            jq('#dast-openApi-url input').val(openApiSettings.sourceUrn);
        } else {
            //ToDo : Write code for showing file name
        }
    }

    setGraphQlSettings(graphQlSettings) {
        jq('#apiTypeList').val('graphQl');
        var inputId = graphQlSettings.sourceType == 'Url' ? 'graphQlInputUrl' : 'graphQlInputFile';
        this.onApiTypeChanged();
        jq('#' + inputId).trigger('click');
        jq('#dast-graphQL-api-host input').val(graphQlSettings.host);
        jq('#dast-graphQL-api-servicePath input').val(graphQlSettings.servicePath);
        jq('#dast-graphQL-schemeType input').val(graphQlSettings.schemeType);
        if (graphQlSettings.sourceType == 'Url') {
            jq('#dast-graphQL-url input').val(graphQlSettings.sourceUrn);
        } else {
            //ToDo : Write code for showing file name
        }
    }

    setGrpcSettings(grpcSettings) {
        jq('#apiTypeList').val('grpc');
        jq('#dast-grpc-api-host input').val(graphQlSettings.host);
        jq('#dast-grpc-api-servicePath input').val(graphQlSettings.servicePath);
        jq('#dast-grpc-schemeType input').val(graphQlSettings.schemeType);
    }

    setPostmanSettings(postmanSettings) {
        jq('#apiTypeList').val('postman');
    }

    apiTypeUserControlVisibility(apiType, isVisible) {

        if ((isVisible !== undefined || null)) {
            this.openApiScanVisibility(false);
            this.grpcScanVisibility(false);
            this.graphQlScanVisibility(false);
            this.postmanScanVisibility(false);
            switch (apiType) {
                case "openApi":
                    this.openApiScanVisibility(isVisible);
                    break;

                case "graphQl":
                    this.graphQlScanVisibility(isVisible);
                    break;

                case "grpc":
                    this.grpcScanVisibility(isVisible);
                    break;

                case "postman":
                    this.postmanScanVisibility(isVisible);
                    break;

                default:
                    this.openApiScanVisibility(false);
                    this.grpcScanVisibility(false);
                    this.graphQlScanVisibility(false);
                    this.postmanScanVisibility(false);
                    break;

            }
        }
    }

    openApiScanVisibility(isVisible) {
        if (isVisible)
            jq('#dast-openApi').closest('.tr').show();
        else
            jq('#dast-openApi').closest('.tr').hide();

        jq('#dast-postman').closest('.tr').hide();
        jq('#dast-graphQL').closest('.tr').hide();
        jq('#dast-grpc').closest('.tr').hide();
    }

    graphQlScanVisibility(isVisible) {
        if (isVisible)
            jq('#dast-graphQL').closest('.tr').show();
        else
            jq('#dast-graphQL').closest('.tr').hide();

        jq('#dast-openApi').closest('.tr').hide()
        jq('#dast-postman').closest('.tr').hide();
        jq('#dast-grpc').closest('.tr').hide();
    }

    grpcScanVisibility(isVisible) {
        if (isVisible)
            jq('#dast-grpc').closest('.tr').show();
        else
            jq('#dast-grpc').closest('.tr').hide();
        jq('#dast-openApi').closest('.tr').hide()
        jq('#dast-postman').closest('.tr').hide();
        jq('#dast-graphQL').closest('.tr').hide();
    }

    postmanScanVisibility(isVisible) {
        if (isVisible)
            jq('#dast-postman').closest('.tr').show();
        else
            jq('#dast-postman').closest('.tr').hide();
        jq('#dast-openApi').closest('.tr').hide()
        jq('#dast-graphQL').closest('.tr').hide();
        jq('#dast-grpc').closest('.tr').hide();
    }

    apiScanSettingVisibility(isVisible) {
        let apiScanSettingRows = jq('.dast-api-setting');
        jq('.dast-api-specific-controls').hide();
        if ((isVisible === undefined || null) || isVisible === false) {
            apiScanSettingRows.hide();

        } else {
            apiScanSettingRows.show();
            validateDropdown('#apiTypeList');
        }
    }

    onFileUpload(event) {

        jq('.uploadMessage').text('');
        let file = null;
        let fileType = null;
        let elem = null;
        let displayMessage = null;

        switch (event.target.id) {
            case 'btnUploadOpenApiFile' :
                file = document.getElementById('openApiFile').files[0];
                fileType = openApiFileType;
                elem = jq('#openApiFileId');
                displayMessage = jq('#openApiUploadMessage');
                break;

            case 'btnUploadPostmanFile' :
                file = document.getElementById('postmanFile').files[0];
                fileType = postmanFileType;
                elem = jq('#postmanFileId');
                displayMessage = jq('#postmanUploadMessage');
                break;

            case 'btnUploadgraphQLFile' :
                file = document.getElementById('graphQLFile').files[0];
                fileType = graphQlFileType;
                elem = jq('#graphQLFileId');
                displayMessage = jq('#grapgQlUploadMessage');
                break

            case 'btnUploadgrpcFile' :
                file = document.getElementById('grpcFile').files[0];
                fileType = grpcFileType;
                elem = jq('#grpcFileId');
                displayMessage = jq('#grpcUploadMessage');
                break;
            default :
                throw new Exception("Illegal argument exception,File Type not valid");
        }

        if (file == null || fileType == null || elem == null) {
            throw new Exception("Illegal argument exception,File Type not valid");
        }

        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), file, fileType).then(res => {

                //Todo: - check
                console.log("File upload success " + res);
                if (res.fileId > 0) {
                    elem.val(res.fileId);
                    displayMessage.text('Uploaded Successfully !!');
                } else {
                    displayMessage.text('Upload Failed !!');
                    throw new Exception("Illegal argument exception,FileId not valid");
                }
            }
        ).catch((err) => {
                displayMessage.text('Upload Failed !!');
                console.log('err' + err);
            }
        );
    }

    onApiTypeChanged() {

        jq('.uploadMessage').text('');
        this.onSourceChange('none');
        let selectedApiTypeValue = jq('#apiTypeList').val();

        if (selectedApiTypeValue === null || undefined) {
            //Reset All ScanTypes Controls
            this.apiTypeUserControlVisibility(null, false);
            jq('.dast-api-specific-controls').show();
        } else {

            this.apiTypeUserControlVisibility(null, false);
            this.apiTypeUserControlVisibility(selectedApiTypeValue, true);
            jq('.dast-api-specific-controls').show();
            validateDropdown('#apiTypeList');
        }
    }

    onSourceChange(id) {

        jq('.openApiSourceControls').hide();
        jq('.graphQLSourceControls').hide();
        jq('.apiOptions').show();
        jq('.sourceTypeFileds').hide();
        jq('.uploadMessage').text('');
        jq('#dast-openApi-filePath').hide();
         jq('.graphQlFilePath').hide();
        if (id === 'openApiInputFile') {
            jq('.openApiSourceControls').show()
            jq('#dast-api-openApi-upload').hide();
            jq('#dast-openApi-filePath').show();
            jq('#openApiRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'openApiInputUrl') {
            jq('.openApiSourceControls').show()
            jq('#dast-openApi-url').show();
            jq('#openApiRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'graphQlInputFile') {
         jq('.graphQlFilePath').show();
            jq('.graphQLSourceControls').show();
            jq('#dast-api-graphQL-upload').hide();
            jq('#graphQlRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'graphQlInputUrl') {
            jq('.graphQLSourceControls').show();
            jq('#dast-graphQL-url').show();
            jq('#graphQlRadioSource').val(jq('#' + event.target.id).val());
        } else {
            jq('.sourceOptions').prop('checked', false);
        }
    }

    onTimeZoneChanged() {
      validateDropdown('#timeZoneStackSelectList');
    }

    onNetworkAuthTypeChanged() {
      validateDropdown('#ddlNetworkAuthType');
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
        let freqType = '';
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
            let {entitlementId, frequencyType} = parseEntitlementDropdownValue(jq('#entitlementSelect').val());
            at = jq('#assessmentTypeSelect').val();
            entId = entitlementId;
            freqType = frequencyType;
            ap = this.getHiddenFieldSelectValue('#auditPreferenceSelect');

        } else if (this.overrideServerSettings) {
            relId = relVal;

            let entVal = jq('#entitlementSelect').val();
            let {entitlementId, frequencyType} = parseEntitlementDropdownValue(entVal);
            entId = entitlementId;
            freqType = frequencyType;
            let atVal = jq('#assessmentTypeSelect').val();
            at = this.assessments && this.assessments[atVal] ? atVal : '';
            ap = this.getHiddenFieldSelectValue('#auditPreferenceSelect');

        } else {
            relId = relVal;
            let {entitlementId, frequencyType} = parseEntitlementDropdownValue(jq('#entitlementSelect').val());
            at = jq('#assessmentTypeSelect').val();
            entId = entitlementId;
            freqType = frequencyType;
        }

        // Auth
        jq('#username').val(un);
        jq('#personalAccessToken').val(pat);
        jq('#tenantId').val(tid);

        // Release Selection
        jq('#releaseId').val(relId);
        jq('#entitlementPreference').val(entPref);

        // Entitlement Options
        jq('#entitlementId').val(entId);
        jq('#entitlementFrequency').val(freqType);
        //entitlementFrequency
        jq('#assessmentTypeId').val(at);
        jq('#auditPreference').val(ap);

        // Auto Provision
        jq('#applicationName').val(app);
        jq('#businessCriticality').val(bcrit);
        jq('#applicationType').val(appT);
        jq('#attributes').val(attr);
        jq('#releaseName').val(rel);
        jq('#sdlcStatus').val(sdlc);
        jq('#owner').val(own);

    }

}


const dynamicPipelineGenerator = new DastPipelineGenerator();
spinAndWait(() => jq('#releaseSelection').val() !== undefined)
    .then(dynamicPipelineGenerator.preinit.bind(dynamicPipelineGenerator));
