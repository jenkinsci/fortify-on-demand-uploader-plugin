package org.jenkinsci.plugins.fodupload;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;

public class BsiTokenParser {
    public org.jenkinsci.plugins.fodupload.models.BsiToken parseBsiToken(String encodedBsiString) {
        byte[] bsiBytes = Base64.getDecoder().decode(encodedBsiString);
        String decodedBsiString = new String(bsiBytes, StandardCharsets.UTF_8);
        Gson gson = new Gson();
        org.jenkinsci.plugins.fodupload.models.BsiToken token = gson.fromJson(decodedBsiString, org.jenkinsci.plugins.fodupload.models.BsiToken.class);
        return token;
    }

    public org.jenkinsci.plugins.fodupload.models.BsiToken tryParseBsiToken(String encodedBsiString) {
        try {
            return parseBsiToken(encodedBsiString);
        } catch (Exception ex) {
            return null;
        }
    }
}
