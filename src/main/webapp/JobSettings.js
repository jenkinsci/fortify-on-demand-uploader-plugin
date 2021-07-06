class JobSettings {

    constructor (instance) {
        this.instance = instance;
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
}