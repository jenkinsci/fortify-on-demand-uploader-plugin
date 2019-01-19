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

import jenkins.model.GlobalConfiguration;
import org.jenkinsci.plugins.fodupload.FodApiConnection;
import org.jenkinsci.plugins.fodupload.models.AuthenticationModel;
import org.jenkinsci.plugins.fodupload.models.FodEnums;
import org.jenkinsci.plugins.fodupload.models.JobModel;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
/**
 *
 * @author tsotack
 */
public class ApiConnectionFactory {
    
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public static FodApiConnection createApiConnection(AuthenticationModel model) {
        FodApiConnection apiConnection = null;
        if(GlobalConfiguration.all() != null && GlobalConfiguration.all().get(FodGlobalDescriptor.class) != null){
            if(model.getOverrideGlobalConfig())
            {

                    String baseUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl();
                    String apiUrl = GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl();
                    if (Utils.isNullOrEmpty(baseUrl))
                        throw new IllegalArgumentException("Base URL is null.");
                    if (Utils.isNullOrEmpty(apiUrl))
                        throw new IllegalArgumentException("Api URL is null.");
                    apiConnection =  new FodApiConnection(model.getTenantId() + "\\" + model.getUsername(),
                                                      model.getPersonalAccessToken(), 
                                                      baseUrl,
                                                      apiUrl, 
                                                      FodEnums.GrantType.PASSWORD,
                                                      "api-tenant");

            }
            else
            {
                apiConnection = GlobalConfiguration.all().get(FodGlobalDescriptor.class).createFodApiConnection();
            }
        }
        return apiConnection;
    }
    
}
