const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';

class ScanSettings {

    constructor() {
        this.api = new Api(instance, descriptor);
        this.uiLoaded = false;
        this.techStacks = {};
        this.techStacksSorted = [];
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

    onAssessmentChanged() {
        let atval = jq('#ddAssessmentType').val();
        let entsel = jq('#entitlementSelectList');
        let apsel = jq('#auditPreferenceSelectList');
        let at = this.assessments[atval];

        entsel.find('option,optgroup').remove();

        if (at) {
            let available = at.entitlementsSorted.filter(e => e.id > 0);
            let forPurchase = at.entitlementsSorted.filter(e => e.id <= 0);
            let availableGrp = forPurchase.length > 0 ? jq(`<optgroup label="Available Entitlements"></optgroup>`) : entsel;

            for (let e of available) {
                availableGrp.append(`<option value="${getEntitlementDropdownValue(e.id, e.frequencyId)}">${e.description}</option>`);
            }

            if (forPurchase.length > 0){
                let grp = jq(`<optgroup label="Available For Purchase"></optgroup>`);

                entsel.append(availableGrp);
                entsel.append(grp);
                for (let e of forPurchase) {
                    grp.append(`<option value="${getEntitlementDropdownValue(0, e.frequencyId)}">${e.description}</option>`);
                }
            }
        }

        if (at && at.name === 'Static+ Assessment') apsel.prop('disabled', false);
        else {
            apsel.val('2');
            apsel.prop('disabled', true);
        }

        this.onEntitlementChanged();
        // ToDo: set to unselected if selected value doesn't exist
    }

    onEntitlementChanged() {
        let val = jq('#entitlementSelectList').val();
        let {entitlementId, frequencyId} = parseEntitlementDropdownValue(val);

        jq('#entitlementId').val(entitlementId);
        jq('#frequencyId').val(frequencyId);
        jq('#purchaseEntitlementsForm input').prop('checked', (entitlementId <=  0));
    }

    async loadEntitlementSettings(releaseChangedPayload) {
        if (!this.uiLoaded) {
            this.deferredLoadEntitlementSettings = _ => this.loadEntitlementSettings(releaseChangedPayload);
            return;
        } else this.deferredLoadEntitlementSettings = null;

        let rows = jq(fodeRowSelector);

        rows.hide();
        this.hideMessages();

        if (releaseChangedPayload && releaseChangedPayload.mode === ReleaseSetMode.bsiToken) {
            jq('.fode-row-bsi').show();
            jq('.fode-row-remediation').show();
            return;
        }

        let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
        let fields = jq('.fode-field.spinner-container');

        releaseId = numberOrNull(releaseId);

        if (releaseId > 0) {
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
                this.onAssessmentChanged();
                jq('#entitlementSelectList').val(getEntitlementDropdownValue(entitlementId, this.scanSettings.entitlementFrequencyType));
                this.onEntitlementChanged();
                jq('#technologyStackSelectList').val(this.scanSettings.technologyStackId);
                this.onTechStackChanged();
                jq('#languageLevelSelectList').val(this.scanSettings.languageLevelId);
                jq('#auditPreferenceSelectList').val(this.scanSettings.auditPreferenceType);
                jq('#cbSonatypeEnabled').prop('checked', this.scanSettings.performOpenSourceAnalysis === true);

            } else {
                this.onAssessmentChanged();
                this.showMessage('Failed to retrieve scan settings from API', true);
                rows.hide();
            }
        } else {
            this.onAssessmentChanged();
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
        let val = jq('#scanCentralBuildTypeForm > select').val().toLowerCase();
        let techStackFilter;

        if (val === 'none') {
            jq('.fode-row-sc').hide();
            jq('.fode-row-nonsc').show();
        } else {
            let scClass = 'fode-row-sc-' + val;

            jq('.fode-row-nonsc').hide();

            jq('.fode-row-sc')
                .each((i, e) => {
                    let jqe = jq(e);

                    if (jqe.hasClass(scClass)) jqe.show();
                    else jqe.hide();
                });

            switch (val) {
                case 'msbuild':
                    closestRow(jq('#technologyStackForm')).show();
                    let currVal = this.techStacks[jq('#technologyStackSelectList').val()];

                    if (!currVal || !this.isDotNetStack(currVal)) jq('#technologyStackSelectList').val(techStackConsts.none);
                    techStackFilter = this.isDotNetStack;
                    break;
                case 'maven':
                case 'gradle':
                    jq('#technologyStackSelectList').val(techStackConsts.java);
                    break;
                case 'php':
                    jq('#technologyStackSelectList').val(techStackConsts.php);
                    break;
                case 'python':
                    jq('#technologyStackSelectList').val(techStackConsts.python);

                    break;
            }
        }

        this.populateTechStackDropdown(techStackFilter);
        this.onTechStackChanged();
    }

    populateTechStackDropdown(filter) {
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

    onTechStackChanged() {
        let ts = this.techStacks[jq('#technologyStackSelectList').val()];
        let llsel = jq('#languageLevelSelectList');
        let llr = jq('.fode-row-langLev');

        llr.show();
        llsel.find('option').not(':first').remove();
        llsel.find('option').first().prop('selected', true);

        // noinspection EqualityComparisonWithCoercionJS
        if (ts && ts.value == techStackConsts.php) llr.hide();
        else if (ts) {
            for (let ll of ts.levels) {
                llsel.append(`<option value="${ll.value}">${ll.text}</option>`);
            }
        }

        this.onLangLevelChanged();
    }

    onLangLevelChanged(){
        // Todo: When you switch to Python, Version remembers previous value. Could be problem
        let bt = jq('#scanCentralBuildTypeForm > select').val();
        let ssv = jq('#buildToolVersionForm > input');

        if (bt === 'Python'){
            let ll = this.techStacks[techStackConsts.python].levels.find(e => e.value == jq('#languageLevelSelectList').val());

            if (ll && ll.text) ssv.val(ll.text.replace(' (Django)', ''));

            ssv.data('python', true);
        } else if (ssv.data('python')) {
            ssv.removeData();
            ssv.val('');
        }
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

        jq('#scanCentralBuildTypeForm > select')
            .change(_ => this.onScanCentralChanged());

        jq('#technologyStackSelectList')
            .change(_ => this.onTechStackChanged());

        jq('#ddAssessmentType')
            .change(_ => this.onAssessmentChanged());

        jq('#entitlementSelectList')
            .change(_ => this.onEntitlementChanged());

        jq('#languageLevelSelectList')
            .change(_ => this.onLangLevelChanged());

        this.populateTechStackDropdown();

        this.uiLoaded = true;
        if (this.deferredLoadEntitlementSettings) {
            this.deferredLoadEntitlementSettings();
            this.deferredLoadEntitlementSettings = null;
        }
    }

}

const scanSettings = new ScanSettings();

spinAndWait(() => jq('#sonatypeForm').val() !== undefined && jq('#scanCentralBuildTypeForm > select').val())
    .then(scanSettings.preinit.bind(scanSettings));