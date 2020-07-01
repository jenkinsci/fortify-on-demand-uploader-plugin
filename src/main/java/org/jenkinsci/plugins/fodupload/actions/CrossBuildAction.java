package org.jenkinsci.plugins.fodupload.actions;

import hudson.model.Action;
import hudson.model.Result;

public class CrossBuildAction implements Action {

    private Result previousStepBuildResult;
    private boolean allowPolling;

    public Result getPreviousStepBuildResult() {
        return previousStepBuildResult;
    }

    public void setPreviousStepBuildResult(Result buildResult) {
        this.previousStepBuildResult = buildResult;
        if(Result.UNSTABLE.equals(buildResult)){
            this.allowPolling = false;
        }
    }

    public boolean allowPolling() {
        return allowPolling;
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