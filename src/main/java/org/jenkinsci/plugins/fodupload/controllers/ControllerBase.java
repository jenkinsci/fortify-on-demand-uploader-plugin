package org.jenkinsci.plugins.fodupload.controllers;

import org.jenkinsci.plugins.fodupload.FodApi;

class ControllerBase {

    protected FodApi api;

    /**
     * Base constructor for all api controllers
     *
     * @param api api object (containing client etc.) of controller
     */
    ControllerBase(FodApi api) {
        this.api = api;
    }
}
