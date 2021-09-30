class ApplicationCreationDialog extends Dialog {

    constructor() {
        super('createApplicationDialog', 'applicationCreationForm');
        this.api = new Api(instance, descriptor);
        this.currentUserId = null;
    }

    clearForm() {
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

        if (!this.currentUserId) {
            this.jqDialog('#assignCurrentUserSection').hide();
        }
        else {
            this.jqDialog('#assignCurrentUserSection').show();
        }
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

    showErrors(errors) {
        let errorsHTML = '';
        for (const error of errors) {
            errorsHTML += '<li>' + error + '</li>';
        }
        this.jqDialog('#errors').html('<ul>' + errorsHTML + '</ul>');
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

        this.jqDialog('#assignCurrentUserLink').off('click').click((ev) => {
            ev.preventDefault();
            if (this.currentUserId) {
                this.jqDialog('#ownerIdField').val(this.currentUserId);
            }
        });

        this.jqDialog('#submitBtn').off('click').click(() => {
            this.jqDialog('#errors').html('');
            this.startSpinning();

            const formObject = this.getFormObject();
            descriptor.submitCreateApplication(formObject, getAuthInfo(), async t => {
                const responseJson = JSON.parse(t.responseJSON);
                if (!responseJson || (!responseJson.success && !responseJson.errors)) {
                    this.showErrors(['Unexpected error. Please reload the page and try again']);
                    return this.stopSpinning();
                }
                if (!responseJson.success && responseJson.errors) {
                    this.showErrors(responseJson.errors);
                    return this.stopSpinning();
                }

                const applicationId = responseJson.value.applicationId;
                let releaseId = responseJson.value.releaseId;
                let microserviceId = responseJson.value.microserviceId;

                try {
                    if (formObject.hasMicroservices && (!microserviceId || microserviceId <= 0)) {
                        const microservices = await this.api.getMicroservices(applicationId, getAuthInfo());
                        microserviceId = microservices[0].microserviceId;
                    }

                    if (!releaseId || releaseId <= 0) {
                        const releases = await this.api.getReleases(applicationId, microserviceId > 0 ? microserviceId : null, {}, getAuthInfo());
                        releaseId = releases.items[0].releaseId;
                    }
                }
                catch (e) {
                    console.error(e);
                    return this.showErrors(['Application was created, but encountered an error. Please refresh']);
                }
                finally {
                    this.stopSpinning();
                }

                const payload = { applicationId, microserviceId, releaseId, ...formObject };
                dispatchEvent('applicationCreated', payload);
                this.closeDialog();
            });
        });

        this.jqDialog('#cancelBtn').off('click').click(() => {
            this.closeDialog();
        });
    }

    onUserDetected(userId) {
        this.currentUserId = userId;
    }

    onInit() {
        jq('#createAppBtn').off('click').click((ev) => {
            ev.preventDefault();
            this.spawnDialog('Create New Application');
        });

        subscribeToEvent('userDetected', e => this.onUserDetected(e.detail.userId));
    }

    onDialogSpawn() {
        this.clearForm();
        this.subscribeToFormEvents();
    }
}

createDialog(new ApplicationCreationDialog());