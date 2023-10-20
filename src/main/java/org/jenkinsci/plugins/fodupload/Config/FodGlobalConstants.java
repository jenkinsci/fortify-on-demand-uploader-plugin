package org.jenkinsci.plugins.fodupload.Config;

public class FodGlobalConstants {

    /*
     %d -> replaces with releaseID at runtime using string.format
     */
    public static class FodDastApiConstants {
        public final static String DastGetApi = "api/v3/releases/%d/dast-automated-scans/scan-setup";
        public final static String DastWebSiteScanPutApi = "api/v3/releases/%d/dast-automated-scans/website-scan-setup";
        public final static String DastApiScanPutApi = "api/v3/releases/%d/dast-automated-scans/api-scan-setup";
        public final static String DastWorkflowScanPutApi = "api/v3/releases/%d/dast-automated-scans/workflow-scan-setup";
        public final static String DastFileUploadPatchApi = "api/v3/releases/%d/dast-automated-scans/scan-setup/file-upload";
        public final static String DastStartScanAPi = "/api/v3/releases/%d/dast-automated-scans/start-scan";

    }

}
