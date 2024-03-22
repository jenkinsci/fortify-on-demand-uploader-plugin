package org.jenkinsci.plugins.fodupload.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class Result<T> {

    private Boolean success;
    private List<String> errors;
    private String reason;
    private T value;

    public Result(Boolean success, List<String> errors, T value) {
        this.success = success;
        this.errors = errors;
        this.value = value;
    }

    public Result(Boolean success, List<String> errors, String reason, T value) {
        this(success, errors, value);
        this.reason = reason;
    }

    public Boolean getSuccess() { return success; }

    public T getValue() {
        return value;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getReason() {
        return reason;
    }
}
