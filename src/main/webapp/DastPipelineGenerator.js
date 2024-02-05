const fodpRowSelector = '.fodp-field-row, .fodp-field-row-verr';
const fodpOverrideRowsSelector = '.fodp-row-relid-ovr';
const appAttributeKeyDelimiter = ';';
const appAttributeKeyValueDelimiter = ':';
const dastScanSetting = 'dast-scan-setting';
const dastWebSiteSettingIdentifier = 'dast-standard-setting';
const dastWorkFlowSettingIdentifier = 'dast-workflow-setting';
const dastCommonScopeSettingIdentifier = 'dast-common-scan-scope';
const nwAuthSetting = 'dast-networkAuth-setting';
const loginAuthSetting = 'dast-login-macro';
const requestFalsePos = closestRow('#requestFalsePositiveRemovalRow');
const loginMacroCreation = closestRow('#loginMacroFileCreationRow');
const dastApiSetting = 'dast-api-setting';
const dastApiScanTypeSpecificControls = 'dast-api-specific-controls';
const pipelineOverRide ='fode-field spinner-container fodp-row-autoProv'
const dastScanPolicyDefaultValues = Object.freeze(
    {
        "WebSiteScan": {"ScanPolicy": "standard"},
        "WorkflowDrivenScan": {"ScanPolicy": "standard"},
    });
const dastAllowedHostRow = closestRow('#allowedHostRow');
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
const apiSettingRowEntry = closestRow('#dast-api-scan-block');

const DastScanTypeEnum = Object.freeze({
    Standard: 'Standard',
    WorkflowDriven: 'Workflow-driven',
    Api: 'API',
});
const DastApiScanTypeEnum = Object.freeze({
    OpenApi: 'openApi',
    Postman:'postman',
    gRPC:'grpc',
    GraphQl:'graphQl'
});
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

    async preInit() {

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
        requestFalsePos.addClass(dastScanSetting);
        loginMacroCreation.addClass(dastScanSetting);
        dastScanTyperow.addClass(dastScanSetting);
        dastStandardScope.addClass(dastScanSetting);
        dastEnv.addClass(dastScanSetting);
        dastTimeZone.addClass(dastScanSetting);
        networkAuth.addClass(nwAuthSetting);
        loginMacro.addClass(loginAuthSetting);

        <!--Scan Specific Scope Sections-->
        dastWrkFlowMacroUpload.addClass(dastWorkFlowSettingIdentifier);
        dastWrkFlowAllowedHost.addClass(dastWorkFlowSettingIdentifier);
        dastExcludeUrl.addClass(dastWebSiteSettingIdentifier);
        dastSiteUrlRow.addClass(dastWebSiteSettingIdentifier);
        apiSettingRowEntry.addClass(dastApiSetting);
        dastWebSiteTimeBoxScan.addClass(dastWebSiteSettingIdentifier);
        commonWebScopeSetting.addClass(dastCommonScopeSettingIdentifier);
        commonWebScopeSettingAttr.addClass(dastCommonScopeSettingIdentifier);
        commonScanPolicy.addClass(dastCommonScopeSettingIdentifier);
        commonScanPolicy.addClass('fodp-row-relid-ovr');
        dastAllowedHostRow.addClass(dastWorkFlowSettingIdentifier);
        <!--Scope sections-->

        try {
            this.hideMessages();
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
            jq('#listStandardScanTypeExcludedUrl').click(_ => this.onExcludeUrlChecked(event));
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

        this.populateAssessmentsDropdown();
        let assmt = null;
        let entl = null;
        let freq = null;
        if (this.scanSettings && this.scanSettings.assessmentTypeId !== null && this.scanSettings.assessmentTypeId > 0) {

            assmt = this.scanSettings.assessmentTypeId;
            entl = this.scanSettings.entitlementId;
            freq = this.scanSettings.entitlementFrequencyType;
            jq('#assessmentTypeSelect').val(assmt);
            await this.onAssessmentChanged(true);
            jq('#entitlementSelect').val(freq && entl ? getEntitlementDropdownValue(entl, freq) : '');
        }
    }

    populateAssessmentsDropdown() {
        let atsel = jq(`#assessmentTypeSelect`);
        atsel.find('option').remove();
        jq(`#entitlementSelect`).find('option').remove();
        if (this.assessments) {
            for (let k of Object.keys(this.assessments)) {
                let at = this.assessments[k];
                if (at !== null) {
                    if (at.assessmentCategory === 'DAST_Automated') {

                        if (at.id) {
                            atsel.append(`<option value="${at.id}">${at.name}</option>`);
                        } else {
                            atsel.append(`<option value="${at.assessmentTypeId}">${at.name}</option>`);
                        }
                        break;
                    }
                }
            }
        }
    }

    async onReleaseSelectionChanged() {

        let rs = jq('#releaseSelection').val();

        jq('.fodp-row-relid').hide();
        jq('.fodp-row-relid-ovr').hide();
        jq('.fodp-row-autoProv').hide();
        jq('#releaseSelectionValue').show();
        this.autoProvMode = false;
        jq('#dast-api-scan-policy-apiType').hide();
        if (rs === '1') {
            jq('#overrideReleaseSettings').prop('checked', false);

        } else if (rs === '2') {
            this.hideMessages();
            if (!await this.retrieveCurrentSession()) {
                this.showMessage('Failed to retrieve auth data');
                return;
            }
            if (this.currentSession && this.currentSession.userId) {
                jq('#autoProvOwnerAssignMe').show();
                validateTextbox('#autoProvOwner');
            }
            else jq('#autoProvOwnerAssignMe').hide();
            jq('#overrideReleaseSettings').prop('checked', false);
            jq('#releaseSelectionValue').hide();
            this.autoProvMode = true;
            validateRequiredFieldsById(requiredFieldsPipelineById)
            jq('.fodp-row-autoProv').show();
        } else {
            jq('.fodp-row-relid').show();
            jq('#releaseLookup').show();
        }
        await this.loadEntitlementOptions();
    }

    onReleaseIdChanged() {
        this.releaseId = numberOrNull(jq('#releaseSelectionValue').val());
        if(!this.releaseId) {
            let rows = jq(fodpOverrideRowsSelector);
            rows.hide();
            this.showMessage('Enter release id', true);
             return;
        }
        if (this.overrideServerSettings) {
            if (this.releaseId < 1) {
                this.releaseId = null;
                let rows = jq(fodpOverrideRowsSelector);
                rows.hide();
                this.showMessage('Enter release id', true);

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

    async loadReleaseEntitlementSettings() {
        let rows = jq(fodpOverrideRowsSelector);
        this.hideMessages();
        if (!this.releaseId) {
            rows.hide();
            this.showMessage('Enter release id', true);
            handleSpinner('#releaseSelectioForm', false);
            return;
        }
        jq('#dastScanDetails').hide();
        rows.show();
        this.scanTypeUserControlVisibility('allTypes', false);
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
                        this.setLoginMacroCreationDetails();
                        this.setFalsePositiveFlagRequest();
                        //Set the PatchUploadManifest File's fileId from get response.
                        this.setPatchUploadFileId();
                        //Enable scan Type right after assessment Type drop populated.
                        this.scanTypeVisibility(true);
                        validateRequiredFields(requiredFieldsPipeline);
                        this.onScanTypeChanged();
                    } else {
                        await this.onAssessmentChanged(false);
                        this.showMessage('Failed to retrieve scan settings from API', true);
                        rows.hide();
                    }
                    this.populateHiddenFields();
                })
                .catch((reason) => {
                    console.error("error in scan settings: " + reason);
                });
        else {
            this.showMessage('Failed to retrieve scan settings from API', true);
            rows.hide();
        }
        handleSpinner('#releaseSelectioForm', true);
        jq('#dastScanDetails').show();

    }

    setNetworkSettings() {
        if (this.scanSettings.networkAuthenticationSettings) {
            jq('#networkUsernameRow').find('input').val(this.scanSettings.networkAuthenticationSettings.userName);
            jq('#networkPasswordRow').find('input')
                .val(this.scanSettings.networkAuthenticationSettings.password);
            if (jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').prop('checked') === false &&
            this.scanSettings.networkAuthenticationSettings.userName) {
                jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').trigger('click');
               }
            let np = jq('#networkPasswordRow').find('input');
            np.attr('type', 'password');
        }
    }

    scanTypeVisibility(isVisible) {
        if (!isVisible) {
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
        if (this.scanSettings && this.scanSettings.loginMacroFileId &&
            this.scanSettings.loginMacroFileId > 0) {
            jq('#loginMacroId').val(this.scanSettings.loginMacroFileId);
        }
        if (this.scanSettings &&
            this.scanSettings.workflowdrivenAssessment &&
            this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro) {
            if (this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro.fileId > 0)
                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro.fileId);
        }
    }

    setSelectedEntitlementValue(entitlements) {
        let currValSelected = false;
        let curVal =
            getEntitlementDropdownValue(this.scanSettings.entitlementId,
                this.scanSettings.entitlementFrequencyType);
        let entitlement = jq('#entitlementSelect');
        for (let ts of Object.keys(entitlements)) {
            let at = this.entp[ts];
            if (curVal  && at.value && curVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                entitlement.append(`<option value="${at.text}" selected>${at.text}</option>`);
            } else {
                entitlement.append(`<option value="${at.text}">${at.text}</option>`);
            }
        }
    }

    setEntitlementForAutoProv(assessmentEntitlement) {
        let entitlement = jq('#entitlementSelect');
        for (let ts of Object.keys(assessmentEntitlement)) {
            let at = assessmentEntitlement[ts];

            let entlVal = at.entitlementDescription.split('(');
            if (entlVal.length > 0) {
                entitlement.append(`<option value="${entlVal[0]}">${at.entitlementDescription}</option>`);
            } else {
                throw new Error("Invalid Entitlement Freq from AutoProv")
            }
        }
    }

    setScanType() {
        if (this.scanSettings) {
            let selectedScanType;
            if (this.scanSettings.websiteAssessment) {
                selectedScanType = DastScanTypeEnum.Standard;
            } else if (this.scanSettings.workflowdrivenAssessment) {
                selectedScanType = DastScanTypeEnum.WorkflowDriven;
            } else if (this.scanSettings.apiAssessment) {
                selectedScanType = DastScanTypeEnum.Api;
            }
            let scanSel = jq('#scanTypeList');
            let currValSelected = false;
            scanSel.find('option').not(':first').remove();
            scanSel.find('option').first().prop('selected', true);
            for (let s of Object.keys(DastScanTypeEnum)) {
                let at = DastScanTypeEnum[s];
                if (selectedScanType
                    && at && (selectedScanType.toLowerCase() === at.toLowerCase())) {
                    currValSelected = true;
                    scanSel.append(`<option value="${at}" selected>${at}</option>`);
                } else {
                    scanSel.append(`<option value="${at}">${at}</option>`);
                }
            }
        }
    }

    setWebSiteScanSetting() {
        if (!Object.is(this.scanSettings.websiteAssessment, undefined)) {
            jq('#dast-standard-site-url').find('input').val(this.scanSettings.websiteAssessment.dynamicSiteUrl);
            jq('#loginMacroFileId').val(this.scanSettings.loginMacroFileId);
        }
    }

    setWorkflowDrivenScanSetting() {
        //only single file upload is allowed from FOD. Todo Iterate the array
        if (!Object.is(this.scanSettings.workflowdrivenAssessment, undefined)) {
            if (!Object.is(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro, undefined)) {

                jq('#listWorkflowDrivenAllowedHostUrl').empty();
                jq('#workflowMacroHosts').val(undefined);
                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].fileId);
                this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].allowedHosts.forEach((item, index, arr) => {
                        console.log(item);
                        if (arr[index]) {
                            jq('#listWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox' id=' " + arr[index] +
                                " ' checked='checked' name='" + arr[index] + "'>" + arr[index] + "</li>");
                            if (!jq('#workflowMacroHosts').val()) {
                                jq('#workflowMacroHosts').val(arr[index]);
                            } else {
                                let host = jq('#workflowMacroHosts').val();
                                if (host) {
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
        if (isVisible) {
            this.commonScopeSettingVisibility(false);
            this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-standard-scan-policy");
            switch (scanType) {
                case DastScanTypeEnum.Standard: {
                    jq('#dast-standard-scan-policy').show();
                    jq('#dast-api-scan-policy-apiType').hide();
                    jq('#requestFalsePositiveRemovalRow').show();
                    jq('#loginMacroFileCreationRow').show();
                    this.websiteScanSettingsVisibility(isVisible);
                    this.workflowScanSettingVisibility(false);
                    this.apiScanSettingVisibility(false);
                    this.networkAuthSettingVisibility(isVisible);
                    this.commonScopeSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(isVisible);
                    this.loginMacroSettingsVisibility(isVisible);
                    this.timeboxScanVisibility(isVisible);
                    break;
                }
                case DastScanTypeEnum.Api: {
                    jq('#dast-standard-scan-policy').hide();
                    jq('#dast-api-scan-policy-apiType').show();
                    jq('.redundantPageDetection').hide();
                    jq('#requestFalsePositiveRemovalRow').show();
                    jq('#loginMacroFileCreationRow').hide();
                    this.commonScopeSettingVisibility(false);
                    this.networkAuthSettingVisibility(true);
                    this.loginMacroSettingsVisibility(false);
                    this.workflowScanSettingVisibility(false);
                    this.websiteScanSettingsVisibility(false);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.timeboxScanVisibility(isVisible);
                    this.apiScanSettingVisibility(isVisible);
                    break;
                }
                case DastScanTypeEnum.WorkflowDriven:
                    this.workflowScanSettingVisibility(isVisible);
                    jq('#dast-standard-scan-policy').show();
                    jq('#dast-api-scan-policy-apiType').hide();
                    jq('.redundantPageDetection').hide();
                    jq('#requestFalsePositiveRemovalRow').show();
                    jq('#loginMacroFileCreationRow').hide();
                    this.apiScanSettingVisibility(false);
                    this.commonScopeSettingVisibility(isVisible);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.timeboxScanVisibility(false);
                    this.websiteScanSettingsVisibility(false);
                    break;
                case "allTypes":
                default:
                    //hide all scan type settings.
                    this.apiScanSettingVisibility(false);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(false);
                    this.commonScopeSettingVisibility(false);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.loginMacroSettingsVisibility(false);
                    this.timeboxScanVisibility(false);
                    this.websiteScanSettingsVisibility(false);
                    this.workflowScanSettingVisibility(false)
                    jq('#dast-api-scan-policy-apiType').hide();
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
        if (!isVisible) {
            loginMacroSetting.hide();
            tr.hide();
        } else {
            loginMacroSetting.show();
            tr.show();
        }
    }

    excludeUrlVisibility(isVisible) {
        if (!isVisible) {
            jq('#standardScanTypeExcludeUrlsRow').hide();
        } else {

            jq('#standardScanTypeExcludeUrlsRow').show();
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
        if (!isVisible) {
            networkAuth.hide();
            //reset the value here so on changing the scan type the hidden n/w values don't retain the values.
            this.resetNetworkSettings();
        } else {
            networkAuth.show();
        }
    }

    websiteScanSettingsVisibility(isVisible) {
       if (!isVisible) {

            this.resetWebSiteSettingsValues();
            jq('.'+ dastWebSiteSettingIdentifier).hide();
            this.excludeUrlVisibility(false);
        } else {
            this.excludeUrlVisibility(true);
           jq('.'+ dastWebSiteSettingIdentifier).show();
        }
    }

    resetWorkFlowSettingSValues() {
        jq('[name=workflowMacroFilePath]').val('');
        jq('[name=workflowMacroHosts]').val('');
        jq('[name=workflowMacroId]').val('');
        jq('#listWorkflowDrivenAllowedHostUrl').empty();
    }

    resetWebSiteSettingsValues() {
        jq('[name=webSiteUrl]').val(null);
        jq('[name=scanTimeBox]').val(null); //timebox not supported for workflow
    }


    commonScopeSettingVisibility(isVisible) {
        let commonScopeRows = jq('.scopeContainer');
        if (!isVisible) {
            commonScopeRows.hide();
        } else {
            commonScopeRows.show();
        }
    }

    setDefaultValuesForSelectBasedOnScanType(scanType, selectControl) {
        switch (scanType) {
            case DastScanTypeEnum.Standard:
                jq('#' + selectControl).val(dastScanPolicyDefaultValues.WebSiteScan.ScanPolicy);
                break;
            case DastScanTypeEnum.WorkflowDriven:
                jq('#' + selectControl).val(dastScanPolicyDefaultValues.WorkflowDrivenScan.ScanPolicy);
                break;
        }
    }

    apiScanSettingVisibility(isVisible) {
               let apiScanSettingRows = jq('.' + dastApiSetting);
               jq('.' + dastApiScanTypeSpecificControls).hide();
               if (!isVisible) {
                   apiScanSettingRows.hide();
               } else {
                   apiScanSettingRows.show();
                   validateDropdown('#apiTypeList');
               }
           }
    

    workflowScanSettingVisibility(isVisible) {
        let workflowScanSettingRows = jq('.dast-workflow-setting');
        if (!isVisible) {
            workflowScanSettingRows.hide();
            jq('#dast-workflow-macro-upload').hide();
            this.resetWorkFlowSettingSValues();
        } else {
            workflowScanSettingRows.show();
            jq('#dast-workflow-macro-upload').show();

        }
    }

    scanSettingsVisibility(isVisible) {
        if (!isVisible) {
            jq('.dast-standard-setting').hide();
        } else {
            jq('.dast-standard-setting').show();
        }
    }

    resetNetworkSettings() {
        jq('#networkUsernameRow').find('input').val(undefined);
        jq('#networkPasswordRow').find('input').val(undefined);
        jq('#ddlNetworkAuthType').prop('selectedIndex', 0);

        let ctl = jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first');
        if(ctl.prop('checked') === true)
            ctl.trigger('click');
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
        this.resetAuthSettings();
        jq('.dast-common-scan-scope').show();
        let selectedScanTypeValue = jq('#scanTypeList').val();
        if (!selectedScanTypeValue) {
            //Reset All ScanTypes Controls
            this.scanTypeUserControlVisibility('allTypes', false);
        } else {
            this.scanTypeUserControlVisibility(selectedScanTypeValue, true);
            validateDropdown('#scanTypeList');
        }
    }

    setRestrictScan() {
        if (this.scanSettings && this.scanSettings.restrictToDirectoryAndSubdirectories) {
            {
                jq('#restrictScan').prop('checked', this.scanSettings.restrictToDirectoryAndSubdirectories);
            }
        }
    }

    setScanPolicy() {
        if (this.scanSettings && this.scanSettings.policy) {
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
            if (allowedHost) {
                if (allowedHost.length > 0) {
                    allowedHost = allowedHost + "," + hostToAdd;
                } else
                    allowedHost = hostToAdd;
                jq('#workflowMacroHosts').val(allowedHost);
            }
        } else {
            if (allowedHost) {
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
        let excludedUrl = jq('#standardScanExcludedUrlText').val();
        if (excludedUrl) {
            jq('#listStandardScanTypeExcludedUrl').append("<li> <input type='checkbox' id=' " + excludedUrl +
                " ' checked='checked' name='" + excludedUrl + "'>" + excludedUrl + "</li>");
            jq('#listStandardScanTypeExcludedUrl').show();
            let urlsList = jq('#excludedUrls').val();
            if (urlsList) {
                if (urlsList !== '' && excludedUrl !== '') {
                    urlsList = urlsList + "," + excludedUrl;
                    jq('#excludedUrls').val(urlsList);
                } else
                    jq('#excludedUrls').val(excludedUrl);
            }
            else
                jq('#excludedUrls').val(excludedUrl);

        }
    }

    onExcludeUrlChecked(event) {

        let excludedUrls = jq('#excludedUrls').val();
        if (event.target.checked) {
            let urlToAdd = event.target.name;
            if (excludedUrls) {
                if (excludedUrls.le > 0) {
                    excludedUrls = excludedUrls + "," + urlToAdd;
                } else
                    excludedUrls = urlToAdd;
                jq('#excludedUrls').val(excludedUrls);
            }
        } else {
            if (excludedUrls) {
                let urls = excludedUrls.split(',');
                urls.forEach((entry) => {
                    if (entry === event.target.name) {
                        let index = urls.lastIndexOf(entry);
                        if (index > -1) {
                            urls.splice(index, 1);

                            let ulList = document.querySelectorAll('#listStandardScanTypeExcludedUrl');
                            if (ulList) {
                                ulList.forEach((item) => {
                                    item.childNodes.forEach((ctrl) => {
                                        if (ctrl.textContent.trim() === entry.trim()) {
                                            ctrl.remove();
                                        }
                                    });
                                });
                            }
                        }
                    }
                });
                jq('#excludedUrls').val(urls.flat());
            }
        }
    }
    setTimeBoxScan() {
        if (this.scanSettings && this.scanSettings.timeBoxInHours) {
            jq('#dast-timeBox-scan').find('input:text:first').val(this.scanSettings.timeBoxInHours);
            if (jq('#dast-timeBox-scan').find('input:checkbox:first').prop('checked') === false) {
                jq('#dast-timeBox-scan').find('input:checkbox:first').trigger('click');
            }
        }
    }

    async onAssessmentChanged(skipAuditPref) {
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

        if (this.autoProvMode) {
            this.hideMessages();
            let appName = jq('#autoProvAppName').val();
            let relName = jq('#autoProvRelName').val();

            if (isNullOrEmpty(appName) || isNullOrEmpty(relName)) {
                this.showMessage('Enter Application and Release names', true);
                jq(fodpOverrideRowsSelector).hide();
            } else {
                await this.loadAutoProvEntitlementSettings(appName, relName)
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

        let assessments = this.api.getAssessmentTypeEntitlementsForAutoProv(appName, relName, false, "", getAuthInfo())
            .then(r => this.assessments = r);
        let tzs = this.api.getTimeZoneStacks(getAuthInfo())
            .then(r => this.timeZones = r);
        let networkAuthTypes = this.api.getNetworkAuthType(getAuthInfo()).then(
            r => this.networkAuthTypes = r
        );

        await Promise.all([assessments, tzs, networkAuthTypes])
            .then(async () => {

                let fail = () => {
                    fields.removeClass('spinner');
                    this.showMessage('Failed to retrieve available entitlements from API', true);
                    return false;
                };
                if (this.assessments == null) {
                    return fail();
                } else {
                    let fields = jq('.fodp-field.spinner-container');
                    fields.addClass('spinner');

                    await this.setAssessmentsAndSettings().then(
                        () => {
                            let entitlementsOfDastAutomatedAsstCategory = this.assessments.findAll(e => e.assessmentCategory === 'DAST_Automated');
                            //Set Entitlement
                            this.setEntitlementForAutoProv(entitlementsOfDastAutomatedAsstCategory);

                            let timeZoneSel = jq('#timeZoneStackSelectList');
                            for (let ts of Object.keys(this.timeZones)) {
                                let at = this.timeZones[ts];
                                timeZoneSel.append(`<option value="${at.value}">${at.text}</option>`);
                            }
                            let networkAuthTypeSel = jq('#ddlNetworkAuthType');
                            for (let ts of Object.keys(this.networkAuthTypes)) {
                                let nt = this.networkAuthTypes[ts];
                                networkAuthTypeSel.append(`<option value="${nt.text}" >${nt.text}</option>`);
                            }
                            jq('.fodp-row-autoProv').show();
                            this.apiTypeUserControlVisibility(null, false);
                            this.scanTypeUserControlVisibility('allTypes', true);
                            this.onTimeZoneChanged()
                            this.onNetworkAuthTypeChanged();
                            validateRequiredFields(requiredFieldsPipeline);
                            validateDropdown('#scanTypeList');
                            fields.removeClass('spinner');
                        }
                    );

                }
            });

    }

    fodOverrideRowsVisibility(isVisible) {
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
   setLoginMacroCreationDetails(){
           if(this.scanSettings && this.scanSettings.websiteAssessment && this.scanSettings.websiteAssessment.loginMacroFileCreationDetails){
               jq('#loginMacroPrimaryUsernameRow').find('input').val(this.scanSettings.websiteAssessment.loginMacroFileCreationDetails.primaryUsername);
               jq('#loginMacroPrimaryPasswordRow').find('input')
                   .val(this.scanSettings.websiteAssessment.loginMacroFileCreationDetails.primaryPassword);
               jq('#loginMacroSecondaryUsernameRow').find('input').val(this.scanSettings.websiteAssessment.loginMacroFileCreationDetails.secondaryUsername);
               jq('#loginMacroSecondaryPasswordRow').find('input')
                   .val(this.scanSettings.websiteAssessment.loginMacroFileCreationDetails.secondaryPassword);
               if (jq('#loginMacroFileCreationRow').find('input:checkbox:first').prop('checked') === false) {
                   jq('#loginMacroFileCreationRow').find('input:checkbox:first').trigger('click');
               }
               let np = jq('#networkPasswordRow').find('input');
               np.attr('type', 'password');
           }
           if(this.scanSettings && this.scanSettings.hasUtilizedAdditionalServices){
           jq('#loginMacroSecondaryPasswordRow').find('input').prop('disabled','disabled');
           jq('#loginMacroPrimaryUsernameRow').find('input').prop('disabled','disabled');
           jq('#loginMacroSecondaryUsernameRow').find('input').prop('disabled','disabled');
           jq('#loginMacroPrimaryPasswordRow').find('input').prop('disabled','disabled');
           jq('#loginMacroFileCreationRow').find('input:checkbox:first').prop('disabled','disabled');
           }
           else{
           jq('#loginMacroSecondaryPasswordRow').find('input').prop('disabled',false);
                   jq('#loginMacroPrimaryUsernameRow').find('input').prop('disabled',false);
                   jq('#loginMacroSecondaryUsernameRow').find('input').prop('disabled',false);
                   jq('#loginMacroPrimaryPasswordRow').find('input').prop('disabled',false);
                   jq('#loginMacroFileCreationRow').find('input:checkbox:first').prop('disabled',false);
           }
       }

       setFalsePositiveFlagRequest(){
           if(this.scanSettings && this.scanSettings.requestFalsePositiveRemoval){
               if (jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').prop('checked') === false) {
                   jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').trigger('click');
               }
           }
           else{
            if (jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').prop('checked') === true) {
                   jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').trigger('click');
                }
           }
           if(this.scanSettings && this.scanSettings.hasUtilizedAdditionalServices){
           jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').prop('disabled','disabled')
           }
           else{
           jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').prop('disabled',false)
           }
       }

    setApiScanSetting() {
        if (this.scanSettings && this.scanSettings.apiAssessment) {
            if (this.scanSettings.apiAssessment.openAPI) {
                this.setOpenApiSettings(this.scanSettings.apiAssessment.openAPI);
            } else if (this.scanSettings.apiAssessment.graphQL) {
                this.setGraphQlSettings(this.scanSettings.apiAssessment.graphQL);
            } else if (this.scanSettings.apiAssessment.gRPC) {
                this.setGrpcSettings(this.scanSettings.apiAssessment.gRPC);
            } else if (this.scanSettings.apiAssessment.postman) {
                this.setPostmanSettings(this.scanSettings.apiAssessment.postman);
            }
        }
    }

    setOpenApiSettings(openApiSettings) {
        jq('#apiTypeList').val('openApi');
        var inputId = openApiSettings.sourceType === 'Url' ? 'openApiInputUrl' : 'openApiInputFile';
        this.onApiTypeChanged();
        jq('#' + inputId).trigger('click');
        jq('#dast-openApi-api-key input').val(openApiSettings.apiKey);
        if (openApiSettings.sourceType === 'Url') {
            jq('#dast-openApi-url input').val(openApiSettings.sourceUrn);
        }
        else if(openApiSettings.sourceType === 'FileId') {
           if (this.scanSettings && this.scanSettings.fileDetails) {
               this.scanSettings.fileDetails.forEach((item, index, arr) => {
                   jq('.openApiFileDetails').text(item.fileName);
                   jq('#openApiFileId').val(item.fileId);
                   jq('.uploadedFileContainer').show();
               });
           }
        }
    }

    setGraphQlSettings(graphQlSettings) {
        jq('#apiTypeList').val('graphQl');
        var inputId = graphQlSettings.sourceType === 'Url' ? 'graphQlInputUrl' : 'graphQlInputFile';
        this.onApiTypeChanged();
        jq('#' + inputId).trigger('click');
        jq('#dast-graphQL-api-host input').val(graphQlSettings.host);
        jq('#dast-graphQL-api-servicePath input').val(graphQlSettings.servicePath);
        jq('#dast-graphQL-schemeType input').val(graphQlSettings.schemeType);
        if (graphQlSettings.sourceType === 'Url') {
            jq('#dast-graphQL-url input').val(graphQlSettings.sourceUrn);
        }
        else if(graphQlSettings.sourceType === 'FileId') {
            if (this.scanSettings && this.scanSettings.fileDetails) {
               this.scanSettings.fileDetails.forEach((item, index, arr) => {
                   jq('.graphQlFileDetails').text(item.fileName);
                   jq('#graphQLFileId').val(item.fileId);
                   jq('.uploadedFileContainer').show();
               });
           }
        }
    }

    setGrpcSettings(grpcSettings) {
        jq('#apiTypeList').val('grpc');
        this.onApiTypeChanged();
            if (this.scanSettings && this.scanSettings.fileDetails) {
               this.scanSettings.fileDetails.forEach((item, index, arr) => {
                   jq('.grpcFileDetails').text(item.fileName);
                   jq('#grpcFileId').val(item.fileId);
                   jq('.uploadedFileContainer').show();
               });
           }
        jq('#dast-grpc-api-host input').val(grpcSettings.host);
        jq('#dast-grpc-api-servicePath input').val(grpcSettings.servicePath);
        jq('#dast-grpc-schemeType input').val(grpcSettings.schemeType);
    }

    setPostmanSettings(postmanSettings) {
        jq('#apiTypeList').val('postman');
        this.onApiTypeChanged();
        if (this.scanSettings && this.scanSettings.fileDetails) {
           this.scanSettings.fileDetails.forEach((item, index, arr) => {
               jq('.postmanFileDetails').text(item.fileName);
               jq('#openApiFileId').val(item.fileId);
               jq('.uploadedFileContainer').show();
           });
       }
    }

    apiTypeUserControlVisibility(apiType, isVisible) {
        if (isVisible) {
            this.openApiScanVisibility(false);
            this.grpcScanVisibility(false);
            this.graphQlScanVisibility(false);
            this.postmanScanVisibility(false);
            switch (apiType) {
                case DastApiScanTypeEnum.OpenApi:
                    this.openApiScanVisibility(isVisible);
                    break;
                case DastApiScanTypeEnum.GraphQl:
                    this.graphQlScanVisibility(isVisible);
                    break;
                case DastApiScanTypeEnum.gRPC:
                    this.grpcScanVisibility(isVisible);
                    break;
                case DastApiScanTypeEnum.Postman:
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
        else{
            jq('#dast-openApi').closest('.tr').hide();

        jq('#dast-postman').closest('.tr').hide();
        jq('#dast-graphQL').closest('.tr').hide();
        jq('#dast-grpc').closest('.tr').hide();
        }
    }

    graphQlScanVisibility(isVisible) {
        if (isVisible)
            jq('#dast-graphQL').closest('.tr').show();
        else{
            jq('#dast-graphQL').closest('.tr').hide();

        jq('#dast-openApi').closest('.tr').hide()
        jq('#dast-postman').closest('.tr').hide();
        jq('#dast-grpc').closest('.tr').hide();
        }
    }

    grpcScanVisibility(isVisible) {
        if (isVisible)
            jq('#dast-grpc').closest('.tr').show();
        else{
        jq('#dast-grpc').closest('.tr').hide();
        jq('#dast-openApi').closest('.tr').hide()
        jq('#dast-postman').closest('.tr').hide();
        jq('#dast-graphQL').closest('.tr').hide();
        }
    }

    postmanScanVisibility(isVisible) {
        if (isVisible){
            jq('#dast-postman').closest('.tr').show();
            jq('.postmanFilePath').show();
            }
        else{
            jq('#dast-postman').closest('.tr').hide();
        jq('#dast-openApi').closest('.tr').hide()
        jq('#dast-graphQL').closest('.tr').hide();
        jq('#dast-grpc').closest('.tr').hide();
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
                throw new Error("Illegal argument exception,File Type not valid");
        }

        if (!file|| !fileType ||!elem ) {
            throw new Error("Illegal argument exception,File Type not valid");
        }

        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), file, fileType).then(res => {

                //Todo: - check
                console.log("File upload success " + res);

                if (res.fileId > 0) {
                    elem.val(res.fileId);
                    displayMessage.text('Uploaded Successfully !!');
                } else {
                    displayMessage.text('Upload Failed !!');
                    throw new Error("Illegal argument exception,FileId not valid");
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

        if (!selectedApiTypeValue) {
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
            jq('#dast-openApi-api-key').show();
            jq('#openApiRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'openApiInputUrl') {
            jq('.openApiSourceControls').show()
            jq('#dast-openApi-api-key').show();
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
            let {entitlementId, frequencyType} = parseEntitlementDropdownValue(jq('#entitlementSelect').val());
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
    .then(dynamicPipelineGenerator.preInit.bind(dynamicPipelineGenerator));
