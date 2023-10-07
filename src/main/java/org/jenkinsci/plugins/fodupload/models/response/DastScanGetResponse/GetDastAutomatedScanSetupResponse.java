package org.jenkinsci.plugins.fodupload.models.response.DastScanGetResponse;

import java.util.ArrayList;
import java.util.List;

/*
{
  "assessmentTypeId": 0,
  "geoLocationId": 0,
  "timeZone": "string",
  "dynamicScanEnvironmentFacingType": "Internal",
  "entitlementFrequencyType": "SingleScan",
  "scanType": "Standard",
  "policy": "Standard",
  "websiteAssessment": {
    "urls": [
      "string"
    ]
  },
  "apiAssessment": {
    "apiType": "OpenApi",
    "apiSource": "File",
    "openAPI": {
      "url": "string",
      "fileId": 0,
      "apiKey": "string"
    },
    "graphQL": {
      "url": "string",
      "fileId": 0,
      "scheme": "string",
      "host": "string",
      "servicePath": "string"
    },
    "gRPC": {
      "fileId": 0,
      "scheme": "string",
      "host": "string",
      "servicePath": "string"
    },
    "postman": {
      "collectionFileIds": [
        0
      ]
    }
  },
  "timeBoxInHours": 0,
  "exclusionsList": [
    {
      "value": "string"
    }
  ],
  "workflowdrivenAssessment": {
    "workflowDrivenMacro": [
      {
        "fileId": 0,
        "allowedHosts": [
          "string"
        ]
      }
    ]
  },
  "loginMacroFileId": 0,
  "requiresSiteAuthentication": true,
  "enableRedundantPageDetection": true,
  "allowFormSubmissions": true,
  "allowSameHostRedirects": true,
  "restrictToDirectoryAndSubdirectories": true,
  "requiresNetworkAuthentication": true,
  "networkAuthenticationSettings": {
    "networkAuthenticationType": "Basic",
    "userName": "string",
    "password": "string"
  },
  "blockout": [
    {
      "day": "Sunday",
      "hourBlocks": [
        {
          "hour": 0,
          "checked": true
        }
      ]
    }
  ]
}
 */
public class GetDastAutomatedScanSetupResponse {

    public int assessmentTypeId;
    public int geoLocationId;
    public String timeZone;
    public DynamicScanEnvironmentFacingTypes dynamicScanEnvironmentFacingType;

    public enum DynamicScanEnvironmentFacingTypes {
        Internal,
        External,
    }

    public enum EntitlementFrequencyTypes {
        SingleScan,
        Subscription
    }

    public EntitlementFrequencyTypes entitlementFrequencyType;

    public enum ScanTypes {
        Static,
        Dynamic,
        Mobile,
        Monitoring,
        Network,
        OpenSource,
    }

    public ScanTypes scanType;

    public enum Policy {
        Standard,
        Api,
        CriticalsAndHighs,
        PassiveScan
    }

    public enum ApiSource {
        FileId,
        Url

    }

    public Policy policy;
    public Website websiteAssessment;
    public API apiAssessment;
    public int timeBoxInHours;

    public ExclusionDTO[] exclusionsList;

    public WorkflowDrivenMacros[] workflowdrivenAssessment;
    public int loginMacroFileId;
    public boolean requiresSiteAuthentication;
    public boolean enableRedundantPageDetection;
    public boolean allowFormSubmissions;
    public boolean allowSameHostRedirects;
    public boolean restrictToDirectoryAndSubdirectories;
    public boolean requiresNetworkAuthentication;
    public NetworkAuthenticationSettings networkAuthenticationSettings;

    public enum DayOfWeekTypes
    {
        Sunday,
        Monday ,
        Tuesday,
        Wednesday ,
        Thursday ,
        Friday ,
        Saturday
    }


}
