package org.jenkinsci.plugins.fodupload.actions;

import hudson.model.Action;
import hudson.model.Result;

public class CrossBuildAction implements Action {

    private Result previousStepBuildResult;
    private boolean allowPolling;
    private int scanId;
    private String correlationId;

    public Result getPreviousStepBuildResult() {
        return previousStepBuildResult;
    }

    public void setPreviousStepBuildResult(Result buildResult) {
        this.previousStepBuildResult = buildResult;
        if(Result.SUCCESS.equals(buildResult)){
            this.allowPolling = true;
        } else {
            this.allowPolling = false;
        }
    }

    public void setScanId(int uploadScanId) {
        this.scanId = uploadScanId;
    }

    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public boolean allowPolling() {
        return allowPolling;
    }

    public int currentScanId() {
        return scanId;
    }

    public String currentCorrelationId() {
        return correlationId;
    }

    @Override
    public String getIconFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrlName() {
        // TODO Auto-generated method stub
        return null;
    }
    
}