function getAuthInfo() {
    return {
        overrideGlobalAuth: jq('[name="overrideGlobalConfig"]').is(':checked'),
        username: jq('#usernameField').val(),
        accessTokenKey: jq('[name="_.personalAccessToken"]').val() || jq('[name="_.personalAccessTokenSelect"]').val(),
        tenantId: jq('#tenantIdField').val()
    };
}

function sendChangedEvent() {
    debounce(() => dispatchEvent('authInfoChanged'), 500)();
}

function init() {
    jq('[name="overrideGlobalConfig"]').off('change').change(() => sendChangedEvent());
    jq('#usernameField').off('change').change(() => sendChangedEvent());
    jq('[name="_.personalAccessToken"]').off('change').change(() => sendChangedEvent());
    jq('#tenantIdField').off('change').change(() => sendChangedEvent());
}

spinAndWait(() => jq('[name="overrideGlobalConfig"]').val()).then(init);