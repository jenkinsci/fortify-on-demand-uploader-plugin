const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';

class ScanSettings {

    constructor() {
        this.api = new Api(instance, descriptor);
        this.uiLoaded = false;
        this.techStacks = {};
        this.techStacksSorted = [];
        this.techIdsWithOutOpenSourceSupport = ["2", "3", "5", "6", "11", "14", "18", "21"];
        this.releaseId = null;
        this.isBsi = false;
        subscribeToEvent('releaseChanged', p => this.loadEntitlementSettings(p.detail));
    }

    showMessage(msg, isError) {
        let msgElem;

        if (isError) msgElem = jq('#fode-error');
        else msgElem = jq('#fode-msg');

        msgElem.text(msg);
        msgElem.show();
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

        entsel.find('option,optgroup').remove();

        if (at) {
            let available = at.entitlementsSorted.filter(e => e.id > 0);
            let forPurchase = at.entitlementsSorted.filter(e => e.id <= 0);
            let availableGrp = forPurchase.length > 0 ? jq(`<optgroup label="Available Entitlements"></optgroup>`) : entsel;

            for (let e of available) {
                availableGrp.append(`<option value="${getEntitlementDropdownValue(e.id, e.frequencyId)}">${e.description}</option>`);
            }

            if (forPurchase.length > 0) {
                let grp = jq(`<optgroup label="Available For Purchase"></optgroup>`);

                entsel.append(availableGrp);
                entsel.append(grp);
                for (let e of forPurchase) {
                    grp.append(`<option value="${getEntitlementDropdownValue(0, e.frequencyId)}">${e.description}</option>`);
                }
            }
        }

        await this.onEntitlementChanged(skipAuditPref);
        // ToDo: set to unselected if selected value doesn't exist
    }

    async onEntitlementChanged(skipAuditPref) {
        let val = jq('#entitlementSelectList').val();
        let {entitlementId, frequencyId} = parseEntitlementDropdownValue(val);

        jq('#entitlementId').val(entitlementId);
        jq('#frequencyId').val(frequencyId);
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
                if (curVal) apSel.val(curVal);
                else apSel.find('option').first().prop('selected', true);
                apSel.prop('disabled', false);
            }, 50);
        }

        if (setSpinner) apCont.removeClass('spinner');
    }

    async loadEntitlementSettings(releaseChangedPayload) {
        if (!this.uiLoaded) {
            this.deferredLoadEntitlementSettings = _ => this.loadEntitlementSettings(releaseChangedPayload);
            return;
        } else this.deferredLoadEntitlementSettings = null;

        this.releaseId = null;

        let rows = jq(fodeRowSelector);

        rows.hide();
        this.hideMessages();
        this.isBsi = false;

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
            this.onScanCentralChanged();

            // ToDo: deal with overlapping calls
            let ssp = this.api.getReleaseEntitlementSettings(releaseId, getAuthInfo())
                .then(r => this.scanSettings = r);
            let entp = this.api.getAssessmentTypeEntitlements(releaseId, getAuthInfo())
                .then(r => this.assessments = r);

            await Promise.all([ssp, entp]);

            if (this.scanSettings && this.assessments) {
                let assessmentId = this.scanSettings.assessmentTypeId;
                let entitlementId = this.scanSettings.entitlementId;

                this.populateAssessmentsDropdown();

                jq('#ddAssessmentType').val(assessmentId);
                await this.onAssessmentChanged(true);
                jq('#entitlementSelectList').val(getEntitlementDropdownValue(entitlementId, this.scanSettings.entitlementFrequencyType));
                await this.onEntitlementChanged(false);

                let scval = this.getScanCentralBuildTypeSelected();

                if (scval !== _scanCentralBuildTypes.None) {
                    switch (this.scanSettings.technologyStackId) {
                        case techStackConsts.dotNet:
                        case techStackConsts.dotNetCore:
                            this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.MSBuild);
                            jq('#technologyStackSelectList').val(this.scanSettings.technologyStackId);
                            break;
                        case techStackConsts.java:
                            // Selected release is Java, but SC was not set to None, Maven, nor Gradle, default to Maven
                            if (scval !== _scanCentralBuildTypes.Maven && scval !== _scanCentralBuildTypes.Gradle) this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.Maven);
                            break;
                        case techStackConsts.php:
                            this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.PHP);
                            break;
                        case techStackConsts.go:
                            this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.Go);
                            break;
                        case techStackConsts.python:
                            this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.Python);
                            break;
                        default:
                            // It's a valid tech stack but not supported by SC
                            if (this.scanSettings.technologyStackId > 0) this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.None);
                            break;
                    }

                    this.onScanCentralChanged();
                }

                if (this.getScanCentralBuildTypeSelected() === _scanCentralBuildTypes.None) {
                    jq('#technologyStackSelectList').val(this.scanSettings.technologyStackId);
                    this.onTechStackChanged();
                    jq('#languageLevelSelectList').val(this.scanSettings.languageLevelId);
                    this.onLangLevelChanged();
                } else {
                    if (this.scanSettings.languageLevelId) jq('#languageLevelSelectList').val(this.scanSettings.languageLevelId);
                    this.onLangLevelChanged();
                }

                jq('#auditPreferenceSelectList').val(this.scanSettings.auditPreferenceType);
                jq('#cbSonatypeEnabled').prop('checked', this.scanSettings.performOpenSourceAnalysis === true);
            } else {
                await this.onAssessmentChanged(false);
                this.showMessage('Failed to retrieve scan settings from API', true);
                rows.hide();
            }
        } else {
            await this.onAssessmentChanged(false);
            if (releaseChangedPayload.mode === ReleaseSetMode.releaseSelect) this.showMessage('Select a release');
            else this.showMessage('Enter a release id');
        }

        fields.removeClass('spinner');
    }

    isDotNetStack(ts) {
        // noinspection EqualityComparisonWithCoercionJS
        return ts.value == techStackConsts.dotNet || ts.value == techStackConsts.dotNetCore;
    }

    onScanCentralChanged() {
        if (this.isBsi) return;
        let val = this.getScanCentralBuildTypeSelected();
        let techStackFilter;

        this.populateTechStackDropdown(null, null);

        if (val === _scanCentralBuildTypes.None) {
            jq('.fode-row-sc').hide();
            jq('.fode-row-nonsc').show();
            jq('#technologyStackSelectList').val(null);
            this.onTechStackChanged();
        } else {
            let scClass = 'fode-row-sc-' + val.toLowerCase();

            jq('.fode-row-nonsc').hide();

            jq('.fode-row-sc')
                .each((i, e) => {
                    let jqe = jq(e);

                    if (jqe.hasClass(scClass)) jqe.show();
                    else jqe.hide();
                });

            switch (val) {
                case _scanCentralBuildTypes.MSBuild:
                    closestRow(jq('#technologyStackForm')).show();
                    let currVal = this.techStacks[jq('#technologyStackSelectList').val()];

                    if (!currVal || !this.isDotNetStack(currVal)) jq('#technologyStackSelectList').val(techStackConsts.none);
                    techStackFilter = this.isDotNetStack;
                    break;
                case _scanCentralBuildTypes.Maven:
                case _scanCentralBuildTypes.Gradle:
                    jq('#technologyStackSelectList').val(techStackConsts.java);
                    break;
                case _scanCentralBuildTypes.PHP:
                    jq('#technologyStackSelectList').val(techStackConsts.php);
                    break;
                case _scanCentralBuildTypes.Go:
                    jq('#technologyStackSelectList').val(techStackConsts.go);
                    break;
                case _scanCentralBuildTypes.Python:
                    jq('#technologyStackSelectList').val(techStackConsts.python);
                    break;
            }
        }

        if (techStackFilter) this.populateTechStackDropdown(null, techStackFilter);

        this.onTechStackChanged(true);
    }

    populateTechStackDropdown(scanCentralBuildType, filter) {
        let tsSel = jq('#technologyStackSelectList');
        let currVal = tsSel.val();
        let currValSelected = false;

        tsSel.find('option').not(':first').remove();
        tsSel.find('option').first().prop('selected', true);


        for (let ts of this.techStacksSorted) {
            if (filter && filter(ts) !== true) continue;

            // noinspection EqualityComparisonWithCoercionJS
            if (currVal == ts.value) {
                currValSelected = true;
                tsSel.append(`<option value="${ts.value}" selected>${ts.text}</option>`);
            } else tsSel.append(`<option value="${ts.value}">${ts.text}</option>`);
        }

        if (!currValSelected) {
            tsSel.find('option').first().prop('selected', true);
            this.onTechStackChanged();
        }
    }

    onTechStackChanged(skipScanCentralCheck) {
        let ts = this.techStacks[jq('#technologyStackSelectList').val()];
        let llv = numberOrNull(jq('#languageLevelSelectList').val()) || -1;
        let llsel = jq('#languageLevelSelectList');
        let llr = jq('.fode-row-langLev');

        llr.show();
        llsel.find('option').not(':first').remove();
        llsel.find('option').first().prop('selected', true);
        jq('.fode-row-screc').hide();

        if (ts) {
            if (Array.isArray(ts.levels) && ts.levels.length > 0) {
                let setllv = false;

                for (let ll of ts.levels) {
                    if (ll.value == llv) setllv = true;
                    llsel.append(`<option value="${ll.value}">${ll.text}</option>`);
                }

                if (setllv) jq('#languageLevelSelectList').val(llv);
            } else llr.hide();

            if (this.techIdsWithOutOpenSourceSupport.includes(ts.value)) {
                jq('.fode-row-sonatype').hide();
            } else {
                jq('.fode-row-sonatype').show();
            }

            if (skipScanCentralCheck !== true) {
                let scbt = this.getScanCentralBuildTypeSelected();
                let tsv = numberOrNull(ts.value);

                if (tsv === techStackConsts.python) {
                    if (scbt !== _scanCentralBuildTypes.Python) {
                        this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.Python);
                        this.onScanCentralChanged();
                        return;
                    }
                }

            }

            if (this.getScanCentralBuildTypeSelected() === _scanCentralBuildTypes.None && _scanCentralRecommended.includes(numberOrNull(ts.value))) {
                jq('.fode-row-screc').show();
            }
        } else {
            jq('#technologyStackSelectList').val('-1');
        }

        this.onLangLevelChanged();
    }

    onLangLevelChanged() {
        // Todo: When you switch to Python, Version remembers previous value. Could be problem
        let bt = this.getScanCentralBuildTypeSelected();
        let ssv = jq('#buildToolVersionForm > input');

        if (bt === 'Python') {
            let ll = this.techStacks[techStackConsts.python].levels.find(e => e.value == jq('#languageLevelSelectList').val());

            if (ll && ll.text) ssv.val(ll.text.replace(' (Django)', ''));

            ssv.data('python', true);
        } else if (ssv.data('python')) {
            ssv.removeData();
            ssv.val('');
        }
    }

    getScanCentralBuildTypeSelected() {
        return _scanCentralBuildTypes[jq('#scanCentralBuildTypeForm select').val()] || _scanCentralBuildTypes.None;
    }

    setScanCentralBuildTypeSelected(val) {
        if (val && _scanCentralBuildTypes[val]) jq('#scanCentralBuildTypeForm select').val(val);
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

                        // Copy Scan Central and BSI css classes to validation-error-area and help-area rows
                        if (c.startsWith('fode-row-sc') || c === 'fode-row-nonsc' || c === 'fode-row-bsi' || c === 'fode-row-langLev') {
                            let vtr = getValidationErrRow(tr);
                            let htr = getHelpRow(tr);

                            if (vtr) vtr.addClass(c);
                            if (htr) htr.addClass(c);
                        }
                    }
                }
            });
        this.init();
    }

    async init() {
        try {
            this.techStacks = await this.api.getTechStacks(getAuthInfo());
            for (let k of Object.keys(this.techStacks)) {
                let ts = this.techStacks[k];

                // noinspection EqualityComparisonWithCoercionJS
                if (ts.value == techStackConsts.dotNetCore) {
                    for (let ll of ts.levels) {
                        ll.text = ll.text.replace(' (.NET Core)', '');
                    }
                }

                this.techStacksSorted.push(ts);
            }

            this.techStacksSorted = this.techStacksSorted.sort((a, b) => a.text.toLowerCase() < b.text.toLowerCase() ? -1 : 1);
        } catch (err) {
            // if (this.api.isAuthError(err)) {
            if (!this.unsubInit) {
                this.unsubInit = () => this.init();
                subscribeToEvent('authInfoChanged', this.unsubInit);
            }
            // } else {
            //     this.showMessage('Unhandled error, please reload page', true);
            // }
            return;
        }

        this.hideMessages();
        this.showMessage('Select a release');
        if (this.unsubInit) unsubscribeEvent('authInfoChanged', this.unsubInit);

        jq('#scanCentralBuildTypeForm select')
            .change(_ => this.onScanCentralChanged());

        jq('#technologyStackSelectList')
            .change(_ => this.onTechStackChanged());

        jq('#ddAssessmentType')
            .change(_ => this.onAssessmentChanged());

        jq('#entitlementSelectList')
            .change(_ => this.onEntitlementChanged());

        jq('#languageLevelSelectList')
            .change(_ => this.onLangLevelChanged());

        setTimeout(_ => this.onScanCentralChanged(), 50);

        jq('.fode-row-screc').hide();

        this.uiLoaded = true;
        if (this.deferredLoadEntitlementSettings) {
            this.deferredLoadEntitlementSettings();
            this.deferredLoadEntitlementSettings = null;
        }
    }

}

const scanSettings = new ScanSettings();

spinAndWait(() => jq('#sonatypeForm').val() !== undefined && jq('#scanCentralBuildTypeForm select').val())
    .then(scanSettings.preinit.bind(scanSettings));
