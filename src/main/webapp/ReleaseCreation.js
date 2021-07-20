jq = jQuery;

class CreateReleaseForm extends Dialog {

    constructor() {
        super('createReleaseDialog', 'releaseCreationForm');
    }

    clearForm() {
        this.hideSpinner();
        this.jqDialog('#errors').html('');
        this.jqDialog('#releaseNameField').val('');
        this.jqDialog('#sdlcStatusField').val('3');
    }

    getFormObject(data) {
        return {
            applicationId: data.applicationId,
            microserviceId: data.microserviceId,
            releaseName: this.jqDialog('#releaseNameField').val(),
            sdlcStatus: Number(this.jqDialog('#sdlcStatusField').val())
        };
    }

    subscribeToFormEvents(data) {
        this.jqDialog('#submitBtn').off('click').click(() => {
            this.jqDialog('#errors').html('');
            this.putMask();
            this.showSpinner();

            const formObject = this.getFormObject(data);
            descriptor.submitCreateRelease(formObject, getAuthInfo(), t => {
                this.hideSpinner();
                this.removeMask();

                const responseJson = JSON.parse(t.responseJSON);
                if (!responseJson) return;
                if (!responseJson.success) {
                    let errorsHTML = '';
                    for (const error of responseJson.errors) {
                        errorsHTML += '<li>' + error + '</li>';
                    }
                    this.jqDialog('#errors').html('<ul>' + errorsHTML + '</ul>');
                    return;
                }

                const payload = { releaseId: responseJson.value, ...formObject };
                dispatchEvent('releaseCreated', payload);
                this.closeDialog();
            });
        });

        this.jqDialog('#cancelBtn').off('click').click(() => {
            this.closeDialog();
        });
    }

    showSpinner() {
        this.jqDialog('#spinner-container').addClass('spinner');
    }

    hideSpinner() {
        this.jqDialog('#spinner-container').removeClass('spinner');
    }

    onInit() {
        jq('#createReleaseBtn').off('click').click(() => {
            this.spawnDialog({
                applicationId: Number(jq('#applicationSelectList').val()),
                microserviceId: jq('#microserviceSelectList').is(':visible') ? Number(jq('#microserviceSelectList').val()) : null
            });
        });
    }

    onDialogSpawn(data) {
        this.clearForm();
        this.subscribeToFormEvents(data);
    }
}

createDialog(new CreateReleaseForm());