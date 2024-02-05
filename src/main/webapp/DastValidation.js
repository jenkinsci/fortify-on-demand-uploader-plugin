const fileUploadSuccess = "File uploaded successfully."
const fileUploadFailed = "File upload failed. Please try again!"
const inValidResponse = "Invalid response,";
const allowedFileExtensions = ["burp","har","webmacro"];
const jsonExtension = ["json"];
const protoExtension = ["proto"];


const requiredFieldsFreestyle = ['webSiteUrl',
                                 'networkAuthUserName',
                                 'networkAuthPassword',
                                 'openApiUrl',
                                 'graphQLUrl',
                                 'graphQLSchemeType',
                                 'graphQlApiHost',
                                 'graphQlApiServicePath',
                                 'grpcApiHost',
                                 'grpcApiServicePath',
                                 'grpcSchemeType',
                                 'scanTimeBox'];

const requiredFieldsPipeline = ['webSiteUrl',
                                'networkAuthUserName',
                                'networkAuthPassword',
                                'openApiUrl',
                                'graphQLUrl',
                                'graphQLSchemeType',
                                'graphQlApiHost',
                                'graphQlApiServicePath',
                                'grpcSchemeType',
                                'grpcApiHost',
                                'grpcApiServicePath',
                                'scanTimeBox'];

const requiredFieldsPipelineById = ['#autoProvOwner',
                                    '#autoProvAppName',
                                    '#autoProvRelName'];


     function setOnblurEventForFreestyle () {
             //website ,workflow-driven scan, Network Authentication, Timebox fields
             jq('[name="webSiteUrl"], [name="networkAuthUserName"], [name="networkAuthPassword"]')
                .blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             jq('[name="scanTimeBox"]').blur(_ => this.validateTimeboxScan("[name='" + event.target.name + "']"));

             //openApi fields
             jq('[name="openApiUrl"]').blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //graphQl fields
             jq('[name="graphQLUrl"],[name="graphQLSchemeType"], [name="graphQlApiServicePath"],[name="graphQlApiHost"]')
               .blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //grpc fields
             jq('[name="grpcApiHost"],[name="grpcSchemeType"], [name="grpcApiServicePath"]').blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

     }

     function setOnblurEventForPipeline () {
             //website ,workflow-driven scan, Network Authentication, Timebox fields
             jq('[name="webSiteUrl"], [name="networkAuthUserName"], [name="networkAuthPassword"]')
                .blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             jq('[name="scanTimeBox"]').blur(_ => this.validateTimeboxScan("[name='" + event.target.name + "']"));

             //openApi fields
             jq('[name="openApiUrl"]').blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //graphQl fields
             jq('[name="graphQLUrl"],[name="graphQLSchemeType"], [name="graphQlApiServicePath"],[name="graphQlApiHost"]')
                .blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             //grpc fields
             jq('[name="grpcSchemeType"],[name="grpcApiHost"], [name="grpcApiServicePath"]').blur(_ => this.validateTextbox("[name='" + event.target.name + "']"));

             jq('[name="workflowMacroFilePath"], [name="loginMacroFilePath"]').blur(_ => this.validateFileExtension("[name='" + event.target.name + "']", allowedFileExtensions));

             jq('[name="postmanFilePath"], [name="openApiFilePath"], [name="graphQLFilePath"]').blur(_ => this.validateFileExtension("[name='" + event.target.name + "']", jsonExtension));

             jq('[name="grpcFilePath"]').blur(_ => this.validateFileExtension("[name='" + event.target.name + "']", protoExtension));

             jq('#autoProvOwner').blur(_ => this.validateTextbox('#autoProvOwner'));
             jq('#autoProvAppName').blur(_ => this.validateTextbox('#autoProvAppName'));
             jq('#autoProvRelName').blur(_ => this.validateTextbox('#autoProvRelName'));
          }

     function handleUploadStatusMessage (id, message, success) {
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

     function validateDropdown (id)
     {
        let value = jq(id).val();
        if(value == null || value == '-1' || value == '0') {
            jq(id).addClass('req-field');
        }
        else {
                jq(id).removeClass('req-field');
             }
     }


     function validateRequiredFields (requiredFields) {
       jq.each(requiredFields, function(index, val) {
        validateTextbox('[name="' + val + '"]');
        });
     }

     function validateRequiredFieldsById (requiredFieldIds) {
        jq.each(requiredFieldIds, function(index, val) {
        validateTextbox(val);
        });
     }

     function validateFileExtension (id, extensions) {
        let ctl = jq(id).next('p');
        ctl.hide();
        jq(id).removeClass('req-field');

        let allowedExtension = false;
        let path = jq(id).val();
        if(path && path.length > 0) {
            let extension = path.split('.').pop();
            jq.each(extensions, function( index, value ) {
                 if(value === extension)
                    allowedExtension = true;
            });
        }
        if(allowedExtension) {
            jq(id).removeClass('req-field');
            ctl.hide();
        }
        else {
            jq(id).addClass('req-field');
            if(path && path.length > 0)
               ctl.show();
        }
     }
     function validateTimeboxScan (id) {
        var value= jq(id).val();
        if(value.length == 0 ) {
            jq(id).addClass('req-field');
        }
        else if( isNaN(value) || (value <= 0 || value > 24)) {
                jq(id).addClass('req-field');
         }
        else
             jq(id).removeClass('req-field');
        }



