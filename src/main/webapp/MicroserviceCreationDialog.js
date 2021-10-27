class MicroserviceCreationDialog extends Dialog {

    constructor() {
        super('createMicroserviceDialog', 'microserviceCreationForm');
    }

    clearForm() {
        this.jqDialog('#errors').html('');
        this.jqDialog('#microserviceNameField').val('');
    }

    getFormObject(data) {
        return {
            applicationId: data.applicationId,
            microserviceName: this.jqDialog('#microserviceNameField').val()
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
            descriptor.submitCreateMicroservice(formObject, getAuthInfo(), t => {
                this.stopSpinning();

                const responseJson = JSON.parse(t.responseJSON);
                if (!responseJson || (!responseJson.success && !responseJson.errors))
                    return this.showErrors(['Unexpected error. Please reload the page and try again']);
                if (!responseJson.success && responseJson.errors)
                    return this.showErrors(responseJson.errors);

                const payload = { microserviceId: responseJson.value, ...formObject };
                dispatchEvent('microserviceCreated', payload);
                this.closeDialog();
            });
        });

        this.jqDialog('#cancelBtn').off('click').click(() => {
            this.closeDialog();
        });
    }

    onInit() {
        jq('#createMicroserviceBtn').off('click').click((ev) => {
            ev.preventDefault();
            this.spawnDialog('Create New Microservice', {
                applicationId: Number(jq('[name="userSelectedApplication"]').val())
            });
        });
    }

    onDialogSpawn(data) {
        this.clearForm();
        this.subscribeToFormEvents(data);
    }
}

createDialog(new MicroserviceCreationDialog());