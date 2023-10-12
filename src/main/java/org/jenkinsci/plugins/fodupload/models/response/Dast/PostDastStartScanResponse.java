package org.jenkinsci.plugins.fodupload.models.response.Dast;

public class PostDastStartScanResponse extends FodDastApiResponse {

    public Integer scanId;
    public Integer getScanId() {
        return scanId;
    }
    public void setScanId(String scanId) {
        scanId = scanId;
    }
}
