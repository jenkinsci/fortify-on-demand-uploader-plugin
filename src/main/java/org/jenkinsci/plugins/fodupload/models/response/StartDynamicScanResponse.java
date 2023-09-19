package org.jenkinsci.plugins.fodupload.models.response;

import java.util.List;

public class StartDynamicScanResponse extends FodDastApiResponse{
    public StartDynamicScanResponse(boolean success, List<String> errors, String messages) {
        super(success, errors, messages);
    }
}
