class ReleaseCreationDialog extends Dialog {

    constructor() {
        super('createReleaseDialog', 'releaseCreationForm');
    }

    clearForm() {
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

    showErrors(errors) {
        let errorsHTML = '';
        for (const error of errors) {
            errorsHTML += '<li>' + error + '</li>';
        }
        this.jqDialog('#errors').html('<ul>' + errorsHTML + '</ul>');
    }

    subscribeToFormEvents(data) {
        this.jqDialog('#submitBtn').off('click').click(() => {
            this.jqDialog('#errors').html('');
            this.startSpinning();

            const formObject = this.getFormObject(data);
            descriptor.submitCreateRelease(formObject, getAuthInfo(), t => {
                this.stopSpinning();

                const responseJson = JSON.parse(t.responseJSON);
                if (!responseJson || (!responseJson.success && !responseJson.errors))
                    return this.showErrors(['Unexpected error. Please reload the page and try again']);
                if (!responseJson.success && responseJson.errors)
                    return this.showErrors(responseJson.errors);

                const payload = { releaseId: responseJson.value, ...formObject };
                dispatchEvent('releaseCreated', payload);
                this.closeDialog();
            });
        });

        this.jqDialog('#cancelBtn').off('click').click(() => {
            this.closeDialog();
        });
    }

    onInit() {
        jq('#createReleaseBtn').off('click').click((ev) => {
            ev.preventDefault();
            this.spawnDialog('Create New Release', {
                applicationId: Number(jq('[name="userSelectedApplication"]').val()),
                microserviceId: Number(jq('[name="userSelectedMicroservice"]').val())
            });
        });
    }

    onDialogSpawn(data) {
        this.clearForm();
        this.subscribeToFormEvents(data);
    }
}

createDialog(new ReleaseCreationDialog());