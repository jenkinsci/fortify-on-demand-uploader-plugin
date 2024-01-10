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
            public final static String DastPipelineReleaseIdNotFound = "Missing Release Id.";
            public final static String DastPipelineScanEntitlementIdNotFound = "Missing Entitlement Id.";
            public final static String DastPipelineScanEntitlementTypeNotFound = "Missing Entitlement Type.";
            public final static String DastPipelineAssessmentIdNotFound = "Missing Assessment Id.";
            public final static String DastPipelineWorkflowMacroIdNotFound = "Missing Workflow Macro Id.";

            public final static String DastPipelineWorkflowMacroFilePathNotFound = "Missing Workflow Macro File Path.";

            public final  static  String DastWorkflowAllowedHostNotFound = "Missing Workflow Allowed Host.";

            public final  static  String DastScanPolicyNotFound = "Missing Scan Policy Type.";

            public final  static  String DastScanNetworkUserNameNotFound = "Missing NetworkWork Authentication User Name.";
            public final  static  String DastScanNetworkPasswordNotFound = "NetworkWork Authentication Password not set.";

            public final  static  String DastScanNetworkAuthTypeNotFound = "Missing NetworkWork Authentication Type.";
            public final  static  String DastScanAPITypeNotFound = "Missing API Type.";
            public final  static  String DastScanOpenApiSourceNotFound = "Missing Open Api Source input.";
            public final  static  String DastScanGrpcSourceNotFound = "Missing GRPC Source input.";
            public final  static  String DastScanGraphQlSourceNotFound = "Missing GraphQL Source input.";
            public final  static  String DastScanGraphQlServicePathNotFound = "Missing GraphQL Service Path input.";
            public final  static  String DastScanGraphQlHostNotFound = "Missing GraphQL Host input.";
            public final  static  String DastScanGraphQlSchemeTypeNotFound = "Missing GraphQL Scheme Type input.";
            public final  static  String DastScanGrpcServicePathNotFound = "Missing GRPC Service Path input.";
            public final  static  String DastScanGrpcHostNotFound = "Missing GRPC Host input.";
            public final  static  String DastScanGrpcSchemeTypeNotFound = "Missing GRPC Scheme Type input.";
            public final  static  String DastScanPostmanSourceNotFound = "Missing Postman Source input.";

        }

        public static class FodDastErrorMsg
        {
            public final static String FailedToSaveScanSettingForRelease = "Failed to save scan settings for release id %d";

        }


    }