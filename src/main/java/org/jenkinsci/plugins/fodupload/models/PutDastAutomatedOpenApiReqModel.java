package org.jenkinsci.plugins.fodupload.models;

public class PutDastAutomatedOpenApiReqModel extends PutDastScanSetupReqModel {
    FodEnums.ApiSourceType SourceType;
    String SourceUrn;
    String ApiKey;
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


