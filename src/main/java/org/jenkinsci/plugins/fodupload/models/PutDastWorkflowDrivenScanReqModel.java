package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
public class PutDastWorkflowDrivenScanReqModel extends PutDastScanSetupReqModel {
    public List<WorkflowDrivenMacro> workflowDrivenMacro;
    public void setPolicy(String policy) {
        this.policy = policy;
    }
    String policy;
}
