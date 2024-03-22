# Changelog
## Version 8.0 (03-13-2024)
- Added support for DAST Automated assessments (technology preview). For freestyle projects, the Fortify on Demand Dynamic Assessment post-build action has been added. For pipelines, the fodDynamicAssessment task has been added.
- Added support for polling for DAST Automated scan results.
- Added **dotnet** as a **ScanCentral Build Type** value.
- Added **Exclude Files** field for packaging with Fortify ScanCentral SAST.

**Important**: 
The DAST Automated asssessment task is a technology preview. The following are known limitations:
- Fortify on Demand Jenkins Plugin 8.0 supports up to Jenkins 2.401.x. Some UI components, including application and release fields, do not load in versions greater than 2.401.x.
- (API scan) For some supported Jenkins versions, file and URL fields do not load.
- (Website scan) Selection of **Enable redundant page detection** is not retained.
- (Website scan) Excluded URLs are not applied in pipelines.

## Version 7.2 (08-08-2023)
- Added support for new technology stacks.
- Added support for remote nodes.
- Added PATH field for specifying the Fortify ScanCentral SAST client directory.
- Upgraded libraries to resolve security vulnerabilities.

## Version 7.1.2 (03-14-2023)
- Added support for packaging files required for Debricked open source scanning with Fortify ScanCentral SAST.
- Increased minimum required Jenkins version to 2.289

## Version 7.1.1 (09-22-2022)
- Added support for Jenkins Configuration as Code.
- Added support for Fortify On Demand URL validation.
- Fixed issue where Kotlin files were not being packaged.

## Version 7.0.3 (05-17-2022)
- Fixed issue where a validation error occurred when using the BSI token in new freestyle projects.

## Version 7.0.2 (03-31-2022)
- Fixed issue where fields were not populating correctly in Jenkins version 2.338.

## Version 7.0.1 (02-23-2022)
- Added support for packaging Go projects with Fortify ScanCentral.
- Removed requirement to use Fortify ScanCentral to package .NET, Java, and PHP projects.

## Version 7.0.0 (01-21-2022)
- Added new options to create or select an application and release, configure scan settings, and invoke Fortify ScanCentral SAST to package application files.
- Added default values for the following pipeline parameters: **RemediationScanPreferenceType** (default **RemediationScanIfAvailable**), **InProgressScanActionType** (default **DoNotStartScan**), **InProgressBuildResultType** (default **FailBuild**)
- Fixed **Build result if scan in progress** field to populate saved selection.
- Fixed issue where polling status displayed **Not Started** for long running scans.
- Fixed issue where saved scan settings were not populated after the Jenkins machine was restarted.
 
## Version 6.1.0 (09-02-2020)
- Added the option to queue a new scan if a scan is in progress.
- Added the option to fail the build or provide a warning if the task fails.

## Version 6.0.1 (06-20-2020)
- Fixed SECURITY-1690 / CVE-2020-2202 and SECURITY-1691 / CVE-2020-2203, CVE-2020-2204

## Version 6.0.0 (06-24-2020)
- Added the ability to specify release ID in place of BSI token.
- Added additional file types to be included in payloads for all technology stacks.

## Version 5.0.1 (01-30-2020)
- Increased timeouts.

## Version 5.0.0 (12-04-2019)
- Added support for the Credentials Plugin.
- Fixed **Action when scan is in progress** field to populate saved data.

## Version 4.0.1 (10-22-2019)
- Fixed SECURITY-1433 / CVE-2019-10449.

## Version 4.0.0 (10-14-2019)
- Removed obsolete and redundant fields.
- Added the ability to specify the directory of the files to upload.
- Updated the **Entitlement Preference** and **Remediation Preference** options.
- Added the ability to cancel an in-progress scan and start a new scan or not start a new scan and mark the build as unstable.
- When a Jenkins build polls for scan status, scan pause and scan cancellation updates now include additional details.
- Polling now stops once a scan is cancelled, completed, or paused. 
- Details of a scan started through the Fortify on Demand Jenkins Plugin include the scan's method of origination. 
- Updated marketplace details.

## Version 3.0.12 (4-05-2019)
- Added support for pipelines. The fodStaticAssessment and fodPollResults tasks have been added.

## Version 3.0.11 (3-22-2019)
-   Fixed SSRF vulnerability.

## Version 3.0.1 (10-09-2017)
**Upgrade Note**: Please be aware that builds will need to be
reconfigured with the BSI Url/Token.

-   Scans are now configured with the BSI Url/Token from the Static Scan Setup page of the release to be scanned in the Fortify on Demand Portal.

## Version 2.0.6 (1-06-2017)
-   Fixed bug that causes plugin to crash configuration pages when
    incomplete information was saved.

## Version 2.0 (4-28-2016)
-   Fixed bug when that causes plugin to crash when particular proxy
    configurations cause authentication to fail.
-   Finalized update to FoD API V3

## Version 1.10 (4-28-2016)
**Bug Fix**: This release addresses a rare issue in which release
information may not be retrieved for certain applications.

-   Corrected encoding issue for application names which can prevent
    release information calls from working properly
-   Additional validation for global polling interval
-   Removed unsupported language level settings for .NET and Java

## Version 1.09 (4-25-2016)
-   Code changes to resolve distributed Jenkins defect (credit to Ruud
    Senden)
-   Minor language support changes in preparation for potential new
    mobile assessment types

## Version 1.08 (4-15-2016)
-   Added support for Jenkins proxy configuration
-   Added connection configuration test button that validates
    reachability of the portal and tests credentials

## Version 1.07 (4-06-2016)
-   Added option to include/exclude identified third-party libraries
    from analysis results
-   Changed order, and description, of advanced options for consistency
    with the Fortify on Demand portal
-   Polling for results is no longer default. Applications set to poll
    will reflect your organization's security policy in Jenkins via
    build stability.
-   Minor branding changes

## Version 1.06
**Bug Fix**: This release addresses a bug where the Assessment Type
may not correctly set under certain conditions

-   Assessment Type no longer has a suggested default selection; the
    user must choose the proper type for enabled entitlement
-   Added .NET as a supported language to Sonatype help text

## Version 1.05
**Upgrade Note**: please ensure you reconfigure any existing builds so
that the filer filter may be set by the plugin; this functionality has
changed with this version.

-   Added support for all language/assessment types except MBS and
    C/C++, which require pre-processing with Fortify SCA prior to
    submission to Fortify on Demand
-   Files selected for upload are automatically set based on language
    type and Fortify on Demand requirements; users may opt to package
    all files, including extraneous types like media, under advanced
    options. Using the automated default is *highly* encouraged
-   The result report link added with the Detailed Reports option now
    refers to the Overview page in the Customer Portal

## Version 1.04
**Upgrade Note**: please ensure you reconfigure any existing builds so
that *Assessment Type* may be set by the plugin as this field is new
with this version.

-   Static-related assessment types may be selected at upload, defaults
    to "Static Assessment"
-   API calls for information lookup are now more resilient with
    retries, and have additional logging of any issues, e.g. lack of
    assessment entitlement

## Version 1.03
-   Star rating and total issue count display in the standard log
    results
-   Detailed build log table output includes a deep link to the FoD
    customer portal for the application release, issue counts by
    criticality, and Fortify on Demand star rating
-   Minor code cleanup for readability

## Version 1.02
-   Minor branding changes
-   Updated UI API token secret validation due to changed 5.0 portal
    format

## Version 1.01
-   Initial release
