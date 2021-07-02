jq = jQuery;

const jobSettings = new JobSettings(instance);

function hideAll() {
    jq('#releaseIdView').hide();
    jq('#bsiTokenView').hide();
    jq('#appAndReleaseNameView').hide();
}

function onReleaseMethodSelection() {
    hideAll();
    const viewChoice = jq('#releaseTypeSelectList').val();

    switch (viewChoice) {
        case "UseBsiToken":
            jq('#bsiTokenView').show();
            break;
        case "UseReleaseId":
            jq('#releaseIdView').show();
            break;
        case "UseAppAndReleaseName":
            initAppSelection();
            break;
    }
}

function initAppSelection() {
    jq('#appAndReleaseNameView').show();
    jq('#microserviceSelectForm').hide();
    jq('#releaseSelectForm').hide();

    showWithSpinner('#applicationSelectForm');


    descriptor.retrieveApplicationList(async t => {
        const applicationSelection = jq('#applicationSelectList');
        const responseJson = JSON.parse(t.responseJSON);
        applicationSelection.empty();

        for(const app of responseJson) {
            applicationSelection.append('<option hasMicroServices="' + app.hasMicroservices + '" value="' + app.applicationId + '">' + app.applicationName + '</option>');
        }

        const savedAppId = await jobSettings.getSavedApplicationId();
        if (savedAppId) {
            jq('#applicationSelectList').val(savedAppId);
        }

        onAppSelection();
        hideSpinner('#applicationSelectForm');
        jq('#applicationSelectList').off('change').change(() => onAppSelection());
    });
}

function onAppSelection() {
    jq('#microserviceSelectForm').hide();
    jq('#releaseSelectForm').hide();

    const hasMicroservices = jq('#applicationSelectList option:selected').attr('hasMicroServices') === 'true';
    if (hasMicroservices) {
        initMicroserviceSelection();
    }
    else {
        initReleaseSelection();
    }
}

function initMicroserviceSelection() {
    jq('#releaseSelectForm').hide();
    showWithSpinner('#microserviceSelectForm');

    const appId = jq('#applicationSelectList').val();
    descriptor.retrieveMicroserviceList(appId, async t => {
        const microserviceSelection = jq('#microserviceSelectList');
        const responseJson = JSON.parse(t.responseJSON);
        microserviceSelection.empty();

        for (const ms of responseJson) {
            microserviceSelection.append('<option value="' + ms.microserviceId + '">' + ms.microserviceName + '</option>');
        }

        const savedMicroserviceId = await jobSettings.getSavedMicroserviceId();
        if (savedMicroserviceId) {
            jq('#microserviceSelectList').val(savedMicroserviceId);
        }

        hideSpinner('#microserviceSelectForm');

        onMicroserviceSelection();
        jq('#microserviceSelectList').off('change').change(onMicroserviceSelection);
    });
}

function onMicroserviceSelection() {
    initReleaseSelection();
}

function initReleaseSelection() {
    showWithSpinner('#releaseSelectForm');

    const appId = jq('#applicationSelectList').val();
    const hasMicroservices = jq('#applicationSelectList option:selected').attr('hasMicroServices') === 'true';

    const microserviceId = !hasMicroservices ? -1 : jq('#microserviceSelectList').val();

    descriptor.retrieveReleaseList(appId, microserviceId, async t => {
        const releaseSelection = jq('#releaseSelectList');
        const responseJson = JSON.parse(t.responseJSON);
        releaseSelection.empty();

        for (const release of responseJson) {
            releaseSelection.append('<option value="' + release.releaseId + '">' + release.releaseName + '</option>');
        }

        const savedReleaseId = await jobSettings.getSavedReleaseId();
        if (savedReleaseId) {
            jq('#releaseSelectList').val(savedReleaseId);
        }

        hideSpinner('#releaseSelectForm');
    });
}

function init() {
    onReleaseMethodSelection();
    jq('#releaseTypeSelectList').off('change').change(onReleaseMethodSelection);
}

function waitForReleaseMethodToInit() {
    return new Promise((res, rej) => {
        let elementsLoaded;

        elementsLoaded = () => {
            if(jq('#releaseTypeSelectList').val()) res();
            else setTimeout(elementsLoaded, 50);
        };

        elementsLoaded();
    });
}

waitForReleaseMethodToInit().then(init);