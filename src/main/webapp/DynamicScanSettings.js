const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';
const fodStandardScanTypeClassIdr = ".dast-standard-scan";
const fodApiScanTypeClassIdr = ".dast-api-scan"
const fodWorkflowScanTypeClassIdr = ".dast-workflow-scan"
const dastScanSettingRow = ".dast-scan-setting"
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
            debugger;
            ddlScanType.hide();
        } else {
            jq('.dast-scan-type').show();
        }
    }

    scanTypeUserControlVisibility(scanType, isVisible) {
        debugger;
        if ((isVisible === undefined || null) || isVisible === false) {
            switch (scanType) {
                case "Standard": {
                    this.standardScanSettingsVisibility(isVisible);
                    this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-standard-scan-policy")
                    break;
                }
                case "API": {
                    this.apiScanSettingVisibility(isVisible);
                    break;
                }
                case "Workflow":
                    this.workflowScanSettingVisibility(isVisible);
                    this.setDefaultValuesForSelectBasedOnScanType(scanType, "dast-standard-scan-policy")
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
        debugger;
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
        jq('.dast-workflow-scan').each((iterator, element) => {
            let currentElement = jq(element);
            let tr = closestRow(currentElement);
            tr.addClass(fodApiSCanTypeClassIdr);
        });
        let workflowScanSettingRows = jq('.dast-workflow-scan');
        if ((isVisible === undefined || null) || isVisible === false) {

            workflowScanSettingRows.hide();
        } else {
            workflowScanSettingRows.show();
        }
    }

    scanSettingsVisibility(isVisible) {
        debugger;
        if ((isVisible === undefined || null) || isVisible === false) {
            jq('.dast-scan-setting').each((iterator, element) => {
                let currentElement = jq(element);
                let tr = closestRow(currentElement);
                tr.addClass('dast-scan-setting');
            });
            let scanSettingsRows = jq('.dast-scan-setting');
            debugger;
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
            debugger;
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
        debugger;
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
        debugger;

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

        if (releaseChangedPayload && releaseChangedPayload.mode === DastReleaseSetMode.bsiToken) {
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

            debugger;

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

                        debugger;
                        jq('#entitlementSelectList').val(getEntitlementDropdownValue(entitlementId, this.scanSettings.entitlementFrequencyType));

                        jq('#entitlementFreqType').val(this.scanSettings.entitlementFrequencyType);
                        alert(jq('#entitlementFreqType').val());
                        await this.onEntitlementChanged(false);

                        this.onLoadTimeZone();
                        jq('#timeZoneStackSelectList').val(timeZoneId);

                        this.onNetworkAuthTypeLoad();
                        jq('#ddlNetworkAuthType').val(networkAuthTypes);

                        this.onGeoLocationLoad();
                        jq('#geoLocationStackSelectList').val(geoLocID);

                        //Enable scan Type right after assessment Type drop populated.
                        this.scanSettingsVisibility(true);
                        this.scanTypeVisibility(true);
                        this.scanTypeUserControlVisibility("allType", false);

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

    onLoadTimeZone() {

        let tsSel = jq('#timeZoneStackSelectList');
        let currVal = tsSel.val();
        let currValSelected = false;
        // tsSel.find('option').not(':first').remove();
        tsSel.find('option').first().prop('selected', true);
        for (let ts of Object.keys(this.timeZones)) {
            let at = this.timeZones[ts];
            if (currVal === at.value) {
                currValSelected = true;
                tsSel.append(`<option value="${at.value}" selected>${at.text}</option>`);
            } else {
                tsSel.append(`<option value="${at.value}">${at.text}</option>`);
            }
        }
    }

    onGeoLocationLoad() {
        debugger;
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

        alert("change");
        let networkAuthType = jq('#ddlNetworkAuthType');
        networkAuthType.find('option').not(':first').remove();
        networkAuthType.find('option').first().prop('selected', true);
        for (let ts of Object.keys(this.networkAuthTypes)) {
            let at = this.networkAuthTypes[ts];
            networkAuthType.append(`<option value="${at.value}">${at.text}</option>`);
        }
    }

    onNetworkAuthTypeChanged() {
        alert('changed');
    }

    onScanTypeChanged() {

        let selectedScanType = jq('#scanTypeList').val();
        //Reset All ScanTypes Controls
        this.scanTypeUserControlVisibility("allTypes", false);

        if (selectedScanType === "Standard") {
            this.standardScanSettingsVisibility(true);
        }
    }

    onLoginMacroFileUpload() {
        debugger;
        jq('#webSiteLoginMacro').val(true);
        let loginMacroFile = document.getElementById('loginFileMacro').files[0];
        this.api.patchLoginMacroFile(this.releaseId, getAuthInfo(), loginMacroFile).then(res => {
                console.log("File upload success " + res);
                jq('#loginMacroId').val(res)

            }
        ).catch((err) => {
                console.log(err);
            }
        );
    }


    onExcludeUrlBtnClick(event, args) {
        debugger;
        //  alert(jq('#standardScanExcludedUrlText').val())
        let excludedUrl = jq('#standardScanExcludedUrlText').val();
        //Add to exclude list
        jq('#listStandardScanTypeExcludedUrl');
        jq('#listStandardScanTypeExcludedUrl').append("<li>" + excludedUrl + "</li>");
        jq('#listStandardScanTypeExcludedUrl').show();
    }

    preinit() {
        debugger;

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
