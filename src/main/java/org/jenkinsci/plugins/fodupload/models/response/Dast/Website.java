package org.jenkinsci.plugins.fodupload.models.response.Dast;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jenkinsci.plugins.fodupload.models.ExclusionDTO;

@SuppressFBWarnings("UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD")
public class Website {
    public String dynamicSiteUrl;
    public boolean requestLoginMacroFileCreation;
    public LoginMacroFileCreationDetails loginMacroFileCreationDetails;
    public ExclusionDTO[] exclusionsList;

    public boolean enableRedundantPageDetection;

}
