class Api {

    //<editor-fold desc="Instance operations">

    constructor (instance, descriptor) {
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

    getMicroservices(appId, searchArgs, customAuth) {
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

    //</editor-fold>

    isAuthError(err) {
        return err == this.failedToAuthMessage;
    }
}