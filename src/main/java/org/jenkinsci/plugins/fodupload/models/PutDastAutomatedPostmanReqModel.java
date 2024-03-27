package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.models.response.Dast.GetDastScanSettingResponse;

import java.lang.reflect.Array;
@SuppressFBWarnings("EI_EXPOSE_REP")
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

