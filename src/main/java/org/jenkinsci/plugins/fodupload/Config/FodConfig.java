package org.jenkinsci.plugins.fodupload.Config;

public class FodConfig {

    /*
     %d -> replaces with releaseID at runtime using string.format
     */
   public static class FodDastApiConstants {
        public static String DastGetApi = "api/v3/releases/%d/dast-automated-scans/scan-setup";
        public static String DastWebSiteScanPutApi = "api/v3/releases/%d/dast-automated-scans/website-scan-setup";
        public static String DastApiScanPutApi = "api/v3/releases/%d/dast-automated-scans/api-scan-setup";
        public static String DastWorkflowScanPutApi = "api/v3/releases/%d/dast-automated-scans/workflow-scan-setup";
        public static String DastFileUploadPatchApi = "api/v3/releases/%d/dast-automated-scans/scan-setup/file-upload";

        public static  String DastStartScanAPi ="/api/v3/releases/%d/dynamic-scans/dast-automated-scans/scan-setup";
    }

}
