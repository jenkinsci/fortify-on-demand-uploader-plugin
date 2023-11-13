package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

public class PutDastAutomatedOpenApiReqModel extends PutDastScanSetupReqModel {
    public FodEnums.ApiSourceType SourceType;
    public String SourceUrn;
    public String ApiKey;
    public void setSourceUrn(String urn) {
       this.SourceUrn = urn;
    }
    public void setSourceType(String sourceType){
        this.SourceType = FodEnums.ApiSourceType.valueOf(sourceType);
    }
    public void setApiKey(String apikey){
        this.ApiKey = apikey;
    }

}


