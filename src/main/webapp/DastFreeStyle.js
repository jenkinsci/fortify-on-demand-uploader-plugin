const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';
const dastManifestWorkflowMacroFileUpload = "WorkflowDrivenMacro";
const dastManifestLoginFileUpload = "LoginMacro";
const openApiFileType = 'OpenAPIDefinition';
const grpcFileType = 'GRPCDefinition';
const graphQlFileType = 'GraphQLDefinition';
const postmanFileType = 'PostmanCollection';
const dastScanSelectDefaultValues = Object.freeze(
    {
        "WebSiteScan": {"ScanPolicy": "standard"},
        "WorkflowDrivenScan": {"ScanPolicy": "standard"},
        "ApiScan": {"ScanPolicy": "API Scan"}
    });

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

class DastFreeStyle {
    constructor() {
        this.api = new Api(instance, descriptor);
        this.uiLoaded = false;
        this.releaseId = null;
        subscribeToEvent('releaseChanged', p => this.loadEntitlementSettings(p.detail));
    }

    showMessage(msg, isError) {
        let msgElem;
        if (isError) msgElem = jq('#fode-error'); else msgElem = jq('#fode-msg');
        msgElem.text(msg);
        msgElem.show();
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

    scanTypeUserControlVisibility(scanType, isVisible) {
       if (isVisible) {
            this.commonScopeSettingVisibility(false);
               switch (scanType) {
                case DastScanTypeEnum.Standard: {
                    jq('#dast-standard-scan-policy').show();
                    jq('#dast-api-scan-policy-apiType').hide();
                    jq('.redundantPageDetection').show();
                    jq('#requestFalsePositiveRemovalRow').show();
                    jq('#loginMacroFileCreationRow').show();
                    this.websiteScanSettingsVisibility(isVisible);
                    this.workflowScanSettingVisibility(false);
                    this.networkAuthSettingVisibility(true);
                    this.apiScanSettingVisibility(false);
                    this.commonScopeSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(isVisible);
                    this.loginMacroSettingsVisibility(isVisible);
                    this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-standard-scan-policy")
                    break;
                }
                case DastScanTypeEnum.Api: {
                    jq('#dast-standard-scan-policy').hide();
                    jq('#dast-api-scan-policy-apiType').show();
                    jq('.redundantPageDetection').hide();
                    jq('#requestFalsePositiveRemovalRow').hide();
                    jq('#loginMacroFileCreationRow').hide();
                    this.apiScanSettingVisibility(isVisible);
                    this.commonScopeSettingVisibility(isVisible);
                    this.networkAuthSettingVisibility(true);
                    this.loginMacroSettingsVisibility(false);
                    this.workflowScanSettingVisibility(false);
                    this.websiteScanSettingsVisibility(false);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    break;
                }
                case DastScanTypeEnum.WorkflowDriven:
                    jq('#dast-standard-scan-policy').show();
                    jq('#dast-api-scan-policy-apiType').hide();
                    jq('.redundantPageDetection').hide();
                    jq('#requestFalsePositiveRemovalRow').hide();
                    jq('#loginMacroFileCreationRow').hide();
                    this.workflowScanSettingVisibility(isVisible);
                    this.apiScanSettingVisibility(false);
                    this.websiteScanSettingsVisibility(false);
                    this.commonScopeSettingVisibility(isVisible);
                    this.loginMacroSettingsVisibility(false);
                    this.networkAuthSettingVisibility(isVisible);
                    this.directoryAndSubdirectoriesScopeVisibility(false);
                    this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-workflow-scan-policy")
                    break;
                default:
                    //hide all scan type settings.
                    this.websiteScanSettingsVisibility(false);
                    this.apiScanSettingVisibility(false);
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
        let loginMacroSetting = jq('.dast-login-macro');
        if (!isVisible) {
            loginMacroSetting.hide();
        } else {
            loginMacroSetting.show();
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
        let standardScanSettingRows = jq('.dast-standard-setting');
        if (!isVisible) {
            standardScanSettingRows.hide();
        } else {
            standardScanSettingRows.show();
        }
    }

    commonScopeSettingVisibility(isVisible) {
        let commonScopeRows = jq('.dastCommonScopeSetting');
        if (!isVisible) {
            commonScopeRows.hide();
        } else {
            commonScopeRows.show();
        }
    }


    setDefaultValuesForSelectBasedOnScanType(scanType, selectControl) {
        switch (scanType) {
            case DastScanTypeEnum.Standard:
                jq('#' + selectControl).val(dastScanSelectDefaultValues.WebSiteScan.ScanPolicy);
                break;
            case DastScanTypeEnum.WorkflowDriven:
                jq('#' + selectControl).val(dastScanSelectDefaultValues.WorkflowDrivenScan.ScanPolicy);
                break;
        }
    }

    workflowScanSettingVisibility(isVisible) {
        let workflowScanSettingRows = jq('.dast-workflow-setting');
        if (!isVisible) {
            workflowScanSettingRows.hide();
        } else {
            workflowScanSettingRows.show();
        }
    }

    scanSettingsVisibility(isVisible) {
        if (!isVisible) {
            let scanSettingsRows = jq('.dast-standard-setting');
            scanSettingsRows.hide();
        } else {
            jq('.dast-scan-setting').show();
        }
        //ToDo:
        // //Remove BSI token form Pick a release selection list as it is not supported for DAST.
        // jq("#releaseTypeSelectList option[value='UseBsiToken']").remove();
    }

    hideMessages(msg) {
        jq('#fode-error').hide();
        jq('#fode-msg').hide();
    }

    populateAssessmentsDropdown() {
        let atsel = jq(`#ddAssessmentType`);

        atsel.find('option').remove();
        jq(`#entitlementSelectList`).find('option').remove();

        for (let k of Object.keys(this.assessments)) {
            let at = this.assessments[k];
            if (at.assessmentCategory == 'DAST_Automated') {
                atsel.append(`<option value="${at.id}">${at.name}</option>`);
            }
        }
    }

    async onAssessmentChanged(skipAuditPref) {
        let atval = jq('#ddAssessmentType').val();
        let entsel = jq('#entitlementSelectList');
        let at = this.assessments ? this.assessments[atval] : null;
        entsel.find('option,optgroup').remove();
        if (at) {
            let available = at.entitlementsSorted.filter(e => e.id > 0);
            let forPurchase = at.entitlementsSorted.filter(e => e.id <= 0);
            let availableGrp = forPurchase.length > 0 ? jq(`<optgroup label="Available Entitlements"></optgroup>`) : entsel;
            for (let e of available) {
                availableGrp.append(`<option value="${getEntitlementDropdownValue(e.id, e.frequencyId, e.frequency)}">${e.description}</option>`);
            }
            if (forPurchase.length > 0) {
                let grp = jq(`<optgroup label="Available For Purchase"></optgroup>`);
                entsel.append(availableGrp);
                entsel.append(grp);
                for (let e of forPurchase) {
                    grp.append(`<option value="${getEntitlementDropdownValue(0, e.frequencyId, e.frequency)}">${e.description}</option>`);
                }
            }
        }
        validateDropdown('#ddAssessmentType');
        await this.onEntitlementChanged(skipAuditPref);
        // ToDo: set to unselected if selected value doesn't exist
    }

    async onEntitlementChanged(skipAuditPref) {
        let val = jq('#entitlementSelectList').val();
        let {entitlementId, frequencyId, frequencyType} = parseEntitlementDropdownValue(val);
        jq('#entitlementId').val(entitlementId);
        jq('#frequencyId').val(frequencyId);
        jq('#frequencyId').val();
        jq('#entitlementFreqType').val(frequencyType);
        jq('#purchaseEntitlementsForm input').prop('checked', (entitlementId <= 0));
        if (skipAuditPref !== true) await this.loadAuditPrefOptions(jq('#ddAssessmentType').val(), frequencyId);
    }

    async loadAuditPrefOptions(assessmentType, frequencyId) {
        let apSel = jq('#auditPreferenceSelectList');
        let curVal = apSel.val();
        let apCont = jq('#auditPreferenceForm');
        let setSpinner = apCont.hasClass('spinner') === false;

        if (setSpinner) apCont.addClass('spinner');
        apSel.find('option').remove();
        assessmentType = numberOrNull(assessmentType);
        frequencyId = numberOrNull(frequencyId);

        if (this.releaseId && assessmentType && frequencyId) {
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
        }

        let optCnt = apSel.find('option').length;

        if (optCnt < 1) {
            apSel.append(_auditPrefOption.automated);
            apSel.prop('disabled', true);
        } else if (optCnt === 1) {
            // For some reason selection wouldn't work without setTimeout
            setTimeout(_ => {
                apSel.find('option').first().prop('selected', true);
                apSel.prop('disabled', true);
            }, 50);
        } else {
            // For some reason selection wouldn't work without setTimeout
            setTimeout(_ => {
                if (curVal) apSel.val(curVal); else apSel.find('option').first().prop('selected', true);
                apSel.prop('disabled', false);
            }, 50);
        }

        if (setSpinner) apCont.removeClass('spinner');
    }

    async loadEntitlementSettings(releaseChangedPayload) {
        if (releaseChangedPayload && releaseChangedPayload.mode === ReleaseSetMode.releaseId
            && numberOrNull(releaseChangedPayload.releaseId) > 0) {
            this.uiLoaded = true;
        }
        if (!this.uiLoaded) {
            this.scanTypeVisibility(false);
            this.scanTypeUserControlVisibility("allTypes", false);
            this.apiTypeUserControlVisibility("allTypes", false);
            this.deferredLoadEntitlementSettings = _ => this.loadEntitlementSettings(releaseChangedPayload);
            return;
        } else this.deferredLoadEntitlementSettings = null;
        this.releaseId = null;
        let rows = jq(fodeRowSelector);
        rows.hide();
        this.hideMessages();

        let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
        let fields = jq('.fode-field.spinner-container');
        releaseId = numberOrNull(releaseId);
        if ( releaseId && releaseId > 0) {
            this.releaseId = releaseId;
            fields.addClass('spinner');
            rows.show();
            jq('.fode-row-bsi').hide();

            let ssp = this.api.getReleaseEntitlementSettings(releaseId, getAuthInfo(), true)
                .then(r => this.scanSettings = r).catch((err) => {
                        console.error("release settings api failed: " + err);
                        throw err;
                    }
                );

            let entp = this.api.getAssessmentTypeEntitlements(releaseId, getAuthInfo())
                .then(r => this.assessments = r)
                .catch((err) => {
                    console.error("entitlement api failed");
                    throw err;
                });
            //ToDo- read from constant instead of API
            let tzs = this.api.getTimeZoneStacks(getAuthInfo())
                .then(r => this.timeZones = r).catch(
                    (err) => {
                        console.error("timezone api failed: " + err)
                        throw err;
                    }
                );
            //ToDo- read from constant instead of API
            let networkAuthTypes = this.api.getNetworkAuthType(getAuthInfo()).then(
                r => this.networkAuthTypes = r
            ).catch((err) => {
                console.error(err);
                throw err;
            });

            await Promise.all([ssp, entp, tzs, networkAuthTypes])
                .then(async () => {

                    if (this.scanSettings && this.assessments) {
                        let assessmentId = this.scanSettings.assessmentTypeId;
                        let timeZoneId = this.scanSettings.timeZone;
                        this.populateAssessmentsDropdown();
                        jq('#ddAssessmentType').val(assessmentId);
                        await this.onAssessmentChanged(true);
                        jq('#entitlementFreqType').val(this.scanSettings.entitlementFrequencyType);
                        await this.onEntitlementChanged(false);
                        this.setSelectedEntitlementValue(entp);
                        jq('#timeZoneStackSelectList').val(timeZoneId);
                        this.onLoadTimeZone();
                        this.onTimeZoneChanged();
                        this.setScanType();
                        this.onScanTypeChanged();
                        this.setScanPolicy();
                        this.setTimeBoxScan();
                        //Set the Website assessment scan type specific settings.
                        if (this.scanSettings.websiteAssessment) {
                            jq('#dast-standard-site-url').find('input')
                                .val(this.scanSettings.websiteAssessment.dynamicSiteUrl);
                        }
                        this.setWorkflowDrivenScanSetting();
                        this.setApiScanSetting();
                        this.setRestrictScan();
                        jq('#ddlNetworkAuthType').val(networkAuthTypes);
                        this.onNetworkAuthTypeLoad();
                        this.onNetworkAuthTypeChanged();
                        this.setNetworkSettings();
                        this.setLoginMacroCreationDetails();
                        this.setFalsePositiveFlagRequest();
                        this.setPatchUploadFileId();
                        this.scanSettingsVisibility(true);
                        this.scanTypeVisibility(true);
                        validateRequiredFields(requiredFieldsFreestyle);
                    } else {
                        await this.onAssessmentChanged(false);
                        this.showMessage('Failed to retrieve scan settings from API', true);
                        rows.hide();
                    }
                })
                .catch((reason) => {
                    console.error("error in scan settings: " + reason);
                })

        } else {
            await this.onAssessmentChanged(false);
            if (releaseChangedPayload.mode === ReleaseSetMode.releaseSelect) this.showMessage('Select a release'); else this.showMessage('Enter a release id');
        }

        fields.removeClass('spinner');
    }

    setLoginMacroCreationDetails(){
            if(this.scanSettings.websiteAssessment.loginMacroFileCreationDetails){
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
        }

    setFalsePositiveFlagRequest(){
            if(this.scanSettings.requestFalsePositiveRemoval){
                if (jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').prop('checked') === false) {
                       jq('#requestFalsePositiveRemovalRow').find('input:checkbox:first').trigger('click');
                }
            }
        }
    setWorkflowDrivenScanSetting() {
        if (this.scanSettings.workflowdrivenAssessment) {
            if (this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro) {
                jq('#listWorkflowDrivenAllowedHostUrl').empty();
                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].fileId);
                this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].allowedHosts.forEach((item, index, arr) => {
                        console.log(item);
                        let ident = arr[index];
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
                )
            }
        }
    }

    setApiScanSetting() {
        if (this.scanSettings.apiAssessment) {
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

    setSelectedEntitlementValue(entitlements) {

        let currValSelected = false;
        let curVal = getEntitlementDropdownValue(this.scanSettings.entitlementId, this.scanSettings.entitlementFrequencyType);
        let entitlement = jq('#entitlementSelectList');
        for (let ts of Object.keys(entitlements)) {
            let at = entitlement[ts];
            console.log(at.text);
            console.log(at.value);
            if (curVal && at.value && curVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                entitlement.append(`<option value="${at.text}" selected>${at.text}</option>`);
                jq('#entitlementId').val(at.text);
            } else {
                if (at.text) {
                    entitlement.append(`<option value="${at.text}">${at.text}</option>`);
                }
            }
        }
    }

    setPatchUploadFileId() {
        if (this.scanSettings &&
            this.scanSettings.loginMacroFileId && this.scanSettings.loginMacroFileId > 0) {
            jq('#loginMacroId').val(this.scanSettings.loginMacroFileId);
        }
        if (this.scanSettings.workflowdrivenAssessment && this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro) {
            if (this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro.fileId > 0)
                jq('#workflowMacroId').val(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro.fileId);
        }
    }

    setNetworkSettings() {
        if (this.scanSettings && this.scanSettings.networkAuthenticationSettings) {
            jq('#networkUsernameRow').find('input').val(this.scanSettings.networkAuthenticationSettings.userName);
            jq('#networkPasswordRow').find('input').val(this.scanSettings.networkAuthenticationSettings.password);
            if (jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').prop('checked') === false &&
                this.scanSettings.networkAuthenticationSettings.password) {
                jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').trigger('click');
            }
            let np = jq('#networkPasswordRow').find('input');
            np.attr('type', 'password');
        }
    }

    resetNetworkSettings() {
        jq('#networkUsernameRow').find('input').val(undefined);
        jq('#networkPasswordRow').find('input').val(undefined);
        jq('#ddlNetworkAuthType').prop('selectedIndex', 0);

        let ctl = jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first')
        if(ctl.prop('checked') === true)
          ctl.trigger('click');
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
    onLoadTimeZone() {
        let tsSel = jq('#timeZoneStackSelectList');
        let currVal = this.scanSettings.timeZone;
        let currValSelected = false;
        tsSel.find('option').not(':first').remove();
        tsSel.find('option').first().prop('selected', true);
        for (let ts of Object.keys(this.timeZones)) {
            let at = this.timeZones[ts];
            if (currVal && at.value && currVal.toLowerCase() === at.value.toLowerCase()) {
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
        if (this.scanSettings.networkAuthenticationSettings &&
            this.scanSettings.networkAuthenticationSettings.networkAuthenticationType) {
            currVal = this.scanSettings.networkAuthenticationSettings.networkAuthenticationType;
        }
        let currValSelected = false;
        networkAuthTypeSel.find('option').not(':first').remove();
        networkAuthTypeSel.find('option').first().prop('selected', true);
        for (let ts of Object.keys(this.networkAuthTypes)) {
            let at = this.networkAuthTypes[ts];
            if (currVal && at.value && currVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                networkAuthTypeSel.append(`<option value="${at.text}" selected>${at.text}</option>`);
            } else {
                networkAuthTypeSel.append(`<option value="${at.text}">${at.text}</option>`);
            }
        }
    }
    onScanTypeChanged() {
        this.resetAuthSettings();
        jq('#apiTypeList').prop('selectedIndex', 0);
        let selectedScanTypeValue = jq('#scanTypeList').val();
        if (!selectedScanTypeValue) {
            //Reset All ScanTypes Controls
            this.scanTypeUserControlVisibility('allTypes', false);
        } else {
            this.scanTypeUserControlVisibility(selectedScanTypeValue, true);
            validateDropdown('#scanTypeList');
        }
    }

     apiScanSettingVisibility(isVisible) {
            let apiScanSettingRows = jq('.' + dastApiSetting);
            jq('.' + dastApiSpecificControls).hide();
            if (!isVisible) {
                apiScanSettingRows.hide();
            } else {
                apiScanSettingRows.show();
                validateDropdown('#apiTypeList');
            }
        }

    onTimeZoneChanged() {
        validateDropdown('#timeZoneStackSelectList');
    }

    onNetworkAuthTypeChanged() {
        validateDropdown('#ddlNetworkAuthType');
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
            let ScanPolicy = ["Standard", "Critical and high", "Passive"]
            let scanPolicySel = jq('#dast-standard-scan-policy').find('select');
            let currValSelected = false;
            scanPolicySel.find('option').not(':first').remove();
            scanPolicySel.find('option').first().prop('selected', true);
            for (let p of ScanPolicy) {
                if (selectedScanPolicyType && selectedScanPolicyType.toLowerCase() === p.toLowerCase()) {
                    currValSelected = true;
                    scanPolicySel.append(`<option value="${p}" selected>${p}</option>`);
                } else {
                    scanPolicySel.append(`<option value="${p}">${p}</option>`);
                }
            }
        }
    }

    setTimeBoxScan() {
        if (this.scanSettings.timeBoxInHours && this.scanSettings.timeBoxInHours > 0) {
            jq('#dast-timeBox-scan').find('input:text:first').val(this.scanSettings.timeBoxInHours);
            if (jq('#dast-timeBox-scan').find('input:checkbox:first').prop('checked') === false) {
                jq('#dast-timeBox-scan').find('input:checkbox:first').trigger('click');
            }
        }
    }

    onLoginMacroFileUpload() {
        jq('#webSiteLoginMacro').val(true);
        let loginMacroFile = document.getElementById('loginFileMacro').files[0];
        let ctl = '#loginMacroUploadContainer';
        let msgCtl = '#loginMacroUploadMessage';
        handleSpinner(ctl, false);
        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), loginMacroFile, dastManifestLoginFileUpload).then(res => {
                if (res && res.fileId > 0) {
                    jq('#loginMacroId').val(res.fileId);
                    handleSpinner(ctl, true);
                    handleUploadStatusMessage(msgCtl, fileUploadSuccess, true);
                } else {
                    handleUploadStatusMessage(msgCtl, inValidResponse + "error =" + res.message, false);
                }
            }
        ).catch((err) => {
                console.log(err);
                handleSpinner(ctl, true);
                handleUploadStatusMessage(msgCtl, fileUploadFailed, false);
            }
        );
    }

    onWorkflowMacroFileUpload() {
        let workFlowMacroFile = document.getElementById('workflowMacroFile').files[0];
        let ctl = '#dast-workflow-macro-upload';
        let msgCtl = '#workflowMacroUploadStatusMessage';
        handleSpinner(ctl, false);
        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), workFlowMacroFile, dastManifestWorkflowMacroFileUpload).then(res => {
                //Todo: - check
                console.log("File upload success " + res);
                handleSpinner(ctl, true);
                if (res && res.fileId > 0) {
                    jq('#workflowMacroId').val(res.fileId)
                    handleUploadStatusMessage(msgCtl, fileUploadSuccess, true);
                } else {
                    handleUploadStatusMessage(msgCtl, inValidResponse + "error =" + res.message, false);
                    return;
                }
                if (res && res.hosts) {
                    let hosts = undefined;
                    res.hosts.forEach(hostIterator);
                    function hostIterator(item, index, arr) {
                        if (arr) {
                            if (hosts)
                                hosts = hosts + "," + arr[index];
                            else
                                hosts = arr[index];
                        }
                    }
                    jq('#workflowMacroHosts').val(hosts);
                    jq('#listWorkflowDrivenAllowedHostUrl').empty();
                    //set the allowed hosts  html list value
                    if (hosts) {
                        let host = hosts.split(',');
                        host.forEach((item) => {
                            jq('#listWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox' id=' " + item +
                                " ' checked='checked' name='" + item + "'>" + item + "</li>");
                        });
                    }
                } else
                    throw Error("Invalid hosts info");
            }
        ).catch((err) => {
                console.log('err' + err);
                handleSpinner(ctl, true);
                handleUploadStatusMessage(msgCtl, fileUploadFailed, false);
            }
        );
    }

    onFileUpload(event) {
        let uploadContainer = '#' + jq('#' + event.target.id).closest('span').attr('id');
        handleSpinner(uploadContainer, false);
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
                displayMessage = jq('#graphQlUploadMessage');
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
        if (!file ||!fileType ||!elem) {
            throw new Error("Illegal argument exception,File Type not valid");
        }
        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), file, fileType).then(res => {
                //Todo: - check
                console.log("File upload success " + res);
                handleSpinner(uploadContainer, true);
                if (res && res.fileId > 0) {
                    elem.val(res.fileId);
                    jq('.uploadedFileDetails').text(res.fileName);
                    jq('.uploadedFileContainer').show();
                    handleUploadStatusMessage(displayMessage, fileUploadSuccess, true);
                } else {
                    handleUploadStatusMessage(displayMessage, fileUploadFailed, false)
                    console.log("Illegal argument exception,FileId not valid");
                }
            }
        ).catch((err) => {
                handleSpinner(uploadContainer, true);
                handleUploadStatusMessage(displayMessage, fileUploadFailed, false)
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
        if (id === 'openApiInputFile') {
            jq('.openApiSourceControls').show()
            jq('#dast-api-openApi-upload').show();
            jq('#openApiRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'openApiInputUrl') {
            jq('.openApiSourceControls').show()
            jq('#dast-openApi-url').show();
            jq('#openApiRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'graphQlInputFile') {
            jq('.graphQLSourceControls').show();
            jq('#dast-api-graphQL-upload').show();
            jq('#graphQlRadioSource').val(jq('#' + event.target.id).val());
        } else if (id === 'graphQlInputUrl') {
            jq('.graphQLSourceControls').show();
            jq('#dast-graphQL-url').show();
            jq('#graphQlRadioSource').val(jq('#' + event.target.id).val());
        } else {
            jq('.sourceOptions').prop('checked', false);
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

    onExcludeUrlBtnClick(event, args) {
        let excludedUrl = jq('#standardScanExcludedUrlText').val();
        jq('#listStandardScanTypeExcludedUrl');
        jq('#listStandardScanTypeExcludedUrl').append("<li>" + excludedUrl + "</li>");
        jq('#listStandardScanTypeExcludedUrl').show();
    }

   async preInit() {
        jq('.fode-field')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.addClass('fode-field-row');
                let vtr = getValidationErrRow(tr);

                if (vtr) vtr.addClass('fode-field-row-verr');
            });
        jq('.fode-row-hidden')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.hide();
                let vtr = getValidationErrRow(tr);

                if (vtr) vtr.hide();
            });

        jq(fodeRowSelector).hide();

        // Move css classes prefixed with fode-row to the row element
        jq('.fode-field')
            .each((i, e) => {
                let jqe = jq(e);
                let classes = jqe.attr('class').split(' ');

                for (let c of classes) {
                    if (c.startsWith('fode-row-')) {
                        jqe.removeClass(c);
                        let tr = jqe.closest('.fode-field-row');

                        tr.addClass(c)

                    }
                }
            });
       await this.init();
    }

    async init() {
        try {
            this.hideMessages();
            this.showMessage('Select a release');
            if (this.unsubInit) unsubscribeEvent('authInfoChanged', this.unsubInit);
            jq('#ddAssessmentType')
                .change(_ => this.onAssessmentChanged());
            jq('#entitlementSelectList')
                .change(_ => this.onEntitlementChanged());
            jq('#scanTypeList').change(_ => this.onScanTypeChanged());
            jq('#btnAddExcludeUrl').click(_ => this.onExcludeUrlBtnClick());
            jq('#btnUploadLoginMacroFile').click(_ => this.onLoginMacroFileUpload());
            jq('#btnUploadWorkflowMacroFile').click(_ => this.onWorkflowMacroFileUpload());
            jq('#listWorkflowDrivenAllowedHostUrl').click(_ => this.onWorkflowDrivenHostChecked(event));
            jq('#apiTypeList').change(_ => this.onApiTypeChanged());
            jq('#openApiInputFile, #openApiInputUrl, #graphQlInputFile, #graphQlInputUrl').change(_ => this.onSourceChange(event.target.id));
            jq('#btnUploadPostmanFile, #btnUploadOpenApiFile, #btnUploadgraphQLFile, #btnUploadgrpcFile').click(_ => this.onFileUpload(event));
            jq('.fode-row-screc').hide();
            jq('.uploadedFileContainer').hide();
            jq('#requestFalsePositiveRemovalRow').hide();
            jq('#loginMacroFileCreationRow').hide();
            jq('#timeZoneStackSelectList').change(_ => this.onTimeZoneChanged());
            jq('#ddlNetworkAuthType').change(_ => this.onNetworkAuthTypeChanged());
            setOnblurEventForFreestyle(setOnblurEventForPipeline);
            this.uiLoaded = true;
            if (this.deferredLoadEntitlementSettings) {
                this.deferredLoadEntitlementSettings();
                this.deferredLoadEntitlementSettings = null;
            }
        } catch (err) {
           console.log(err);
        }

    }
}

const scanSettings = new DastFreeStyle();
spinAndWait(
    () => jq(
            '#selectedRelease'
        ).text()
        !==
        undefined
        &&
        jq(
            '#selectedRelease'
        ).text()
        !==
        ''
).then(scanSettings.preInit
        .bind(scanSettings));
spinAndWait(
    () => jq(
        '#releaseTypeSelectList'
    ).val() !== undefined).then(scanSettings
    .scanSettingsVisibility
    .bind(scanSettings));
