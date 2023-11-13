package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

public class PutDastAutomatedPostmanReqModel extends PutDastScanSetupReqModel {
    public int[] CollectionFileIds;

    public void setCollectionFileIds(int[] fileIds){
        this.CollectionFileIds = fileIds;
    }


}

