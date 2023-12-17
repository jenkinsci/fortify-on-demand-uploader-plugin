package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

public class PutDastAutomatedPostmanReqModel extends PutDastScanSetupReqModel {
    int[] CollectionFileIds;

    public void setCollectionFileIds(int[] fileIds){
        this.CollectionFileIds = fileIds;
    }


}

