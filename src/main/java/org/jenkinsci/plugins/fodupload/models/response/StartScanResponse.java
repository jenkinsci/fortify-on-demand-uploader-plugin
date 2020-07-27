package org.jenkinsci.plugins.fodupload.models.response;

public class StartScanResponse {
    private boolean success;
    private boolean scanInProgress;
    private int scanId;

    public boolean isScanInProgress() {
        return scanInProgress;
    }
    public boolean isSuccessful() {
        return success;
    }
    public int getScanId() {
        return scanId;
    }
    public void setScanId(int scanId) {
        this.scanId = scanId;
    }
    //Best case scenario
    public void uploadSuccessfulScanStarting() {
        success = true;
        scanInProgress = false;
    }
    //Best case scenario
    public void uploadSuccessfulScanStarting(int scanId) {
        this.success = true;
        this.scanInProgress = true;
        this.scanId = scanId;
    }
    //Scan in progress but no issues uploading files. Not intricately verified.
    public void uploadSuccessfulScanNotStarted() {
        success = true;
        scanInProgress= true;
    }
    //Scan in progress but no issues uploading files. Not intricately verified.
    public void uploadSuccessfulScanNotStarted(int scanId) {
        this.success = true;
        this.scanInProgress= true;
        this.scanId = scanId;
    }
    //Generic something went wrong
    public void uploadNotSuccessful() {
        success = false;
        scanInProgress = false;
    }
}