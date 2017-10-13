package org.jenkinsci.plugins.fodupload.controllers;

import org.jenkinsci.plugins.fodupload.FodApiConnection;

abstract class ControllerBase {

    protected FodApiConnection apiConnection;

    /**
     * Base constructor for all apiConnection controllers
     *
     * @param apiConnection apiConnection object (containing client etc.) of controller
     */
    ControllerBase(FodApiConnection apiConnection) {
        this.apiConnection = apiConnection;
    }
}
