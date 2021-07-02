class JobSettings {

    constructor (instance) {
        this.instance = instance;
    }

    getSavedApplicationId() {
        return new Promise((res, rej) => {
            if (!instance) {
                return res(null);
            }
            instance.getUserSelectedApplication(t => {
                const appId = t.responseObject();
                res(appId);
            });
        });
    }

    getSavedReleaseId() {
        return new Promise((res, rej) => {
            if (!instance) {
                return res(null);
            }

            instance.getUserSelectedRelease(t => {
                const releaseId = t.responseObject();
                res(releaseId);
            });
        });
    }

    getSavedMicroserviceId() {
        return new Promise((res, rej) => {
            if (!instance) {
                return res(null);
            }

            instance.getUserSelectedMicroservice(t => {
                const msId = t.responseObject();
                res(msId);
            });
        });
    }
}