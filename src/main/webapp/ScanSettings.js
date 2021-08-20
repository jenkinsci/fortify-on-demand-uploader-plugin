jq = jQuery;

const fodeRowSelector = '.fode-field-row, .fode-field-row-verr';

class ScanSettings {

    constructor() {
        this.api = new Api(instance, descriptor);
        this.uiLoaded = false;
        subscribeToEvent('releaseChanged', p => this.loadEntitlementSettings(p.detail));
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

    async loadEntitlementSettings(releaseChangedPayload) {
        if (!this.uiLoaded) {
            setTimeout(_ => this.loadEntitlementSettings(releaseChangedPayload), 500);
            return;
        }

        let rows = jq(fodeRowSelector);

        jq('#releaseNotSelected').hide();
        jq('#releaseInBsi').hide();
        jq('#fode-error').hide();
        rows.hide();

        if (releaseChangedPayload && releaseChangedPayload.mode === ReleaseSetMode.bsiToken) {
            jq('#releaseInBsi').show();
            return;
        }

        let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
        let fields = jq('.fode-field.spinner-container');

        releaseId = Number(releaseId);

        if (Number.isInteger(releaseId) && releaseId > 0) {
            rows.show();
            this.setScanCentralVisibility();
            fields.addClass('spinner');

            let r = await this.api.getReleaseEntitlementSettings(releaseId, getAuthInfo());

            if (r) {
                this.setSelectValues('assessmentTypeForm', r.assessmentType, r.assessmentTypes);
                this.setSelectValues('entitlementForm', r.entitlement, r.entitlements);
                this.setSelectValues('technologyStackForm', r.technologyStack, null);
                this.setSelectValues('languageLevelForm', r.languageLevel, null);
                this.setSelectValues('auditPreferenceForm', r.auditPreference, r.auditPreferences);
                jq('#sonatypeForm').prop('checked', r.sonatypeScan === true);

                jq('#cbOverrideRelease').val(false);
            } else {
                // ToDo: Write some useful error message
                jq('#fode-error').show();
                rows.hide();
            }
        } else {
            jq('#releaseNotSelected').show();
        }

        fields.removeClass('spinner');
    }

    getValidationErrRow(row) {
        let vtr = nextRow(row);

        if (vtr.length > 0 && vtr.hasClass('validation-error-area')) return vtr;

        return null;
    }

    setScanCentralVisibility() {
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
        }
    }

    init() {
        jq('.fode-field')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.addClass('fode-field-row');
                let vtr = this.getValidationErrRow(tr);

                if (vtr) vtr.addClass('fode-field-row-verr');
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
                            let vtr = this.getValidationErrRow(tr);

                            if (vtr) vtr.addClass(c);
                        }
                    }
                }
            });

        jq('#scanCentralBuildTypeForm > select')
            .change(_ => this.setScanCentralVisibility());

        this.setScanCentralVisibility();
    }

}

const scanSettings = new ScanSettings();

spinAndWait(() => {
    scanSettings.uiLoaded = jq('#sonatypeForm').val() !== undefined && jq('#scanCentralBuildTypeForm > select').val();

    return scanSettings.uiLoaded;
}).then(scanSettings.init.bind(scanSettings));