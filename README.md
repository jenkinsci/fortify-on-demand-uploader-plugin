# FoD-Jenkins
This is a Jenkins plugin for the HPE Fortify on Demand service. An API key from the FoD service is necessary for this plugin to communicate with FoD.

The plugin currently requires that Jenkins be running on Java 1.8, due to SSL cipher suite constraints with FoD.

Current functionality is limited to a post build step that zips up all project artifacts, uploads them to FoD and waits for the scan to complete. Failed scans cause a build to be marked as unstable. Scan failure may be due to technical issues or due to security policy constraints applied to scan results.

More information about Fortify on Demand:
 https://saas.hpe.com/software/fortify-on-demand
