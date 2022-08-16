package org.jenkinsci.plugins.fodupload;

import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FodGlobalDescriptorTest {


    private static final String EXPECTED_API_URL = "https://api.ams.fortify.com";
    private static final String EXPECTED_BASE_URL = "https://ams.fortify.com";
    private static final String EXPECTED_GLOBAL_AUTH_TYPE = "apiKeyType";
    private static final String EXPECTED_API_AUTH_TYPE = "apiKeyType";
    private static final String EXPECTED_PAT_AUTH_TYPE = "personalAccessTokenType";
    private static final String EXPECTED_CLIENT_SECRET = "myclientSecret";
    private static final String EXPECTED_CLIENT_ID = "myClientId";
    private static final String EXPECTED_USER_NAME = "myUserName";
    private static final String EXPECTED_PERSONAL_ACCESS_TOKEN = "12345678-abcd-1234-efgh-123456abcdef";
    private static final String EXPECTED_TENANT_ID = "myTenant";
    private static final String EXPECTED_SCANCENTRAL_PATH = "thePathWithoutValidation";

    @Rule
    public final JenkinsConfiguredWithCodeRule jenkinsConfiguredWithRule = new JenkinsConfiguredWithCodeRule();

    @Test
    @ConfiguredWithCode("org.jenkinsci.plugins.fodupload/FodGlobalDescriptorTest/configuration-as-code.yml")
    public void fodGlobalDescriptor_AttributeValues_configuredWithRule() {

        final FodGlobalDescriptor fodGlobalDescriptor = (FodGlobalDescriptor) jenkinsConfiguredWithRule.jenkins.getDescriptorOrDie(FodGlobalDescriptor.class);

        assertEquals(EXPECTED_API_URL, fodGlobalDescriptor.getApiUrl());
        assertEquals(EXPECTED_BASE_URL, fodGlobalDescriptor.getBaseUrl());
        assertEquals(EXPECTED_GLOBAL_AUTH_TYPE, fodGlobalDescriptor.getGlobalAuthType());
        assertEquals(EXPECTED_CLIENT_SECRET, fodGlobalDescriptor.getClientSecret());
        assertEquals(EXPECTED_CLIENT_ID, fodGlobalDescriptor.getClientId());
        assertEquals(EXPECTED_USER_NAME, fodGlobalDescriptor.getUsername());
        assertEquals(EXPECTED_PERSONAL_ACCESS_TOKEN, fodGlobalDescriptor.getPersonalAccessToken());
        assertEquals(EXPECTED_TENANT_ID, fodGlobalDescriptor.getTenantId());
        assertEquals(EXPECTED_SCANCENTRAL_PATH, fodGlobalDescriptor.getScanCentralPath());
    }

    @Test
    @ConfiguredWithCode("org.jenkinsci.plugins.fodupload/FodGlobalDescriptorTest/pat-auth-value-configuration.yml")
    public void fodGlobalDescriptor_PatAuthVaue_configuredWithRule() {

        final FodGlobalDescriptor fodGlobalDescriptor = (FodGlobalDescriptor) jenkinsConfiguredWithRule.jenkins.getDescriptorOrDie(FodGlobalDescriptor.class);

        if (fodGlobalDescriptor.getGlobalAuthType().equals(EXPECTED_PAT_AUTH_TYPE)) {
            assertEquals(EXPECTED_USER_NAME, fodGlobalDescriptor.getUsername());
            assertEquals(EXPECTED_PERSONAL_ACCESS_TOKEN, fodGlobalDescriptor.getPersonalAccessToken());
            assertEquals(EXPECTED_TENANT_ID, fodGlobalDescriptor.getTenantId());
        }
    }

    @Test
    @ConfiguredWithCode("org.jenkinsci.plugins.fodupload/FodGlobalDescriptorTest/apikey-auth-value-configuration.yml")
    public void fodGlobalDescriptor_ApiAuthVaue_configuredWithRule() {

        final FodGlobalDescriptor fodGlobalDescriptor = (FodGlobalDescriptor) jenkinsConfiguredWithRule.jenkins.getDescriptorOrDie(FodGlobalDescriptor.class);

        if (fodGlobalDescriptor.getGlobalAuthType().equals(EXPECTED_API_AUTH_TYPE)) {
            assertEquals(EXPECTED_CLIENT_SECRET, fodGlobalDescriptor.getClientSecret());
            assertEquals(EXPECTED_CLIENT_ID, fodGlobalDescriptor.getClientId());
        }
    }
}
