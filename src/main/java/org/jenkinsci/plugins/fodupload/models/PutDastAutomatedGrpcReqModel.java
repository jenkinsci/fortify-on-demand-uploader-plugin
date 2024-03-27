package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;
@SuppressFBWarnings({"PA_PUBLIC_PRIMITIVE_ATTRIBUTE","SE_NO_SERIALVERSIONID"})
public class PutDastAutomatedGrpcReqModel extends PutDastScanSetupReqModel {

    public int FileId;
    String SchemeType;
    String Host;
    String ServicePath;
    Integer timeBoxInHours;

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
    public void setTimeBoxInHours(Integer timeBoxInHours) {
        this.timeBoxInHours = timeBoxInHours;
    }

}
