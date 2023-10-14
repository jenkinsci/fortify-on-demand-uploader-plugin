const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';
const fodStandardScanTypeClassIdr = "dast-standard-scan";
const fodApiScanTypeClassIdr = "dast-api-scan";
const fodWorkflowScanTypeClassIdr = "dast-workflow-scan";
const dastManifestWorkflowMacroFileUpload = "WorkflowDrivenMacro";
const dastManifestLoginFileUpload = "LoginMacro";

const dastScanSettingRow = ".dast-scan-setting"

const dastScanTypes = [{"value": 'Standard', "text": 'Standard'}, {
    "value": 'Workflow-driven',
    "text": 'Workflow-driven'
},
    {"value": 2, "text": 'API'}]
const dastScanSelectDEfaultValues =
    {
        "WebSiteScan": {"ScanPolicy": "standard"},
        "WorkflowDrivenScan": {"ScanPolicy": "standard"},
        "ApiScan": {"ScanPolicy": "high"}
    }


class DynamicScanSettings {

    constructor() {
        this.api = new Api(instance, descriptor);
        this.uiLoaded = false;
        this.releaseId = null;
        this.geoLocStack = {};
        subscribeToEvent('releaseChanged', p => this.loadEntitlementSettings(p.detail));
    }

    showMessage(msg, isError) {
        let msgElem;

        if (isError) msgElem = jq('#fode-error'); else msgElem = jq('#fode-msg');

        msgElem.text(msg);
        msgElem.show();
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

    scanTypeUserControlVisibility(scanType, isVisible) {
        debugger;
        if ((isVisible !== undefined || null) && isVisible === true) {
            switch (scanType) {
                case "Standard": {
                    this.standardScanSettingsVisibility(isVisible);
                    this.workflowScanSettingVisibility(false);
                    this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-standard-scan-policy")
                    break;
                }
                case "API": {
                    this.apiScanSettingVisibility(isVisible);
                    break;
                }
                case "Workflow-driven":
                    this.workflowScanSettingVisibility(isVisible);
                    this.standardScanSettingsVisibility(false);
                    this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-workflow-scan-policy")
                    break;
                default:
                    //hide all scan type settings.
                    this.standardScanSettingsVisibility(false);
                    this.apiScanSettingVisibility(false);
                    this.workflowScanSettingVisibility(false);
                    break;
            }
        }
    }

    standardScanSettingsVisibility(isVisible) {

        jq('.dast-standard-scan').each((iterator, element) => {
            let currentElement = jq(element);
            let tr = closestRow(currentElement);
            tr.addClass('dast-standard-scan');
        });
        let standardScanSettingRows = jq('.dast-standard-scan');
        if ((isVisible === undefined || null) || isVisible === false) {
            standardScanSettingRows.hide();
        } else {
            standardScanSettingRows.show();
        }
    }


    setDefaultValuesForSelectBasedOnScanType(scanType, selectControl) {
        switch (scanType) {
            case "Standard":
                jq('#' + selectControl).val(dastScanSelectDEfaultValues.WebSiteScan.ScanPolicy);
                break;
            case "Workflow":
                jq('#' + selectControl).val(dastScanSelectDEfaultValues.WorkflowDrivenScan.ScanPolicy);
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
        debugger;

        jq('.dast-workflow-scan').each((iterator, element) => {
            let currentElement = jq(element);
            let tr = closestRow(currentElement);
            tr.addClass(fodWorkflowScanTypeClassIdr);
        });
        let workflowScanSettingRows = jq('.dast-workflow-scan');

        if ((isVisible === undefined || null) || isVisible === false) {
            workflowScanSettingRows.hide();
        } else {
            workflowScanSettingRows.show();
        }
    }

    scanSettingsVisibility(isVisible) {

        if ((isVisible === undefined || null) || isVisible === false) {
            jq('.dast-scan-setting').each((iterator, element) => {
                let currentElement = jq(element);
                let tr = closestRow(currentElement);
                tr.addClass('dast-scan-setting');
            });
            let scanSettingsRows = jq('.dast-scan-setting');

            scanSettingsRows.hide();
        } else {
            jq('.dast-scan-setting').show();
        }
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

            atsel.append(`<option value="${at.id}">${at.name}</option>`);
        }
    }

    async onAssessmentChanged(skipAuditPref) {
        let atval = jq('#ddAssessmentType').val();
        let entsel = jq('#entitlementSelectList');
        let at = this.assessments ? this.assessments[atval] : null;
        console.log(this.assessments);
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

    async loadDynamicScanType(assessmentType) {
        console.log(assessmentType);

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

        if (!this.uiLoaded) {
            this.scanTypeVisibility(false);
            this.scanTypeUserControlVisibility("allTypes", false);
            this.deferredLoadEntitlementSettings = _ => this.loadEntitlementSettings(releaseChangedPayload);
            return;
        } else this.deferredLoadEntitlementSettings = null;

        this.releaseId = null;
        let rows = jq(fodeRowSelector);
        rows.hide();
        this.hideMessages();

        if (releaseChangedPayload && releaseChangedPayload.mode === ReleaseSetMode.bsiToken) {
            this.isBsi = true;
            jq('.fode-row-bsi').show();
            jq('.fode-row-remediation').show();
            return;
        }

        let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
        let fields = jq('.fode-field.spinner-container');

        releaseId = numberOrNull(releaseId);

        if (releaseId > 0) {
            this.releaseId = releaseId;
            fields.addClass('spinner');
            rows.show();
            jq('.fode-row-bsi').hide();


            let ssp = this.api.getReleaseEntitlementSettings(releaseId, getAuthInfo())
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

            let tzs = this.api.getTimeZoneStacks(getAuthInfo())
                .then(r => this.timeZones = r).catch(
                    (err) => {
                        console.error("timezone api failed: " + err)
                        throw err;
                    }
                );

            let geoLoc = this.api.getGeoLocationStacks(getAuthInfo())
                .then(r => this.geoLocStacks = r)
                .catch((err) => {
                    console.error("geoloc api failed: " + err);
                    throw err;
                });

            let networkAuthTypes = this.api.getNetworkAuthType(getAuthInfo()).then(
                r => this.networkAuthTypes = r
            ).catch((err) => {
                console.error(err);
                throw err;
            });

            await Promise.all([ssp, entp, tzs, geoLoc, networkAuthTypes])
                .then(async () => {

                    console.log("success");

                    if (this.scanSettings && this.assessments) {
                        let assessmentId = this.scanSettings.assessmentTypeId;
                        let entitlementId = this.scanSettings.entitlementId;
                        let timeZoneId = this.scanSettings.timeZone;
                        let geoLocID = this.scanSettings.geoLocStack;

                        this.populateAssessmentsDropdown();

                        jq('#ddAssessmentType').val(assessmentId);
                        await this.onAssessmentChanged(true);

                        // jq('#entitlementSelectList').val(getEntitlementDropdownValue(this.scanSettings.entitlementId, this.scanSettings.entitlementFrequencyType));

                        jq('#entitlementFreqType').val(this.scanSettings.entitlementFrequencyType);
                        // alert(jq('#entitlementFreqType').val());
                        await this.onEntitlementChanged(false);
                        debugger;
                        this.setSelectedEntitlementValue(entp);

                        jq('#timeZoneStackSelectList').val(timeZoneId);
                        this.onLoadTimeZone();

                        debugger;
                        /*'set the scan type based on the scan setting get response'*/
                        this.setScanType();
                        this.onScanTypeChanged();

                        //Set scan policy from the response.
                        this.setScanPolicy();

                        debugger;
                        //ToDo - url will be array from the response ?
                        /*Set dynamic site URL from response */

                        //Set the Website assessment scan type specific settings.
                        if (!Object.is(this.scanSettings.websiteAssessment, undefined)) {
                            jq('#dast-standard-site-url').find('input').val(this.scanSettings.websiteAssessment.urls[0]);
                        }

                        this.setWorkflowDrivenScanSetting();

                        /*Set restrict scan value from response to UI */
                        this.setRestrictScan();

                        /*set allow http(s) */
                        this.setHttpSettings();

                        /* Set Allow Form Submission*/
                        this.setFormSubmission();

                        /*Set network settings from response. */
                        jq('#ddlNetworkAuthType').val(networkAuthTypes);
                        this.onNetworkAuthTypeLoad();
                        this.setNetworkSettings();

                        //Set the PatchUploadManifest File's fileId from get response.
                        this.setPatchUploadFileId();

                        jq('#geoLocationStackSelectList').val(geoLocID);
                        this.onGeoLocationLoad();

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
                })

        } else {
            await this.onAssessmentChanged(false);
            if (releaseChangedPayload.mode === DastReleaseSetMode.releaseSelect) this.showMessage('Select a release'); else this.showMessage('Enter a release id');
        }

        fields.removeClass('spinner');
    }

    setWorkflowDrivenScanSetting() {

        debugger;
        //only single file upload is allowed from FOD. Todo Iterate the array
        if (!Object.is(this.scanSettings.workflowdrivenAssessment, undefined)) {
            if (!Object.is(this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro, undefined)) {
                this.scanSettings.workflowdrivenAssessment.workflowDrivenMacro[0].allowedHosts.forEach((item, index, arr) => {
                        console.log(item);
                        jq('#lisWorkflowDrivenAllowedHostUrl').append("<li>" + "<input type='checkbox'>" + arr[index] + "</li>")
                    }
                )

            }

        }

    }

    setSelectedEntitlementValue(entitlements) {
        debugger;
        let currValSelected = false;
        let curVal = getEntitlementDropdownValue(this.scanSettings.entitlementId, this.scanSettings.entitlementFrequencyType);
        let entitlement = jq('#entitlementSelectList');
        for (let ts of Object.keys(entitlements)) {
            let at = this.entp[ts];
            if (curVal !== undefined && curVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                entitlement.append(`<option value="${at.text}" selected>${at.text}</option>`);
            } else {
                entitlement.append(`<option value="${at.text}">${at.text}</option>`);
            }
        }
    }

    setPatchUploadFileId() {
        debugger;
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

    setNetworkSettings() {
        if (!Object.is(this.scanSettings.networkAuthenticationSettings, null)
            && !Object.is(this.scanSettings.networkAuthenticationSettings, undefined)) {
            jq('#networkUsernameRow').find('input').val(this.scanSettings.networkAuthenticationSettings.userName);
            jq('#networkPasswordRow').find('input').val(this.scanSettings.networkAuthenticationSettings.password);

            jq('#webSiteNetworkAuthSettingEnabledRow').find('input:checkbox:first').trigger('click');

            let np  = jq('#networkPasswordRow').find('input');
            np.attr('type','password');

        }
    }

    setScanType() {
        debugger;

        if (this.scanSettings !== undefined && this.scanSettings !== null) {
            let selectedScanType;
            if (this.scanSettings.websiteAssessment !== null && this.scanSettings.websiteAssessment !== undefined) {
                selectedScanType = dastScanTypes.find(v => v.value === "Standard");
            } else if (this.scanSettings.workflowdrivenAssessment !== null && this.scanSettings.workflowdrivenAssessment !== undefined) {
                selectedScanType = dastScanTypes.find(v => v.value === "Workflow-driven")
            }
            // Check for API Type
            debugger;
            //Set other scan type values in the dropdown.
            let scanSel = jq('#scanTypeList');
            let currValSelected = false;
            scanSel.find('option').not(':first').remove();
            scanSel.find('option').first().prop('selected', true);

            for (let s of Object.keys(dastScanTypes)) {
                let at = dastScanTypes[s];
                if (selectedScanType !== undefined && (selectedScanType.value.toLowerCase() === at.text.toLowerCase())) {
                    currValSelected = true;
                    scanSel.append(`<option value="${at.value}" selected>${at.text}</option>`);
                } else {
                    scanSel.append(`<option value="${at.value}">${at.text}</option>`);
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
            if (currVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                tsSel.append(`<option value="${at.value}" selected>${at.text}</option>`);
            } else {
                tsSel.append(`<option value="${at.value}">${at.text}</option>`);
            }
        }
    }

    onGeoLocationLoad() {

        let geoSel = jq('#geoLocationStackSelectList');
        console.log(this.geoLocations);
        geoSel.find('option').not(':first').remove();
        geoSel.find('option').first().prop('selected', true);
        for (let ts of Object.keys(this.geoLocStacks)) {
            let at = this.geoLocStacks[ts];
            geoSel.append(`<option value="${at.value}">${at.text}</option>`);
        }
    }

    onNetworkAuthTypeLoad() {
        debugger;

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
            if (currVal !== undefined && currVal.toLowerCase() === at.value.toLowerCase()) {
                currValSelected = true;
                networkAuthTypeSel.append(`<option value="${at.text}" selected>${at.text}</option>`);
            } else {
                networkAuthTypeSel.append(`<option value="${at.text}">${at.text}</option>`);
            }
        }

    }

    onScanTypeChanged() {

        let selectedScanTypeValue = jq('#scanTypeList').val();

        if (selectedScanTypeValue === null || undefined) {
            //Reset All ScanTypes Controls
            this.scanTypeUserControlVisibility(null, false);
        } else {
            debugger;
            this.scanTypeUserControlVisibility(null, false);
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

    setHttpSettings() {
        if (this.scanSettings !== undefined && this.scanSettings.allowSameHostRedirects !== undefined) {
            jq('#allowHttp').prop('checked', this.scanSettings.allowSameHostRedirects);
        }
    }

    setFormSubmission() {
        if (this.scanSettings !== undefined && this.scanSettings.allowFormSubmissions !== null || undefined) {
            jq('#allowHttp').prop('checked', this.scanSettings.allowFormSubmissionCrawl);
        }
    }

    setScanPolicy() {

        debugger;
        if (this.scanSettings !== undefined && this.scanSettings.policy !== null || undefined) {
            let selectedScanType = this.scanSettings.policy
            let scanPolicySel = jq('#dast-standard-scan-policy').find('select');
            let currValSelected = false;
            scanPolicySel.find('option').not(':first').remove();
            scanPolicySel.find('option').first().prop('selected', true);

            for (let s of Object.keys(dastScanTypes)) {
                let at = dastScanTypes[s];
                if (selectedScanType.toLowerCase() === at.text.toLowerCase()) {
                    currValSelected = true;
                    scanPolicySel.append(`<option value="${at.value}" selected>${at.text}</option>`);
                } else {
                    scanPolicySel.append(`<option value="${at.value}">${at.text}</option>`);
                }
            }
        }
    }

    onLoginMacroFileUpload() {
        jq('#webSiteLoginMacro').val(true);
        let loginMacroFile = document.getElementById('loginFileMacro').files[0];
        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), loginMacroFile, dastManifestLoginFileUpload).then(res => {
                debugger;
                console.log("File upload success " + res);
                let response = res;
                jq('#loginMacroId').val(res)
            }
        ).catch((err) => {
                console.log(err);
            }
        );
    }

    onNetworkAuthTypeChanged() {
        // ToDo
    }

    onWorkflowMacroFileUpload() {
        debugger;
        let workFlowMacroFile = document.getElementById('workflowMacroFile').files[0];

        this.api.patchSetupManifestFile(this.releaseId, getAuthInfo(), workFlowMacroFile, dastManifestWorkflowMacroFileUpload).then(res => {
                debugger;
                //Todo: - check
                console.log("File upload success " + res);
                if (res.fileId > 0) {
                    jq('#workflowMacroId').val(res.fileId)
                } else {
                    throw new Exception("Illegal argument exception,FileId not valid");
                }
                if (!Object.is(res.hosts, undefined) && !Object.is(res.hosts, null)) {
                    let hosts;
                    res.hosts.forEach(hostIterator);

                    function hostIterator(item, index, arr) {
                        if (arr !== null || arr !== undefined) {
                            if (hosts !== null && hosts !== undefined)
                                hosts = hosts + "," + arr[index];
                            else
                                hosts = arr[index];
                        }
                    }

                    jq('#workflowMacroHosts').val(hosts)
                } else
                    throw new Exception("Invalid hosts info");
            }
        ).catch((err) => {
                console.log('eer' + err);

            }
        );
    }

    hostIterator(item, index, arr) {
        let hosts;
        if (arr !== null || arr !== undefined) {
            if (hosts !== null && hosts !== undefined)
                hosts = hosts + "," + arr[index];
            else
                hosts = arr[index];
        }
    }

    onExcludeUrlBtnClick(event, args) {
        //  alert(jq('#standardScanExcludedUrlText').val())
        let excludedUrl = jq('#standardScanExcludedUrlText').val();
        //Add to exclude list
        jq('#listStandardScanTypeExcludedUrl');
        jq('#listStandardScanTypeExcludedUrl').append("<li>" +  excludedUrl + "</li>");
        jq('#listStandardScanTypeExcludedUrl').show();
    }

    preinit() {


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
        this.init();
    }

    async init() {
        try {

        } catch (err) {
            // if (this.api.isAuthError(err)) {
            if (!this.unsubInit) {
                this.unsubInit = () => this.init();
                subscribeToEvent('authInfoChanged', this.unsubInit);
            }
            return;
        }

        this.hideMessages();
        this.showMessage('Select a release');

        if (this.unsubInit) unsubscribeEvent('authInfoChanged', this.unsubInit);

        jq('#ddAssessmentType')
            .change(_ => this.onAssessmentChanged());
        jq('#entitlementSelectList')
            .change(_ => this.onEntitlementChanged());

        jq('#scanTypeList').change(_ => this.onScanTypeChanged());

        jq('#ddlNetworkAuthType').change(_ => this.onNetworkAuthTypeChanged());

        jq('#btnAddExcludeUrl').click(_ => this.onExcludeUrlBtnClick());

        jq('#btnUploadLoginMacroFile').click(_ => this.onLoginMacroFileUpload());

        jq('#btnUploadWorkflowMacroFile').click(_ => this.onWorkflowMacroFileUpload());

        jq('.fode-row-screc').hide();
        this.uiLoaded = true;

        if (this.deferredLoadEntitlementSettings) {
            this.deferredLoadEntitlementSettings();
            this.deferredLoadEntitlementSettings = null;
        }
    }

}

const scanSettings = new DynamicScanSettings();

spinAndWait(() => jq('#selectedRelease').text() !== undefined && jq('#selectedRelease').text() !== '')
    .then(scanSettings.preinit.bind(scanSettings));
spinAndWait(() => jq('#releaseTypeSelectList').val() !== undefined).then(scanSettings.scanSettingsVisibility.bind(scanSettings));
