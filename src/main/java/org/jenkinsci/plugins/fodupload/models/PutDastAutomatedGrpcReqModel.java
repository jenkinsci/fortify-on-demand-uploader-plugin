package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

public class PutDastAutomatedGrpcReqModel extends PutDastScanSetupReqModel {

    public int FileId;
    String SchemeType;
    String Host;
    String ServicePath;

    public void setFileId(Integer fileId) {
        this.FileId = fileId;
    }

    public void setServicePath(String servicePath) {
        this.ServicePath = servicePath;
    }

    public void setHost(String host) {
        this.Host = host;
    }

    public void setSchemeType(String scheme) {
        this.SchemeType = scheme;
    }

}
