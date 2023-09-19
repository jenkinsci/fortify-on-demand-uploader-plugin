package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class PutDynamicScanSetupResponse extends FodDastApiResponse {

    public PutDynamicScanSetupResponse(boolean success, List<String> errors,String messages) {
        super(success, errors, messages);
    }

}
