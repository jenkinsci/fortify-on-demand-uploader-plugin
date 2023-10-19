package org.jenkinsci.plugins.fodupload.models.response;

import org.jenkinsci.plugins.fodupload.models.response.Dast.FodDastApiResponse;

import java.util.List;

/* Response model
{
  "fileId": 0,
  "hosts": [
    "string"
  ]
}
 */
public class PatchDastFileUploadResponse extends FodDastApiResponse {
    public PatchDastFileUploadResponse(){
    }
    public void setFileId(int fileId) {
        this.fileId = fileId;
    }
    public int fileId;
    public String[] hosts;
}
