package org.jenkinsci.plugins.fodupload.steps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Run;
import jenkins.tasks.SimpleBuildStep;

public abstract class FortifyStep extends Step implements SimpleBuildStep {


    protected Run<?, ?> lastBuild;

    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    protected void setLastBuild(Run<?, ?> lastBuild) {
        this.lastBuild = lastBuild;
    }



    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        return false;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build != null && launcher != null && listener != null && build.getWorkspace() != null) {
            perform(build, build.getWorkspace(), launcher, listener);
        }
        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Collections.emptyList();
    }

    @Override
    public StepExecution start(StepContext arg0) throws Exception {
        return null;
    }

    public hudson.tasks.BuildStepMonitor getRequiredMonitorService() {
        return null;
    }

}
