package org.jenkinsci.plugins.fodupload.models.response.Dast;

import org.jenkinsci.plugins.fodupload.models.ExclusionDTO;

public class Website {
    public String dynamicSiteUrl;
    public boolean requestLoginMacroFileCreation;
    public LoginMacroFileCreationDetails loginMacroFileCreationDetails;
    public ExclusionDTO[] exclusionsList;

}
