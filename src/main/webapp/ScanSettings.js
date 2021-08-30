jq = jQuery;

const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';
const techStackConsts = {
    none: -1,
    dotNet: 1,
    dotNetCore: 23,
    java: 7,
    php: 9,
    python: 10
};

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

    setSelectValues(id, selected, options) {
        let select = jq(`#${id} .fode-edit > select`);

        if (options) {
            select.find('option[value]:not([value=""])').remove();
            for (let o of options) {
                select.append(`<option value="${o.value}">${o.text}</option>`);
            }
        }

        select.val(selected);
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
        let atval = jq(`#ddAssessmentType`).val();
        let entsel = jq(`#entitlementSelectList`);
        let at = this.assessments[atval];

        entsel.find('option').remove();

        if (at) {
            for (let e of at.entitlementsSorted) {
                entsel.append(`<option value="${this.getEntitlementDropdownValue(e.id, e.frequencyId)}">${e.description}</option>`);
            }
        }
        this.onEntitlementChanged();
        // ToDo: set to unselected if selected value doesn't exist
    }

    onEntitlementChanged() {
        let entId = '';
        let freqId = '';
        let val = jq('#entitlementSelectList').val();

        if (val) {
            let spl = val.split('-');

            if (spl.length === 2){
                entId = spl[0];
                freqId = spl[1];
            }
        }

        jq('#entitlementId').val(entId);
        jq('#frequencyId').val(freqId);
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
            this.showMessage('Settings defined in BSI Token');
            return;
        }

        let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
        let fields = jq('.fode-field.spinner-container');

        releaseId = Number(releaseId);

        if (Number.isInteger(releaseId) && releaseId > 0) {
            fields.addClass('spinner');
            rows.show();
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
                jq('#entitlementSelectList').val(this.getEntitlementDropdownValue(entitlementId, this.scanSettings.entitlementFrequencyType));
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

            let techStackFilter;

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

            this.populateTechStackDropdown(techStackFilter)
            this.onTechStackChanged();
        }
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

    getEntitlementDropdownValue(id, freq) {
        return `${id}-${freq}`;
    }

    onTechStackChanged() {
        let ts = this.techStacks[jq('#technologyStackSelectList').val()];
        let llsel = jq('#languageLevelSelectList');

        llsel.find('option').not(':first').remove();
        llsel.find('option').first().prop('selected', true);

        if (!ts) return;

        for (let ll of ts.levels) {
            llsel.append(`<option value="${ll.value}">${ll.text}</option>`);
        }
    }

    async init() {
        try {
            this.techStacks = await this.api.getTechStacks(getAuthInfo());
            for (let k of Object.keys(this.techStacks)) {
                let ts = this.techStacks[k];

                // noinspection EqualityComparisonWithCoercionJS
                if (ts.value == techStackConsts.dotNetCore) {
                    for (let ll of ts.levels) {
                        ll.text = ll.text.replace(' (.NET Core)','');
                    }
                }

                this.techStacksSorted.push(ts);
            }

            this.techStacksSorted = this.techStacksSorted.sort((a, b) => a.text.toLowerCase() < b.text.toLowerCase() ? -1 : 1);
        } catch (err) {
            if (this.api.isAuthError(err)) {
                this.unsubInit = () => this.init();
                subscribeToEvent('authInfoChanged', this.unsubInit);
            } else {
                this.showMessage('Unhandled error, please reload page', true);
            }
            return;
        }

        this.hideMessages();
        this.showMessage('Select a release');
        if (this.unsubInit) unsubscribeEvent('authInfoChanged', this.unsubInit);

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

                        // Copy Scan Central css classes to validation-error-area rows
                        if (c.startsWith('fode-row-sc') || c === 'fode-row-nonsc') {
                            let vtr = getValidationErrRow(tr);

                            if (vtr) vtr.addClass(c);
                        }
                    }
                }
            });

        jq('#scanCentralBuildTypeForm > select')
            .change(_ => this.onScanCentralChanged());

        jq('#technologyStackSelectList')
            .change(_ => this.onTechStackChanged());

        jq('#ddAssessmentType')
            .change(_ => this.onAssessmentChanged());

        jq('#entitlementSelectList')
            .change(_ => this.onEntitlementChanged());

        this.populateTechStackDropdown();

        this.uiLoaded = true;
        if (this.deferredLoadEntitlementSettings) this.deferredLoadEntitlementSettings();
    }

}

const scanSettings = new ScanSettings();

spinAndWait(() => jq('#sonatypeForm').val() !== undefined && jq('#scanCentralBuildTypeForm > select').val())
    .then(scanSettings.init.bind(scanSettings));