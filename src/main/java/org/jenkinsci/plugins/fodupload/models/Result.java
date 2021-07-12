package org.jenkinsci.plugins.fodupload.models;

import java.util.List;

public class Result<T> {

    private Boolean success;
    private List<String> errors;
    private T value;

    public Result(Boolean success, List<String> errors, T value) {
        this.success = success;
        this.errors = errors;
        this.value = value;
    }

    public Boolean getSuccess() { return success; }

    public T getValue() {
        return value;
    }

    public List<String> getErrors() {
        return errors;
    }
}
