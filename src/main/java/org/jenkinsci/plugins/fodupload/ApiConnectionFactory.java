/*
 * The MIT License
 *
 * Copyright 2018 tsotack.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.fodupload;

import hudson.Launcher;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.PrintStream;

import static org.jenkinsci.plugins.fodupload.Utils.FOD_URL_ERROR_MESSAGE;
import static org.jenkinsci.plugins.fodupload.Utils.isValidUrl;

/**
 * @author tsotack
 */
public class ApiConnectionFactory {

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static FodApiConnection createApiConnection(AuthenticationModel model, boolean executeOnRemoteAgent, Launcher launcher, PrintStream logger) throws FormValidation {
        FodApiConnection apiConnection = null;
        if (GlobalConfiguration.all() != null && GlobalConfiguration.all().get(FodGlobalDescriptor.class) != null) {
            if (model.getOverrideGlobalConfig()) {

                String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
                String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
                if (Utils.isNullOrEmpty(isValidUrl(baseUrl)))
                    throw new IllegalArgumentException(FOD_URL_ERROR_MESSAGE);
                if (Utils.isNullOrEmpty(isValidUrl(apiUrl)))
                    throw new IllegalArgumentException(FOD_URL_ERROR_MESSAGE);
                apiConnection = new FodApiConnection(model.getTenantId() + "\\" + model.getUsername(),
                        Utils.retrieveSecretDecryptedValue(model.getPersonalAccessToken()),
                        baseUrl,
                        apiUrl,
                        FodEnums.GrantType.PASSWORD,
                        "api-tenant",
                        executeOnRemoteAgent, launcher, logger);

            } else {
                apiConnection = GlobalConfiguration.all().get(FodGlobalDescriptor.class).createFodApiConnection(executeOnRemoteAgent, launcher, logger);
            }
        }
        return apiConnection;
    }

}
