package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class PutDynamicScanSetupResponse extends FodDastApiResponse {

    public PutDynamicScanSetupResponse(boolean success, List<FodDastApiResponse.Error> errors, List<String> messages) {
        super(success, errors, messages);
    }

}
