class Api {

    //<editor-fold desc="Instance operations">

    constructor(instance, descriptor) {
        this.instance = instance;
        this.descriptor = descriptor;
        this.failedToAuthMessage = 'Failed to authenticate';
    }

    getSavedSelectedApplicationId() {
        return this._getSavedEntity(i => i.getUserSelectedApplication);
    }

    getSavedSelectedReleaseId() {
        return this._getSavedEntity(i => i.getUserSelectedRelease);
    }

    getSavedSelectedMicroserviceId() {
        return this._getSavedEntity(i => i.getUserSelectedMicroservice);
    }

    getSavedReleaseId() {
        return this._getSavedEntity(i => i.getReleaseId);
    }

    getSavedBsiToken() {
        return this._getSavedEntity(i => i.getBsiToken);
    }

    _getSavedEntity(instanceOperation) {
        return new Promise((res, rej) => {
            if (!instance) {
                return res(null);
            }

            instanceOperation(instance)(t => {
                const val = t.responseObject();
                res(val);
            });
        });
    }

    //</editor-fold>

    //<editor-fold desc="Descriptor operations">

    getApplications(searchArgs, customAuth) {
        let searchTerm = searchArgs?.keyword ?? null;
        if (searchTerm == "")
            searchTerm = null;

        return new Promise((res, rej) => {
            this.descriptor.retrieveApplicationList(searchTerm, searchArgs?.offset ?? 0, searchArgs?.limit ?? 25, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);
                if (responseJSON == null) {
                    return rej(this.failedToAuthMessage);
                }

                res(responseJSON);
            });
        });
    }

    getMicroservices(appId, customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveMicroserviceList(appId, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);
                if (responseJSON == null) {
                    return rej(this.failedToAuthMessage);
                }

                res(responseJSON);
            });
        });
    }

    getReleases(appId, microserviceId, searchArgs, customAuth) {
        let searchTerm = searchArgs?.keyword ?? null;
        if (searchTerm == "")
            searchTerm = null;

        return new Promise((res, rej) => {
            this.descriptor.retrieveReleaseList(appId, microserviceId ?? 0, searchTerm, searchArgs?.offset ?? 0, searchArgs.limit ?? 25, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);
                if (responseJSON == null) {
                    return rej(this.failedToAuthMessage);
                }

                res(responseJSON);
            });
        });
    }

    getReleaseById(releaseId, customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveReleaseById(releaseId, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);
                if (responseJSON === null) {
                    return rej(this.failedToAuthMessage);
                }

                res([responseJSON.success, responseJSON.value, responseJSON.errors]);
            });
        });
    }

    getCurrentUserSession(customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveCurrentUserSession(customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);
                if (responseJSON === null || (!responseJSON.success && responseJSON.reason === 'no_auth')) {
                    return rej(this.failedToAuthMessage);
                }

                if (!responseJSON.success) {
                    return res(null);
                }

                return res(responseJSON.value);
            });
        });
    }

    getStaticScanSettings(releaseId, customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveStaticScanSettings(releaseId, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);

                return res(responseJSON);
            });
        });
    }

    getAssessmentTypeEntitlements(releaseId, customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveAssessmentTypeEntitlements(releaseId, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);

                if (responseJSON === null) return res(null);

                return res(this._mutateAssessmentEntitlements(responseJSON));
            });
        });
    }

    getAssessmentTypeEntitlementsForAutoProv(appName, relName, customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveAssessmentTypeEntitlementsForAutoProv(appName, relName, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);

                if (responseJSON === null) return res(null);

                responseJSON.assessments = this._mutateAssessmentEntitlements(responseJSON.assessments);

                return res(responseJSON);
            });
        });
    }

    _mutateAssessmentEntitlements(responseJSON){
        if (Array.isArray(responseJSON) && responseJSON.length > 0) {
            let assessments = {};

            for (let ae of responseJSON) {
                if (ae.isRemediation) continue;
                let assessment = assessments[ae.assessmentTypeId];

                if (!assessment) {
                    assessment = {
                        id: ae.assessmentTypeId,
                        name: ae.name,
                        entitlements: {},
                        entitlementsSorted: []
                    };
                    assessments[ae.assessmentTypeId] = assessment;
                }
                let entitlement = {
                    id: ae.entitlementId,
                    frequency: ae.frequencyType,
                    frequencyId: ae.frequencyTypeId,
                    units: ae.units,
                    unitsAvailable: ae.unitsAvailable,
                    subscriptionEndDate: Date.parse(ae.subscriptionEndDate),
                    isBundledAssessment: ae.isBundledAssessment,
                    parentAssessmentTypeId: ae.parentAssessmentTypeId,
                    description: ae.entitlementDescription,
                    sortValue: (ae.frequencyType === 'Subscription' ? 0 : 1).toString() + '_' + ae.entitlementDescription.toLowerCase()
                };

                assessment.entitlements[ae.entitlementId] = entitlement;
                assessment.entitlementsSorted.push(entitlement);
            }

            for (let k of Object.keys(assessments)) {
                assessments[k].entitlementsSorted = assessments[k].entitlementsSorted.sort((a, b) => a.sortValue < b.sortValue ? -1 : 0);
            }

            return assessments;
        }

        return null;
    }

    getReleaseEntitlementSettings(releaseId, customAuth) {
        return new Promise((res, rej) => {
            this.descriptor.retrieveStaticScanSettings(releaseId, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);

                return res(responseJSON);
            });
        });
    }

    getTechStacks(customAuth) {
        return new Promise((res, rej) => {
            let techStacks;
            let langLevels;

            let ttprom = new Promise((ttres, ttrej) => {
                this.descriptor.retrieveLookupItems('TechnologyTypes', customAuth, t => {
                    techStacks = JSON.parse(t.responseJSON);
                    ttres();
                });
            });
            let llprom = new Promise((llres, llrej) => {
                this.descriptor.retrieveLookupItems('LanguageLevels', customAuth, t => {
                    langLevels = JSON.parse(t.responseJSON);
                    llres();
                });
            });

            // ToDo: handle auth errors
            Promise.all([ttprom, llprom])
                .then(_ => {
                    let result = {};

                    if (Array.isArray(techStacks) && Array.isArray(langLevels)) {
                        for (let tt of techStacks) {
                            result[tt.value] = {...tt, levels: []};
                        }
                        for (let ll of langLevels) {
                            let tt = result[ll.group];

                            if (tt) tt.levels.push(ll);
                        }

                        res(result);
                    } else rej('Invalid response');
                })
                .catch(rej);
        });
    }

    //</editor-fold>

    isAuthError(err) {
        return err == this.failedToAuthMessage;
    }
}