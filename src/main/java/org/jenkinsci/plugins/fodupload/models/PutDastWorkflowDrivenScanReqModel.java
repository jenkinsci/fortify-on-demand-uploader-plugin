package org.jenkinsci.plugins.fodupload.models;

public class PutDastWorkflowDrivenScanReqModel extends PutDastScanSetupReqModel {
    public WorkflowDrivenMacro getWorkflowDrivenMacro() {
        return new WorkflowDrivenMacro();
    }

    /*
        * "workflowDrivenMacro": [
        {
          "fileId": 0,
          "allowedHosts": [
            "string"
          ]
        }
      ], */
    public WorkflowDrivenMacro workflowDrivenMacro;

    public class WorkflowDrivenMacro {
        public int fileId;
        public String[] allowedHosts;
    }
}
