package org.jenkinsci.plugins.fodupload.controllers;

import org.jenkinsci.plugins.fodupload.FodApi.FodApiConnection;

import java.io.PrintStream;

abstract class ControllerBase {

    protected FodApiConnection apiConnection;

    protected String correlationId;
    protected PrintStream logger;

    /**
     * Base constructor for all apiConnection controllers
     *
     * @param apiConnection apiConnection object (containing client etc.) of controller
     * @param logger logger object
     * @param correlationId correlation id
     */
    ControllerBase(final FodApiConnection apiConnection, final PrintStream logger, final String correlationId) {
        this.apiConnection = apiConnection;
        this.logger = logger;
        this.correlationId = correlationId;
    }

    protected String getCorrelationId() {
        if (this.correlationId == null) {
            return "";
        }

        return this.correlationId;
    }

    protected void println(String log) {
        if (this.logger != null) {
            this.logger.println(log);
        }
        else {
            System.out.println(log);
        }
    }

    protected void printStackTrace(Exception e) {
        if (this.logger != null) {
            e.printStackTrace(this.logger);
        }
        else {
            e.printStackTrace();
        }
    }
}
