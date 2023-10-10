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
    public List<WorkflowDrivenMacro> workflowDrivenMacro;

}
