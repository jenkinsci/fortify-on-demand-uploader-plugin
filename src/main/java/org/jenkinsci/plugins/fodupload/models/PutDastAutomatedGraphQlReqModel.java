package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("PA_PUBLIC_PRIMITIVE_ATTRIBUTE")
public class PutDastAutomatedGraphQlReqModel extends PutDastScanSetupReqModel {
    public FodEnums.ApiSourceType SourceType;
    public String SourceUrn;
    public String SchemeType;
    public String Host;

    public String ServicePath;
    Integer timeBoxInHours;

    public void setSourceType(String sourceType){
        this.SourceType = FodEnums.ApiSourceType.valueOf(sourceType);
    }
    public  void setSourceUrn(String sourceUrn){
        this.SourceUrn = sourceUrn;
    }
    public void setServicePath(String servicePath){
        this.ServicePath = servicePath;
    }
    public void setHost(String host){
        this.Host = host;
    }
    public void setSchemeType(String scheme){
        this.SchemeType = scheme;
    }
    public void setTimeBoxInHours(Integer timeBoxInHours) {
        this.timeBoxInHours = timeBoxInHours;
    }


}

