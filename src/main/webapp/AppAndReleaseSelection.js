jq = jQuery;

const api = new Api(instance, descriptor);

function hideAll() {
    jq('.releaseIdView').hide();
    jq('.bsiTokenView').hide();
    jq('.appAndReleaseNameView').hide();
    jq('.appAndReleaseNameErrorView').hide();
}

function onCredsChanged() {
    const viewChoice = jq('#releaseTypeSelectList').val();

    if (viewChoice == "UseAppAndReleaseName") {
        catchAuthError(() => initAppAndReleaseSelection(true));
    }
}

function onReleaseMethodSelection() {
    hideAll();
    const viewChoice = jq('#releaseTypeSelectList').val();

    switch (viewChoice) {
        case "UseBsiToken":
            initBsiToken();
            break;
        case "UseReleaseId":
            initReleaseId();
            break;
        case "UseAppAndReleaseName":
            catchAuthError(() => initAppAndReleaseSelection(true));
            break;
    }
}

async function initBsiToken() {
    const savedBsiToken = await api.getSavedBsiToken();
    if (savedBsiToken) {
        jq('#bsiTokenField').val(savedBsiToken);
    }
    jq('.bsiTokenView').show();
}

async function initReleaseId() {
    const savedReleaseId = await api.getSavedReleaseId();
    if (savedReleaseId) {
        jq('#releaseIdField').val(savedReleaseId);
    }
    jq('.releaseIdView').show();
}

function resetSelectApplication() {
    jq('#selectedApp')[0].innerText = 'Select an application';
    jq('[name="userSelectedApplication"]').val(null);
}

function selectApplication(appId, appName) {
    jq('#selectedApp')[0].innerText = appName;
    jq('[name="userSelectedApplication"]').val(appId);
}

function resetSelectMicroservice() {
    jq('#selectedMicroservice')[0].innerText = 'Select a microservice';
    jq('[name="userSelectedMicroservice"]').val(null);
}

function selectMicroservice(microserviceId, microserviceName) {
    jq('#selectedMicroservice')[0].innerText = microserviceName;
    jq('[name="userSelectedMicroservice"]').val(microserviceId);
}

function resetSelectRelease() {
    jq('#selectedRelease')[0].innerText = 'Select a release';
    jq('[name="userSelectedRelease"]').val(null);
}

function selectRelease(releaseId, releaseName) {
    jq('#selectedRelease')[0].innerText = releaseName;
    jq('[name="userSelectedRelease"]').val(releaseId);
}

async function initAppAndReleaseSelection() {
    jq('.appAndReleaseNameView').show();
    jq('#appAndReleaseNameErrorView').hide();
    jq('#microserviceSelectView').hide();
    jq('#releaseSelectView').hide();

    showWithSpinner('#applicationSelectView');

    const savedReleaseId = await api.getSavedReleaseId();
    const [success, release] = await api.getReleaseById(savedReleaseId, getAuthInfo());

    hideSpinner('#applicationSelectView');

    if (success) {
        jq('#selectedApp')[0].innerText = release.applicationName;
        selectApplication(release.applicationId, release.applicationName);
        if (release.microserviceId) {
            jq('#microserviceSelectView').show();
            selectMicroservice(release.microserviceId, release.microserviceName);
        }
        jq('#releaseSelectView').show();
        selectRelease(release.releaseId, release.releaseName);
    }
    else {
        resetSelectApplication();
    }

}

async function catchAuthError(op) {
    try {
        await op();
    }
    catch(err) {
        if (api.isAuthError(err)) {
            showApiRetrievalError();
        }
        else {
            throw ex;
        }
    }
}

function showApiRetrievalError() {
    hideAll();
    jq('#appAndReleaseNameErrorView').show();
}

function init() {
    onReleaseMethodSelection();
    jq('#releaseTypeSelectList').off('change').change(onReleaseMethodSelection);

    subscribeToEvent('authInfoChanged', () => onCredsChanged());
}

spinAndWait(() => jq('#releaseTypeSelectList').val()).then(init);