jq = jQuery;

const _api = new Api(instance, descriptor);

let _uiLoaded = false;

function setSelectValues(id, selected, options) {
    let select = jq(`#${id} .fode-edit > select`);

    if (options) {
        select.find('option[value]:not([value=""])').remove();
        for (let o of options) {
            select.append(`<option value="${o.value}">${o.text}</option>`);
        }
    }

    let selText = '';

    select.val(selected);

    if (selected > 0) selText = jq(`#${id} .fode-edit > select > option:selected`).text();

    jq(`#${id} .fode-view > div`).text(selText);
}

async function loadEntitlementSettings(releaseChangedPayload) {
    if (!_uiLoaded) {
        setTimeout(loadEntitlementSettings, 500);
        return;
    }

    jq('#releaseNotSelected').hide();
    jq('#fode-error').hide();

    let releaseId = releaseChangedPayload ? releaseChangedPayload.releaseId : null;
    let rows = jq('tr.fode-field-row');
    let fields = jq('.fode-field.spinner-container');

    releaseId = Number(releaseId);
    let success = false;

    if (Number.isInteger(releaseId) && releaseId > 0) {
        rows.show();
        fields.addClass('spinner');

        let r = await _api.getReleaseEntitlementSettings(releaseId, getAuthInfo());

        if (r) {
            setSelectValues('assessmentTypeForm', r.assessmentType, r.assessmentTypes);
            setSelectValues('entitlementForm', r.entitlement, r.entitlements);
            setSelectValues('technologyStackForm', r.technologyStack, null);
            setSelectValues('languageLevelForm', r.languageLevel, null);
            setSelectValues('auditPreferenceForm', r.auditPreference, r.auditPreferences);
            jq('#sonatypeForm').prop('checked', r.sonatypeScan === true);

            jq('#cbOverrideRelease').val(false);
            success = true;
        }
        else jq('#fode-error').show();
    } else jq('#releaseNotSelected').show();

    fields.removeClass('spinner');
    if (!success) rows.hide();
}

function initEntitlements() {
    jq('.fode-field').parent().parent().addClass('fode-field-row');
    jq('#cbOverrideRelease').parent().parent().parent().addClass('fode-field-row');

    let viewDivs = jq('.fode-field .fode-view');
    let editDivs = jq('.fode-field .fode-edit');
    let checkboxes = jq('.fode-checkbox input');

    jq('tr.fode-field-row').hide();
    viewDivs.show();
    editDivs.hide();
    checkboxes.prop('disabled', true);

    jq('#cbOverrideRelease')
        .click(e => {
            debugger;

            if (jq(e.target).prop('checked')) {
                viewDivs.hide();
                editDivs.show();
                checkboxes.prop('disabled', false);
            } else {
                viewDivs.show();
                editDivs.hide();
                checkboxes.prop('disabled', true);
            }
        });
}

function elementLoaded(id) {
    return jq('#' + id).val() !== undefined;
}

subscribeToEvent('releaseChanged', p => loadEntitlementSettings(p.detail));

spinAndWait(() => {
    _uiLoaded = elementLoaded('cbOverrideRelease');

    return _uiLoaded;
}).then(initEntitlements);