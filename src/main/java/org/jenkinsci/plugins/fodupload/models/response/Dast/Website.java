package org.jenkinsci.plugins.fodupload.models.response.Dast;

import org.jenkinsci.plugins.fodupload.models.ExcludedUrl;

import java.util.List;

public class Website {
    public String dynamicSiteUrl;
    public boolean requestLoginMacroFileCreation;
    public LoginMacroFileCreationDetails loginMacroFileCreationDetails;
    public List<ExcludedUrl> exclusionsList;

}
