package org.jenkinsci.plugins.fodupload.models.response;

public class StartScanResponse {
    private boolean success;
    private boolean scanInProgress;

    public boolean isScanInProgress() {
        return scanInProgress;
    }
    public boolean isSuccessful() {
        return success;
    }
    //Best case scenario
    public void uploadSuccessfulScanStarting() {
        success = true;
        scanInProgress = false;
    }
    //Scan in progress but no issues uploading files. Not intricately verified.
    public void uploadSuccessfulScanNotStarted() {
        success = true;
        scanInProgress= true;
    }
    //Generic something went wrong
    public void uploadNotSuccessful() {
        success = false;
        scanInProgress = false;
    }
}