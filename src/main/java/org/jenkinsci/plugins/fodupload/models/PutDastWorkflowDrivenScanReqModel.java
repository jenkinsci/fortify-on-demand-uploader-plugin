package org.jenkinsci.plugins.fodupload.models;

import java.util.List;

public class PutDastWorkflowDrivenScanReqModel extends PutDastScanSetupReqModel {
    public List<WorkflowDrivenMacro> workflowDrivenMacro;
    public void setPolicy(String policy) {
        this.policy = policy;
    }
    String policy;
}
