package org.jenkinsci.plugins.fodupload.models.response;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;

@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
public class StartScanResponse implements Serializable {
    private boolean success;
    private boolean scanInProgress;
    private int scanId;

    public boolean isScanUploadAccepted() {
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
        scanInProgress= false;
    }
    //Scan in progress but no issues uploading files. Not intricately verified.
    public void uploadSuccessfulScanNotStarted(int scanId) {
        this.success = true;
        this.scanInProgress= false;
        this.scanId = scanId;
    }
    //Generic something went wrong
    public void uploadNotSuccessful() {
        success = false;
        scanInProgress = false;
    }
}