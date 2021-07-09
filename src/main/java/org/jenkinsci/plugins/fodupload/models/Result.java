package org.jenkinsci.plugins.fodupload.models;

import java.util.List;

public class Result<T> {

    private Boolean isSuccess;
    private List<String> errors;
    private T value;

    public Result(Boolean isSuccess, List<String> errors, T value) {
        this.isSuccess = isSuccess;
        this.errors = errors;
        this.value = value;
    }

    public Boolean getIsSuccess() { return isSuccess; }

    public T getValue() {
        return value;
    }

    public List<String> getErrors() {
        return errors;
    }
}
