package org.jenkinsci.plugins.fodupload.models;

/* Request model for Dynamic Start scan
{
  "assessmentTypeId": 0,
  "entitlementId": 0,
  "entitlementFrequencyType": "SingleScan",
  "isRemediationScan": true,
  "startDate": "2023-09-18T13:07:11.558Z"
}
 */
public class StartDynamicScanReqModel {
    public void setAssessmentTypeId(int assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEntitlementFrequencyType(String entitlementFrequencyType) {
        this.entitlementFrequencyType = entitlementFrequencyType;
    }

    int assessmentTypeId;

    public void setEntitlementId(int entitlementId) {
        this.entitlementId = entitlementId;
    }

    int entitlementId;

    String startDate;

    String entitlementFrequencyType;

    public void setRemediationScan(boolean remediationScan) {
    }
}
