class Api {

    //<editor-fold desc="Instance operations">

    constructor (instance, descriptor) {
        this.instance = instance;
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

    getReleaseById(releaseId, customAuth) {
        return new Promise((res, rej) => {
            descriptor.retrieveReleaseById(releaseId, customAuth, async t => {
                const responseJSON = JSON.parse(t.responseJSON);
                if (responseJSON === null) {
                    return rej(this.failedToAuthMessage);
                }

                res([responseJSON.success, responseJSON.value]);
            });
        });
    }

    //</editor-fold>

    isAuthError(err) {
        return err == this.failedToAuthMessage;
    }
}