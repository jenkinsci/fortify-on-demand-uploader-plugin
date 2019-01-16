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
/**
 *
 * @author tsotack
 */
public class ApiConnectionFactory {
    
    public static FodApiConnection createApiConnection(AuthenticationModel model) {
        FodApiConnection apiConnection = null;
        if(model.getProjectAuthType().equals("personalAccessTokenType"))
        {
            apiConnection =  new FodApiConnection(model.getTenantId() + "\\" + model.getUsername(),
                                                  model.getPersonalAccessToken(), 
                                                  GlobalConfiguration.all().get(FodGlobalDescriptor.class).getBaseUrl(),
                                                  GlobalConfiguration.all().get(FodGlobalDescriptor.class).getApiUrl(), 
                                                  FodEnums.GrantType.PASSWORD,
                                                  "api-tenant");
        }
        else if(model.getProjectAuthType() == "useGlobalAuthType")
        {
            apiConnection = GlobalConfiguration.all().get(FodGlobalDescriptor.class).createFodApiConnection();
        }
        return apiConnection;
    }
    
}
