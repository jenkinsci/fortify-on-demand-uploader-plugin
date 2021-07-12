jq = jQuery;

window.createApplication = window.createApplication || {'dialog': null, 'body': null};
window.createApplication.init = function () {
    if (!(window.createApplication.dialog)) {
        var div = document.createElement("DIV");
        document.body.appendChild(div);
        div.innerHTML = "<div id='createApplicationDialog'><div class='bd'></div></div>";
        window.createApplication.body = $('createApplicationDialog');
        window.createApplication.body.innerHTML = $('applicationCreationForm').innerHTML;
        window.createApplication.dialog = new YAHOO.widget.Panel(window.createApplication.body, {
            fixedcenter: true,
            close: true,
            draggable: true,
            zindex: 1000,
            modal: true,
            visible: false,
            keylisteners: [
                new YAHOO.util.KeyListener(document, {keys:27}, {
                    fn:(function() {window.createApplication.dialog.hide();}),
                    scope:document,
                    correctScope:false
                })
            ]
        });
        window.createApplication.dialog.render();
    }
};

class CreateApplicationForm {

    clearForm() {
        this.jqDialog('#errors').html('');
        this.jqDialog('#applicationNameField').val('');
        this.jqDialog('#businessCriticalityField').val('1');
        this.jqDialog('#applicationTypeField').val('1');
        this.jqDialog('#applicationAttributesField').val('');
        this.jqDialog('#microserviceApplicationField').val(false);
        this.jqDialog('#microserviceNameField').val('');
        this.jqDialog('#microserviceAttributesField').val('');
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
            descriptor.submitCreateApplication(this.getFormObject(), getAuthInfo(), t => {
                const responseJson = JSON.parse(t.responseJSON);
                if (!responseJson.success) {
                    let errorsHTML = '';
                    for (const error of responseJson.errors) {
                        errorsHTML += '<li>' + error + '</li>';
                    }
                    this.jqDialog('#errors').html('<ul>' + errorsHTML + '</ul>');
                    return;
                }

                dispatchEvent('applicationCreated', { applicationId: responseJson.value });
                window.createApplication.dialog.close.click();
            });
        });

        this.jqDialog('#cancelBtn').off('click').click(() => {
            window.createApplication.dialog.close.click();
        });
    }

    jqDialog(selector) {
        return jq('#createApplicationDialog ' + selector);
    }

    init() {
        jq('#applicationCreationForm').hide();

        jq('#createAppBtn').off('click').click(() => {
            window.createApplication.init();
            window.createApplication.dialog.show();
            this.clearForm();
            this.subscribeToFormEvents();
        });
    }
}

new CreateApplicationForm().init();