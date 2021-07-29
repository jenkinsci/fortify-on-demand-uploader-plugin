jq = jQuery;

class AppAndReleaseSelectionDialog extends Dialog {

    ELEMENTS_PER_ROW = 4;

    constructor() {
        super('appAndReleaseSelectionDialog', 'appAndReleaseSelectionForm');
        this.api = new Api(instance, descriptor);
    }

    clearForm(data) {
        this.jqDialog('#list-container').empty();
        this.jqDialog('#searchBox').val('');
        this.jqDialog('#dialogBody').off('click');
    }

    async preloadData(data) {
        this.startSpinning();
        try {
            switch (data.type) {
                case 'app':
                    await this.populateApplications();
                    break;
                case 'microservice':
                    await this.populateMicroservices(data.applicationId);
                    break;
                case 'release':
                    await this.populateReleases(data.applicationId, data.microserviceId);
                    break;
            }
        }
        finally {
            this.stopSpinning();
        }
    }

    async populateApplications() {
        const apps = await this.api.getApplications({}, getAuthInfo());
        this.displayItems(apps.map(
            app => '<a href="#" class="selectAppLink" data-app-id="' + app.applicationId + '" data-app-name="' + app.applicationName + '" data-app-hasmicroservices="' + app.hasMicroservices + '">' + app.applicationName + '</a>'
        ));
    }

    async populateMicroservices(applicationId) {
        const microservices = await this.api.getMicroservices(applicationId, {}, getAuthInfo());
        this.displayItems(microservices.map(
            ms => '<a href ="#" class="selectMicroserviceLink" data-ms-id="' + ms.microserviceId + '" data-ms-name="' + ms.microserviceName + '">' + ms.microserviceName + '</a>'
        ));
    }

    async populateReleases(applicationId, microserviceId) {
        const releases = await this.api.getReleases(applicationId, microserviceId, {}, getAuthInfo());
        this.displayItems(releases.map(
            r => '<a href="#" class="selectReleaseLink" data-release-id="' + r.releaseId + '" data-release-name="' + r.releaseName + '">' + r.releaseName + '</a>'
        ));
    }

    displayItems(items) {
        this.jqDialog('#list-container').empty();
        for (const item of items) {
            this.jqDialog('#list-container').append('<div class="flex-item">' + item + '</div>');
        }
    }

    subscribeToEvents(data) {
        this.jqDialog('#dialogBody').on('click', '.selectAppLink', (ev) => {
            ev.preventDefault();
            const applicationId = Number(jq(ev.target).attr('data-app-id'));
            const applicationName = jq(ev.target).attr('data-app-name');
            const hasMicroservices = jq(ev.target).attr('data-app-hasmicroservices') == 'true';

            dispatchEvent('dialogSelectedApplication', { applicationId, applicationName, hasMicroservices });
            this.closeDialog();
        });

        this.jqDialog('#dialogBody').on('click', '.selectMicroserviceLink', (ev) => {
            ev.preventDefault();
            const microserviceId = Number(jq(ev.target).attr('data-ms-id'));
            const microserviceName = jq(ev.target).attr('data-ms-name');

            dispatchEvent('dialogSelectedMicroservice', { microserviceId, microserviceName });
            this.closeDialog();
        });

        this.jqDialog('#dialogBody').on('click', '.selectReleaseLink', (ev) => {
            ev.preventDefault();
            const releaseId = Number(jq(ev.target).attr('data-release-id'));
            const releaseName = jq(ev.target).attr('data-release-name');

            dispatchEvent('dialogSelectedRelease', { releaseId, releaseName });
            this.closeDialog();
        });
    }

    onInit() {
        jq('#selectedApp').off('click').click((ev) => {
            ev.preventDefault();
            this.spawnDialog('Select an Application',{
                type: 'app'
            });
        });

        jq('#selectedMicroservice').off('click').click((ev) => {
            ev.preventDefault();
            this.spawnDialog('Select a Microservice', {
                type: 'microservice',
                applicationId: Number(jq('[name="userSelectedApplication"]').val())
            });
        });

        jq('#selectedRelease').off('click').click((ev) => {
            ev.preventDefault();
            const microserviceId = jq('[name="userSelectedMicroservice"]').val();
            this.spawnDialog('Select a Release', {
                type: 'release',
                applicationId: Number(jq('[name="userSelectedApplication"]').val()),
                microserviceId: microserviceId ? Number(microserviceId) : null
            });
        });
    }

    async onDialogSpawn(data) {
        this.clearForm(data);
        this.preloadData(data);
        this.subscribeToEvents(data);
    }
}

createDialog(new AppAndReleaseSelectionDialog());