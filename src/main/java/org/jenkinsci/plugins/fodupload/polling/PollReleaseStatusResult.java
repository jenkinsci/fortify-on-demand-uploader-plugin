package org.jenkinsci.plugins.fodupload.polling;

public class PollReleaseStatusResult {

    private boolean isPollingSuccessful;
    private boolean isScanUploadAccepted;
    private boolean isPassing;
    private String failReason;

    public PollReleaseStatusResult() {

    }

    public boolean isPassing() {
        return isPassing;
    }

    public void setPassing(boolean passing) {
        isPassing = passing;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public boolean isPollingSuccessful() {
        return isPollingSuccessful;
    }

    public void setPollingSuccessful(boolean pollingSuccessful) {
        isPollingSuccessful = pollingSuccessful;
    }

    public boolean isScanUploadAccepted() {
        return isScanUploadAccepted;
    }

    public void setScanInProgress(boolean scanInProgress) {
        isScanUploadAccepted = scanInProgress;
    }
}
