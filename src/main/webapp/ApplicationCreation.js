jq = jQuery;

class CreateApplicationForm extends Dialog {

    constructor() {
        super('createApplicationDialog', 'applicationCreationForm');
    }

    clearForm() {
        this.hideSpinner();
        this.jqDialog('#errors').html('');
        this.jqDialog('#applicationNameField').val('');
        this.jqDialog('#businessCriticalityField').val('1');
        this.jqDialog('#applicationTypeField').val('1');
        this.jqDialog('#applicationAttributesField').val('');
        this.jqDialog('#microserviceApplicationField').val(false);
        this.jqDialog('#microserviceNameField').val('');
        this.jqDialog('.microservice-fields').hide();
        this.jqDialog('#releaseNameField').val('');
        this.jqDialog('#sdlcStatusField').val('3');
        this.jqDialog('#ownerIdField').val('');
    }

    getFormObject() {
        return {
            applicationName: this.jqDialog('#applicationNameField').val(),
            businessCriticality: Number(this.jqDialog('#businessCriticalityField').val()),
            applicationType: Number(this.jqDialog('#applicationTypeField').val()),
            applicationAttributes: this.jqDialog('#applicationAttributesField').val(),
            hasMicroservices: this.jqDialog('#microserviceApplicationField').is(':checked'),
            microserviceName: this.jqDialog('#microserviceNameField').val(),
            releaseName: this.jqDialog('#releaseNameField').val(),
            sdlcStatus: Number(this.jqDialog('#sdlcStatusField').val()),
            ownerId: Number(this.jqDialog('#ownerIdField').val())
        };
    }

    subscribeToFormEvents() {
        this.jqDialog('#microserviceApplicationField').off('change').change(() => {

            const show = this.jqDialog('#microserviceApplicationField').is(':checked');
            if (show) {
                this.jqDialog('.microservice-fields').show();
            }
            else {
                this.jqDialog('.microservice-fields').hide();
            }
        });

        this.jqDialog('#submitBtn').off('click').click(() => {
            this.jqDialog('#errors').html('');
            this.putMask();
            this.showSpinner();

            const formObject = this.getFormObject();
            descriptor.submitCreateApplication(formObject, getAuthInfo(), t => {
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

                const payload = { applicationId: responseJson.value, ...formObject };
                dispatchEvent('applicationCreated', payload);
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
        jq('#createAppBtn').off('click').click(() => {
            this.spawnDialog();
        });
    }

    onDialogSpawn() {
        this.clearForm();
        this.subscribeToFormEvents();
    }
}

createDialog(new CreateApplicationForm());