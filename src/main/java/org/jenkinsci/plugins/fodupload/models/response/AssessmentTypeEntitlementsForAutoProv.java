package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class AssessmentTypeEntitlementsForAutoProv {
    private Integer releaseId;
    private List<AssessmentTypeEntitlement> assessments;
    private GetStaticScanSetupResponse settings;

    public AssessmentTypeEntitlementsForAutoProv(Integer releaseId, List<AssessmentTypeEntitlement> entitlements, GetStaticScanSetupResponse settings) {
        this.releaseId = releaseId;
        this.assessments = entitlements;
        this.settings = settings;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    public List<AssessmentTypeEntitlement> getAssessments() {
        return assessments;
    }

    public GetStaticScanSetupResponse getSettings() {
        return settings;
    }
}
