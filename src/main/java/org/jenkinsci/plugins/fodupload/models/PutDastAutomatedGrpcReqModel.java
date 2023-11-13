package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

public class PutDastAutomatedGrpcReqModel extends PutDastScanSetupReqModel {
    public int FileId;
    public String SchemeType;
    public String Host;
    public String ServicePath;


    public  void setFileId(String fileId){
        this.FileId = Integer.parseInt(fileId);
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

}
