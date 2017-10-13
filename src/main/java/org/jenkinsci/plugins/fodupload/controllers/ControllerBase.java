package org.jenkinsci.plugins.fodupload.controllers;

import org.jenkinsci.plugins.fodupload.FodApiConnection;

class ControllerBase {

    protected FodApiConnection api;

    /**
     * Base constructor for all api controllers
     *
     * @param apiConnection api object (containing client etc.) of controller
     */
    ControllerBase(FodApiConnection apiConnection) {
        this.api = apiConnection;
    }
}
