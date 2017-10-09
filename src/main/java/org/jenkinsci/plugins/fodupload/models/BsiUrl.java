package org.jenkinsci.plugins.fodupload.models;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jenkinsci.plugins.fodupload.Utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class BsiUrl {
    private int tenantId;
    private String tenantCode;
    private int projectVersionId;
    private String payloadType;
    private int assessmentTypeId;
    private String technologyStack;
    private String languageLevel = "";
    private String endpoint;
    public String ORIGINAL_VALUE;

    public BsiUrl(String bsiUrl) throws URISyntaxException {
        ORIGINAL_VALUE = bsiUrl;
        URI uri = new URI(bsiUrl);
        endpoint = uri.getScheme() + "://" + uri.getAuthority();
        List<NameValuePair> params = URLEncodedUtils.parse(uri, "UTF-8");
        createBsiUrl(params);
    }

    private void createBsiUrl(List<NameValuePair> params) {
        for(NameValuePair param : params) {
            switch(param.getName()) {
                case "tid":
                    tenantId = Integer.parseInt(param.getValue());
                    break;
                case "tc":
                    tenantCode = param.getValue();
                    break;
                case "pv":
                    projectVersionId = Integer.parseInt(param.getValue());
                    break;
                case "ts":
                    technologyStack = param.getValue();
                    break;
                case "ll":
                    languageLevel = param.getValue();
                    break;
                case "astid":
                    assessmentTypeId = Integer.parseInt(param.getValue());
                    break;
                case "payloadType":
                    payloadType = param.getValue();
                    break;
                default:
                    break;
            }
        }
    }

    public int getTenantId() {
        return tenantId;
    }
    public boolean hasTenantId() {
        return tenantId != 0;
    }

    public String getTenantCode() {
        return tenantCode;
    }
    public boolean hasTenantCode() {
        return !Utils.isNullOrEmpty(tenantCode);
    }

    public int getProjectVersionId() {
        return projectVersionId;
    }
    public boolean hasProjectVersionId() {
        return projectVersionId != 0;
    }

    public String getPayloadType() {
        return payloadType;
    }
    public boolean hasPayloadType() {
        return !Utils.isNullOrEmpty(payloadType);
    }

    public int getAssessmentTypeId() {
        return assessmentTypeId;
    }
    public boolean hasAssessmentTypeId() {
        return assessmentTypeId != 0;
    }

    public String getTechnologyStack() {
        return technologyStack;
    }
    public boolean hasTechnologyStack() {
        return !Utils.isNullOrEmpty(technologyStack);
    }

    public String getLanguageLevel() {
        return languageLevel;
    }
    public boolean hasLanguageLevel() {
        return !Utils.isNullOrEmpty(languageLevel);
    }

    public String getEndpoint() {
        return endpoint;
    }
    public boolean hasEndpoint() {
        return !Utils.isNullOrEmpty(endpoint);
    }
}
