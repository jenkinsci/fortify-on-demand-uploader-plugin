class ApplicationSource {

    constructor(api) {
        this.api = api;
    }

    async fetch(search, offset, limit) {
        const res = await this.api.getApplications({
            keyword: search,
            offset,
            limit
        }, getAuthInfo());

        return res;
    }

    getHTMLLink(item) {
        return '<a href="#" class="selectAppLink" data-app-id="' + item.applicationId + '" data-app-name="' + item.applicationName + '" data-app-hasmicroservices="' + item.hasMicroservices + '">' + item.applicationName + '</a>';
    }

    getLinkClass() {
        return 'selectAppLink';
    }

    handleClick(target) {
        const applicationId = Number(target.attr('data-app-id'));
        const applicationName = target.attr('data-app-name');
        const hasMicroservices = target.attr('data-app-hasmicroservices') == 'true';

        dispatchEvent('dialogSelectedApplication', { applicationId, applicationName, hasMicroservices });
    }
}
//
// class MicroserviceSource {
//
//     constructor(api, appId) {
//         this.api = api;
//         this.appId = appId;
//     }
//
//     async fetch(search, offset, limit) {
//         if (!this.cache) {
//             this.cache = await this.api.getMicroservices(this.appId, getAuthInfo());
//         }
//
//         const filtered = search ? this.cache.filter(x => x.microserviceName.toLowerCase().indexOf(search) !== -1) : this.cache;
//         return { totalCount: filtered.length, items: filtered.slice(offset, offset + limit) };
//     }
//
//     getHTMLLink(item) {
//         return '<a href="#" class="selectMicroserviceLink" data-ms-id="' + item.microserviceId + '" data-ms-name="' + item.microserviceName + '">' + item.microserviceName + '</a>';
//     }
//
//     getLinkClass() {
//         return 'selectMicroserviceLink';
//     }
//
//     handleClick(target) {
//         const microserviceId = Number(target.attr('data-ms-id'));
//         const microserviceName = target.attr('data-ms-name');
//
//         dispatchEvent('dialogSelectedMicroservice', { microserviceId, microserviceName });
//     }
// }
//
// class ReleaseSource {
//
//     constructor(api, appId, microserviceId) {
//         this.api = api;
//         this.appId = appId;
//         this.microserviceId = microserviceId;
//     }
//
//     async fetch(search, offset, limit) {
//         const res = await this.api.getReleases(this.appId, this.microserviceId, {
//             keyword: search,
//             offset,
//             limit
//         }, getAuthInfo());
//
//         return res;
//     }
//
//     getHTMLLink(item) {
//         return '<a href="#" class="selectReleaseLink" data-release-id="' + item.releaseId + '" data-release-name="' + item.releaseName + '">' + item.releaseName + '</a>';
//     }
//
//     getLinkClass() {
//         return 'selectReleaseLink';
//     }
//
//     handleClick(target) {
//         const releaseId = Number(target.attr('data-release-id'));
//         const releaseName = target.attr('data-release-name');
//
//         dispatchEvent('dialogSelectedRelease', { releaseId, releaseName });
//     }
// }

class PipelineReleaseSelectionDialog extends Dialog {

    SIZE_PER_PAGE = 25;

    constructor() {
        super('pipelineReleaseSelectionDialog', 'pipelineReleaseSelectionForm');
        this.api = new Api(null, descriptor);
    }

    clearForm() {
        this.jqDialog('#searchBox').val('');
        this.emptyContainers();
    }

    emptyContainers() {
        this.jqDialog('#list-container').empty();
        this.jqDialog('#paging-container').hide();
        this.jqDialog('#errors-container').hide();
        this.jqDialog('#empty-container').hide();
    }

    subscribeToEvents(source, search) {
        this.jqDialog('#dialogBody').off('click');

        this.jqDialog('#dialogBody').on('click', '.' + source.getLinkClass(), (ev) => {
            ev.preventDefault();
            source.handleClick(jq(ev.target));
            this.closeDialog();
        });

        this.jqDialog('#prevPageLink').off('click').click((ev) => {
            ev.preventDefault();
            const page = Number(this.jqDialog('#pageNumber').html());
            this.loadPage(source, search, page - 1);
        });

        this.jqDialog('#nextPageLink').off('click').click((ev) => {
            ev.preventDefault();
            const page = Number(this.jqDialog('#pageNumber').html());
            this.loadPage(source, search, page + 1);
        });

        this.jqDialog('#searchBox').off('keyup').keyup((ev) => {
            if (ev.keyCode === 13) {
                let searchTerm = this.jqDialog('#searchBox').val();
                if (searchTerm) {
                    searchTerm = searchTerm.trim();
                }
                if (searchTerm == "") {
                    searchTerm = null;
                }

                this.subscribeToEvents(source, searchTerm);
                this.loadPage(source, searchTerm, 1);
            }
        })
    }

    async loadPage(source, search, page) {
        try {
            this.startSpinning();
            this.emptyContainers();

            const pageSize = this.SIZE_PER_PAGE;
            const {totalCount, items} = await source.fetch(search, (page - 1) * pageSize, pageSize);

            if (totalCount == 0) {
                this.jqDialog('#empty-container').show();
            }

            if (totalCount <= pageSize) {
                this.jqDialog('#paging-container').hide();
            } else {
                this.jqDialog('#paging-container').show();

                if (page == 1) {
                    this.jqDialog('#prevPageLink').hide();
                } else {
                    this.jqDialog('#prevPageLink').show();
                }

                if (page * pageSize >= totalCount) {
                    this.jqDialog('#nextPageLink').hide();
                } else {
                    this.jqDialog('#nextPageLink').show();
                }

                this.jqDialog('#pageNumber').html(page);
            }

            this.displayItems(items.map(x => source.getHTMLLink(x)));
        }
        catch (e) {
            console.error(e);
            this.jqDialog('#errors-container').show();
        }
        finally {
            this.stopSpinning();
        }
    }

    displayItems(items) {
        this.jqDialog('#list-container').empty();
        for (const item of items) {
            this.jqDialog('#list-container').append('<div class="flex-item">' + item + '</div>');
        }
    }

    onInit() {
        jq('#releaseLookup').off('click').click((ev) => {
            ev.preventDefault();
            this.spawnDialog('Select an Application', new ApplicationSource(this.api));
        });
    }

    async onDialogSpawn(source) {
        this.clearForm();
        this.subscribeToEvents(source, null);
        await this.loadPage(source, null, 1);
    }
}

createDialog(new PipelineReleaseSelectionDialog());