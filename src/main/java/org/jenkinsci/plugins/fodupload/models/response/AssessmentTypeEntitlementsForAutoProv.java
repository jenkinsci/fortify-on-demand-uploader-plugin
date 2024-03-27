package org.jenkinsci.plugins.fodupload.models.response;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.List;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class AssessmentTypeEntitlementsForAutoProv {
    private Integer releaseId;
    private List<AssessmentTypeEntitlement> assessments;
    private GetStaticScanSetupResponse settings;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public AssessmentTypeEntitlementsForAutoProv(Integer releaseId, List<AssessmentTypeEntitlement> entitlements, GetStaticScanSetupResponse settings) {
        this.releaseId = releaseId;
        this.assessments = entitlements;
        this.settings = settings;
    }

    public Integer getReleaseId() {
        return releaseId;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<AssessmentTypeEntitlement> getAssessments() {
        return assessments;
    }

    public GetStaticScanSetupResponse getSettings() {
        return settings;
    }
}
