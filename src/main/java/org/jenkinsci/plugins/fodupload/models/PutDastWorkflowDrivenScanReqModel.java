package org.jenkinsci.plugins.fodupload.models;

import org.json.JSONPropertyIgnore;

import java.util.List;

public class PutDastWorkflowDrivenScanReqModel extends PutDastScanSetupReqModel {
    public List<WorkflowDrivenMacro> workflowDrivenMacro;
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    String policy;
    private boolean enableRedundantPageDetection;

    public boolean isEnableRedundantPageDetection() {
        return enableRedundantPageDetection;
    }

    public void setEnableRedundantPageDetection(boolean enableRedundantPageDetection) {
        this.enableRedundantPageDetection = enableRedundantPageDetection;
    }

}
