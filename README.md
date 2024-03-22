# Fortify on Demand Jenkins Plugin

[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/fortify-on-demand-uploader.svg)](https://plugins.jenkins.io/fortify-on-demand-uploader/)
[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=plugins/fortify-on-demand-uploader-plugin/master)](https://ci.jenkins.io/job/plugins/job/fortify-on-demand-uploader-plugin/job/master)

Fortify on Demand is a Software as a Service (SaaS) solution that enables your organization to easily and quickly build and expand a Software Security Assurance program. The Fortify on Demand Jenkins Plugin enables users to perform Static Application Security Testing (SAST) and Dynamic Applicaton Security Testing (DAST) from Jenkins. This plugin features the following tasks:

* Run a static assessment for each build triggered by Jenkins.
* Run a DAST Automated assessment for each build triggered by Jenkins.
* Poll for scan status and scan results.

This plugin requires a Fortify on Demand account. For more information on Fortify on Demand and to request a free trial, see https://www.opentext.com/products/fortify-on-demand.

**More Information**

Changelog: https://github.com/jenkinsci/fortify-on-demand-uploader-plugin/blob/master/CHANGELOG.md  
Usage instructions: https://www.microfocus.com/documentation/fortify-on-demand-jenkins-plugin/

**Limitations**
- The DAST Automated asssessment task is a technology preview. The following are known limitations:
	- Fortify on Demand Jenkins Plugin 8.0 supports up to Jenkins 2.401.x. Some UI components, including application and release fields, do not load in versions greater than 2.401.x.
	- (API scan) For some supported Jenkins versions, file and URL fields do not load.
	- (Website scan) Selection of **Enable redundant page detection** is not retained.
	- (Website scan) Excluded URLs are not applied in pipelines.
- The 2.0.9 (Obsolete) plugin version is slow to populate the pull down menu's in Redhat 7 machines.  Please wait a minute or two and the first field should populate.
