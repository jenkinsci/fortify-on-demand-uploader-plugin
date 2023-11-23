const fileUploadSuccess = "File uploaded successfully."
const fileUploadFailed = "File upload failed. Please try again!"
const inValidResponse = "Invalid response. Please try again!";


const requiredFields = ['webSiteUrl',
                       'webSiteNetworkAuthUserName',
                       'webSiteNetworkAuthPassword',
                       'openApiUrl',
                       'graphQLUrl',
                       'graphQLSchemeType',
                       'graphQlApiHost',
                       'graphQlApiServicePath',
                       'grpcApiServicePath',
                       'grpcSchemeType',
                       'scanTimeBox'];

     function setOnblurEvent() {
             //website ,workflow-driven scan, Network Authentication, Timebox fields
             jq('[name="webSiteUrl"], [name="webSiteNetworkAuthUserName"], [name="webSiteNetworkAuthPassword"], [name="scanTimeBox"]')
                .blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //openApi fields
             jq('[name="openApiUrl"]').blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //graphQl fields
             jq('[name="graphQLUrl"],[name="graphQLSchemeType"], [name="graphQlApiServicePath"],[name="graphQlApiHost"]')
               .blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //grpc fields
             jq('[name="grpcSchemeType"], [name="grpcApiServicePath"]').blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));


     }

     function handleUploadStatusMessage(id, message, success) {
          let ctl = jq(id);
          ctl.text(message);
             if(success)
                ctl.removeClass('error-msg');
             else
                ctl.addClass('error-msg');
          ctl.show();
     }

     function handleSpinner(id, completed) {
        if(completed)
           jq(id).removeClass('spinner');
        else
           jq(id).addClass('spinner');
     }

     function validateTextbox (id) {
        if(jq(id).val().length == 0) {
            jq(id).addClass('req-field');
         }
        else {
                jq(id).removeClass('req-field');
             }
     }

     function validateDropdown(id)
     {
        let value = jq(id).val();
        if(value == '-1' || value == '0' ) {
            jq(id).addClass('req-field');
        }
        else {
                jq(id).removeClass('req-field');
             }

     }
     function validateRequiredFields() {
       jq.each(requiredFields, function(index, val)  {
        validateTextbox('[name="' + val + '"]');
        });
     }