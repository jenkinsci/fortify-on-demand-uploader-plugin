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

function jqDialog(selector) {
    return jq('#createApplicationDialog ' + selector);
}

function clearForm() {
    jqDialog('#applicationNameField').val('');
    jqDialog('#businessCriticalityField').val('1');
    jqDialog('#applicationTypeField').val('1');
    jqDialog('#applicationAttributesField').val('');
    jqDialog('#microserviceApplicationField').val(false);
    jqDialog('#microserviceNameField').val('');
    jqDialog('#microserviceAttributesField').val('');
    jqDialog('.microservice-fields').hide();
}

function subscribeToFormEvents() {
    jqDialog('#microserviceApplicationField').off('change').change(() => {

        const show = jqDialog('#microserviceApplicationField').is(':checked');
        if (show) {
            jqDialog('.microservice-fields').show();
        }
        else {
            jqDialog('.microservice-fields').hide();
        }
    });

    jqDialog('#submitBtn').off('click').click(() => {

    });

    jqDialog('#cancelBtn').off('click').click(() => {
        window.createApplication.dialog.close.click();
    });
}

function init() {
    jq('#applicationCreationForm').hide();

    jq('#createAppBtn').off('click').click(() => {
        window.createApplication.init();
        window.createApplication.dialog.show();
        clearForm();
        subscribeToFormEvents();
    });
}

init();