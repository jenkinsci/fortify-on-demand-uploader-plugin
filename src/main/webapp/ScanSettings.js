jq = jQuery;


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

        jq('#releaseNotSelected').hide();
        jq('#fode-error').hide();

        let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
        let rows = jq('tr.fode-field-row');
        let fields = jq('.fode-field.spinner-container');

        releaseId = Number(releaseId);

        if (Number.isInteger(releaseId) && releaseId > 0) {
            rows.show();
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
            rows.hide();
        }

        fields.removeClass('spinner');
    }

    init() {
        jq('.fode-field').parent().parent().addClass('fode-field-row');
        jq('#cbOverrideRelease').parent().parent().parent().addClass('fode-field-row');
        jq('tr.fode-field-row').hide();
    }

}

const scanSettings = new ScanSettings();

spinAndWait(() => {
    scanSettings.uiLoaded = jq('#sonatypeForm').val() !== undefined;

    return scanSettings.uiLoaded;
}).then(scanSettings.init.bind(scanSettings));