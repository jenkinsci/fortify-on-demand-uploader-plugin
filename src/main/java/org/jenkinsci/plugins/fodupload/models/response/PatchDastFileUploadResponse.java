package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

/* Response model
{
  "fileId": 0,
  "hosts": [
    "string"
  ]
}
 */
public class PatchDastFileUploadResponse extends  FodDastApiResponse{


    public PatchDastFileUploadResponse(boolean success, List<String> errors, String messages) {
        super(success, errors, messages);
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int fileId;

    public String[] getHosts() {
        return hosts;
    }

    public void setHosts(String[] hosts) {
        this.hosts = hosts;
    }

    public String[] hosts;
}
