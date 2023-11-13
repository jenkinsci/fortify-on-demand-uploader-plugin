package org.jenkinsci.plugins.fodupload.Config;

public class FodGlobalConstants {

    /*
     %d -> replaces with releaseID at runtime using string.format
     */
    public static class FodDastApiEndpoint {
        public final static String DastGetApi = "api/v3/releases/%d/dast-automated-scans/scan-setup";
        public final static String DastWebSiteScanPutApi = "api/v3/releases/%d/dast-automated-scans/website-scan-setup";
        public final static String DastWorkflowScanPutApi = "api/v3/releases/%d/dast-automated-scans/workflow-scan-setup";
        public final static String DastFileUploadPatchApi = "api/v3/releases/%d/dast-automated-scans/scan-setup/file-upload";
        public final static String DastStartScanAPi = "/api/v3/releases/%d/dast-automated-scans/start-scan";
        public final static String DastOpenApiScanPutApi = "api/v3/releases/%d/dast-automated-scans/openapi-scan-setup";
        public final static String DastGrpcScanPutApi = "api/v3/releases/%d/dast-automated-scans/grpc-scan-setup";
        public final static String DastGraphQLScanPutApi = "api/v3/releases/%d/dast-automated-scans/graphql-scan-setup";
        public final static String DastPostmanScanPutApi = "api/v3/releases/%d/dast-automated-scans/postman-scan-setup";
    }
        //ERRORS

        public static  class FodDastValidation
        {
            public final static String DastPipelineScanTypeNotFound = "Missing scan type.";
            public final static String DastPipelineWebSiteUrlNotFound = "Missing Website URL.";
            public final static String DastPipelineReleaseIdNotFound = "Missing Release Id";
            public final static String DastPipelineScanEntitlementIdNotFound = "Missing Entitlement Id.";
            public final static String DastPipelineScanEntitlementTypeNotFound = "Missing Entitlement Type.";
            public final static String DastPipelineAssessmentIdNotFound = "Missing Assessment Id.";
            public final static String DastPipelineWorkflowMacroIdNotFound = "Missing Workflow Macro Id.";
        }

        public static class FodDastErrorMsg
        {
            public final static String FailedToSaveScanSettingForRelease = "Failed to save scan settings for release id %d";

        }


    }