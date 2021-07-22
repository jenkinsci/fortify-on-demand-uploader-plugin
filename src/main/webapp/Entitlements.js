jq = jQuery;

const _jobSettings = new JobSettings(instance);

// let _lastRequestId = null;

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

function loadEntitlementSettings(releaseId) {
    let fields = jq('.fode-field.spinner-container');

    releaseId = Number(releaseId);

    // _lastRequestId = newGuid();

    if (Number.isInteger(releaseId)) {
        jq('#releaseNotSelected').hide();

        fields.show();
        fields.addClass('spinner');

        descriptor.getReleaseEntitlementSettings(releaseId, getAuthInfo(), async t => {
            let r = JSON.parse(t.responseJSON);

            // if (_lastRequestId === r.requestId) {
            setSelectValues('assessmentTypeForm', r.assessmentType, r.assessmentTypes);
            setSelectValues('entitlementForm', r.entitlement, r.entitlements);
            setSelectValues('technologyStackForm', r.technologyStack, null);
            setSelectValues('languageLevelForm', r.languageLevel, null);
            setSelectValues('auditPreferenceForm', r.auditPreference, r.auditPreferences);
            jq('#sonatypeForm').prop('checked', r.sonatypeScan === true);

            jq('.fode-field.spinner-container').removeClass('spinner');
            jq('#cbOverrideRelease').val(false);
            // }
        });
    } else {
        fields.removeClass('spinner');
        fields.hide();
        jq('#releaseNotSelected').show();
    }
}

function initEntitlements() {
    jq('#releaseSelectList')
        .change(e => loadEntitlementSettings(jq(e.target).val())); // is the val the id?

    jq('#bsiTokenField')
        .change(e => loadEntitlementSettings(null)); // parse bsi

    jq('#releaseIdField')
        .change(e => loadEntitlementSettings(jq(e.target).val()));


    let viewDivs = jq('.fode-field .fode-view');
    let editDivs = jq('.fode-field .fode-edit');
    let checkboxes = jq('.fode-checkbox input');

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

    let releaseId = -1;

    switch (jq('#releaseTypeSelectList').val()) {
        case 'UseAppAndReleaseName':
            releaseId = jq('#releaseSelectList').val();
            break;
        case 'UseBsiToken':
            // jq('#bsiTokenField');
            break;
        case 'UseReleaseId':
            releaseId = jq('#releaseIdField').val();
            break;
    }

    loadEntitlementSettings(releaseId);
}

function elementLoaded(id) {
    return jq('#' + id).val() !== undefined;
}

spinAndWait(() => {
    return elementLoaded('cbOverrideRelease') && // override checkbox
        elementLoaded('releaseSelectList') && // AppAndReleaseSelection
        elementLoaded('bsiTokenField') && // BSI Token
        elementLoaded('releaseIdField') // Release Id
}).then(initEntitlements);