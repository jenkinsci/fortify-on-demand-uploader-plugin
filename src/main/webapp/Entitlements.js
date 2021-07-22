jq = jQuery;

const _jobSettings = new JobSettings(instance);

// let _lastRequestId = null;

function setFieldValue(id, value) {
    let selText = '';

    jq(`#${id} .fode-edit > select`).val(value);

    if (value > 0) selText = jq(`#${id} .fode-edit > select > option:selected`).text();

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
            setFieldValue('assessmentTypeForm', r.assessmentTypeForm);
            setFieldValue('entitlementForm', r.entitlementForm);
            setFieldValue('technologyStackForm', r.technologyStackForm);
            setFieldValue('languageLevelForm', r.languageLevelForm);
            setFieldValue('auditPreferenceForm', r.auditPreferenceForm);
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
    let checkboxes = jq('.fode-checkbox');

    jq('#cbOverrideRelease')
        .change(e => {
            let override = jq(e.target).val() === 'true';

            if (override) {
                viewDivs.hide();
                editDivs.show();
                checkboxes.prop('disable', false);
            } else {
                viewDivs.show();
                editDivs.hide();
                checkboxes.prop('disable', true);
            }
        });

}

spinAndWait(() => {
    return jq('#cbOverrideRelease').val() && // override checkbox
        jq('#releaseSelectList').val() && // AppAndReleaseSelection
        jq('#bsiTokenField').val() && // BSI Token
        jq('#releaseIdField').val() // Release Id
}).then(initEntitlements);