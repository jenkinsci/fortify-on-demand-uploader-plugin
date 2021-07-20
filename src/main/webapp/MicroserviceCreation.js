jq = jQuery;

class CreateMicroserviceForm extends Dialog {

    constructor() {
        super('createMicroserviceDialog', 'microserviceCreationForm');
    }

    clearForm() {
        this.hideSpinner();
        this.jqDialog('#errors').html('');
        this.jqDialog('#microserviceNameField').val('');
    }

    getFormObject(data) {
        return {
            applicationId: data.applicationId,
            microserviceName: this.jqDialog('#microserviceNameField').val()
        };
    }

    subscribeToFormEvents(data) {
        this.jqDialog('#submitBtn').off('click').click(() => {
            this.jqDialog('#errors').html('');
            this.putMask();
            this.showSpinner();

            const formObject = this.getFormObject(data);
            descriptor.submitCreateMicroservice(formObject, getAuthInfo(), t => {
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

                const payload = { microserviceId: responseJson.value, ...formObject };
                dispatchEvent('microserviceCreated', payload);
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
        jq('#createMicroserviceBtn').off('click').click(() => {
            this.spawnDialog({
                applicationId: Number(jq('#applicationSelectList').val())
            });
        });
    }

    onDialogSpawn(data) {
        this.clearForm();
        this.subscribeToFormEvents(data);
    }
}

createDialog(new CreateMicroserviceForm());