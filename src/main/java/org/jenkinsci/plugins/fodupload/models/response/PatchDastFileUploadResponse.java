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
public class PatchDastFileUploadResponse   {
    public PatchDastFileUploadResponse(){
    }
    public void setFileId(int fileId) {
        this.fileId = fileId;
    }
    public int fileId;
    public String[] hosts;
}
