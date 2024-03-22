package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;

@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
public class Error implements Serializable {
    @SuppressWarnings("unused")
    public int errorCode;
    @SuppressWarnings("unused")
    public String message;
}
