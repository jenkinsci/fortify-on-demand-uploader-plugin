package org.jenkinsci.plugins.fodupload;

import java.util.Base64;

import com.google.gson.Gson;

public class BsiTokenParser {
    public org.jenkinsci.plugins.fodupload.models.BsiToken parseBsiToken(String encodedBsiString) {
        byte[] bsiBytes = Base64.getDecoder().decode(encodedBsiString);
        String decodedBsiString = String.valueOf(bsiBytes);
        Gson gson = new Gson();
        org.jenkinsci.plugins.fodupload.models.BsiToken token = gson.fromJson(decodedBsiString, org.jenkinsci.plugins.fodupload.models.BsiToken.class);
        return token;
    }
}
