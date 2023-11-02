package org.jenkinsci.plugins.fodupload.models;

import org.json.JSONPropertyIgnore;

import java.util.List;

public class PutDastWorkflowDrivenScanReqModel extends PutDastScanSetupReqModel {

    /*
        * "workflowDrivenMacro": [
        {
          "fileId": 0,
          "allowedHosts": [
            "string"
          ]
        }
      ], */

    private boolean enableRedundantPageDetection;

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }
    public List<WorkflowDrivenMacro> workflowDrivenMacro;

}
