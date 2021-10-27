# Changelog

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
