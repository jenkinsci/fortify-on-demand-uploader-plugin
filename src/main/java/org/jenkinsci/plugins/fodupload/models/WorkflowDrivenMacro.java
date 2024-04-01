package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
public class WorkflowDrivenMacro implements Serializable {
    public int fileId;
    public String[] allowedHosts;
}
