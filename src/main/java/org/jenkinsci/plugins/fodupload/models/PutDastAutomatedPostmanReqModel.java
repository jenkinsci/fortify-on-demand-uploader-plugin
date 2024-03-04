package org.jenkinsci.plugins.fodupload.models;

import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

public class PutDastAutomatedPostmanReqModel extends PutDastScanSetupReqModel {
    int[] CollectionFileIds;
    Integer timeBoxInHours;

    public void setCollectionFileIds(int[] fileIds){
        this.CollectionFileIds = fileIds;
    }
    public void setTimeBoxInHours(Integer timeBoxInHours) {
        this.timeBoxInHours = timeBoxInHours;
    }


}

