const fodpRowSelector = '.fodp-field-row, .fodp-field-row-verr';
const fodpOverrideRowsSelector = '.fodp-row-relid-ovr';
const fodpAutoProvRowsSelector = '.fodp-row-autoProv';
const appAttributeKeyDelimiter = ';';
const appAttributeKeyValueDelimiter = ':';

class PipelineGenerator {
    constructor() {
        this.api = new Api(null, descriptor);
        this.currentSession = null
        this.appAttributes = {};
        this.autoProvMode = false;
        this.overrideServerSettings = false;
        this.overrideAuth = false;
        this.releaseId = null;
        this.uiLoaded = false;
        this.techStacks = {};
        this.techStacksSorted = [];
    }

    showMessage(msg, isError) {
        let msgElem;

        if (isError) msgElem = jq('#fodp-error');
        else msgElem = jq('#fodp-msg');

        msgElem.text(msg);
        msgElem.show();
    }

    hideMessages() {
        jq('#fodp-error').hide();
        jq('#fodp-msg').hide();
    }

    populateAssessmentsDropdown() {
        let atsel = jq(`#assessmentTypeSelect`);

        atsel.find('option').remove();
        jq(`#entitlementSelect`).find('option').remove();

        if (this.assessments) {
            for (let k of Object.keys(this.assessments)) {
                let at = this.assessments[k];

                atsel.append(`<option value="${at.id}">${at.name}</option>`);
            }
        }
    }

    onAssessmentChanged() {
        let atval = jq('#assessmentTypeSelect').val();
        let entsel = jq('#entitlementSelect');
        let apsel = jq('#auditPreferenceSelect');
        let at = this.assessments ? this.assessments[atval] : null;

        entsel.find('option,optgroup').remove();

        if (at) {
            let available = at.entitlementsSorted.filter(e => e.id > 0);

            for (let e of available) {
                entsel.append(`<option value="${getEntitlementDropdownValue(e.id, e.frequencyId)}">${e.description}</option>`);
            }
        }

        if (at && at.name === 'Static+ Assessment') apsel.prop('disabled', false);
        else {
            apsel.val('2');
            apsel.prop('disabled', true);
        }

        this.onEntitlementChanged();
    }

    onEntitlementChanged() {
        this.populateHiddenFields();
    }

    async loadReleaseEntitlementSettings() {
        let rows = jq(fodpOverrideRowsSelector);

        this.hideMessages();

        if (!this.releaseId) {
            rows.hide();
            this.showMessage('Enter release id', true);
            return;
        }

        let fields = jq('.fodp-field.spinner-container');

        fields.addClass('spinner');
        rows.show();
        this.onScanCentralChanged();

        // ToDo: deal with overlapping calls
        let ssp = this.api.getReleaseEntitlementSettings(this.releaseId, getAuthInfo())
            .then(r => this.scanSettings = r);
        let entp = this.api.getAssessmentTypeEntitlements(this.releaseId, getAuthInfo())
            .then(r => this.assessments = r);


        await Promise.all([ssp, entp]);

        if (this.scanSettings && this.assessments) this.setAssessmentsAndSettings();
        else {
            this.onAssessmentChanged();
            this.showMessage('Failed to retrieve scan settings from API', true);
            rows.hide();
        }

        fields.removeClass('spinner');
    }

    async loadAutoProvEntitlementSettings(appName, relName, isMicro, microName) {
        let fields = jq('.fodp-field.spinner-container');

        fields.addClass('spinner');
        this.onScanCentralChanged();

        let assessments = await this.api.getAssessmentTypeEntitlementsForAutoProv(appName, relName, isMicro, microName, getAuthInfo());
        let fail = () => {
            fields.removeClass('spinner');
            this.onAssessmentChanged();
            this.showMessage('Failed to retrieve available entitlements from API', true);
            return false;
        };

        if (assessments == null) return fail();

        this.assessments = assessments.assessments;

        if (this.assessments) {
            this.scanSettings = assessments.settings;
            this.releaseId = assessments.releaseId;
        } else {
            this.scanSettings = null;
            this.releaseId = null;
        }

        this.setAssessmentsAndSettings();

        fields.removeClass('spinner');

        if (this.assessments) return true;
        else return fail();
    }

    setAssessmentsAndSettings() {
        let assmt = null;
        let entl = null;
        let freq = null;
        let tech = null;
        let lang = null;
        let audit = null;
        let sona = false;

        if (this.scanSettings) {
            assmt = this.scanSettings.assessmentTypeId;
            entl = this.scanSettings.entitlementId;
            freq = this.scanSettings.entitlementFrequencyType;
            tech = this.scanSettings.technologyStackId;
            lang = this.scanSettings.languageLevelId;
            audit = this.scanSettings.auditPreferenceType;
            sona = this.scanSettings.performOpenSourceAnalysis === true;
        }

        this.populateAssessmentsDropdown();

        jq('#assessmentTypeSelect').val(assmt);
        this.onAssessmentChanged();
        jq('#entitlementSelect').val(freq && entl ? getEntitlementDropdownValue(entl, freq) : '');
        this.onEntitlementChanged();

        let scval = this.getScanCentralBuildTypeSelected();

        if (scval === _scanCentralBuildTypes.None) {
            jq('#technologyStackSelect').val(tech);
            this.onTechStackChanged();
            jq('#languageLevelSelect').val(lang);
            this.onScanCentralChanged();
        } else {
            switch (tech) {
                case 1:
                case 23:
                    this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.MSBuild);
                    jq('#technologyStackSelectList').val(tech);
                    break;
                case 7:
                    // Selected release is Java, but SC was not set to None, Maven, nor Gradle, default to Maven
                    if (scval !== _scanCentralBuildTypes.Maven && scval !== _scanCentralBuildTypes.Gradle) this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.Maven);
                    break;
                case 9:
                    this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.PHP);
                    break;
                case 10:
                    this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.Python);
                    break;
                default:
                    // It's a valid tech stack but not supported by SC
                    if (tech > 0) this.setScanCentralBuildTypeSelected(_scanCentralBuildTypes.None);
                    break;
            }

            if (this.getScanCentralBuildTypeSelected() === _scanCentralBuildTypes.None) {
                jq('#technologyStackSelect').val(tech);
                this.onTechStackChanged();
                jq('#languageLevelSelect').val(lang);
                this.onScanCentralChanged();
            } else {
                this.onScanCentralChanged();
                if (lang) jq('#languageLevelSelectList').val(lang);
                this.onLangLevelChanged();
            }
        }

        jq('#auditPreferenceSelect').val(audit);
        jq('#sonatypeEnabled').prop('checked', sona);
    }

    getScanCentralBuildTypeSelected() {
        return _scanCentralBuildTypes[jq('#scanCentralBuildTypeSelect').val()] || _scanCentralBuildTypes.None;
    }

    setScanCentralBuildTypeSelected(val) {
        if (val && _scanCentralBuildTypes[val]) jq('#scanCentralBuildTypeSelect').val(val);
    }

    isDotNetStack(ts) {
        // noinspection EqualityComparisonWithCoercionJS
        return ts.value == techStackConsts.dotNet || ts.value == techStackConsts.dotNetCore;
    }

    onScanCentralChanged() {
        let val = jq('#scanCentralBuildTypeForm > select').val().toLowerCase();
        let techStackFilter;
        this.populateTechStackDropdown();
        if (val === 'none') {
            jq('.fodp-row-sc').hide();
            if (this.overrideServerSettings || this.autoProvMode) jq('.fodp-row-nonsc').show();
        } else {
            let scClass = 'fodp-row-sc-' + val;

            jq('.fodp-row-nonsc').hide();

            jq('.fodp-row-sc')
                .each((i, e) => {
                    let jqe = jq(e);

                    if (jqe.hasClass(scClass)) jqe.show();
                    else jqe.hide();
                });
            switch (val) {
                case 'msbuild':
                    if (this.overrideServerSettings || this.autoProvMode) {
                        closestRow(jq('#technologyStackForm')).show();
                        let currVal = this.techStacks[jq('#technologyStackSelect').val()];
                        if (!currVal || !this.isDotNetStack(currVal)) jq('#technologyStackSelect').val(techStackConsts.none);
                        techStackFilter = this.isDotNetStack;
                    }
                    break;
                case 'maven':
                case 'gradle':
                    if (this.overrideServerSettings || this.autoProvMode) jq('#technologyStackSelect').val(techStackConsts.java);
                    break;
                case 'php':
                    if (this.overrideServerSettings || this.autoProvMode) jq('#technologyStackSelect').val(techStackConsts.php);
                    break;
                case 'python':
                    if (this.overrideServerSettings || this.autoProvMode) jq('#technologyStackSelect').val(techStackConsts.python);
                    break;
            }
        }
        if (techStackFilter) this.populateTechStackDropdown(techStackFilter);
        this.onTechStackChanged();
    }

    populateTechStackDropdown(filter) {
        let tsSel = jq('#technologyStackSelect');
        let currVal = tsSel.val();
        let currValSelected = false;

        tsSel.find('option').not(':first').remove();
        tsSel.find('option').first().prop('selected', true);


        for (let ts of this.techStacksSorted) {
            if (filter && filter(ts) !== true) continue;

            // noinspection EqualityComparisonWithCoercionJS
            if (currVal == ts.value) {
                currValSelected = true;
                tsSel.append(`<option value="${ts.value}" selected>${ts.text}</option>`);
            } else tsSel.append(`<option value="${ts.value}">${ts.text}</option>`);
        }

        if (!currValSelected) {
            tsSel.find('option').first().prop('selected', true);
            this.onTechStackChanged();
        }
    }

    onTechStackChanged() {
        let ts = this.techStacks[jq('#technologyStackSelect').val()];
        let llv = numberOrNull(jq('#languageLevelSelect').val()) || -1;
        let llsel = jq('#languageLevelSelect');
        let llr = jq('.fodp-row-langLev');

        if (this.overrideServerSettings || this.autoProvMode) llr.show();
        llsel.find('option').not(':first').remove();
        llsel.find('option').first().prop('selected', true);

        if (ts) {
            if (Array.isArray(ts.levels) && ts.levels.length > 0) {
                let setllv = false;

                for (let ll of ts.levels) {
                    if (ll.value == llv) setllv = true;
                    llsel.append(`<option value="${ll.value}">${ll.text}</option>`);
                }

                if (setllv) jq('#languageLevelSelect').val(llv);
            } else llr.hide();
        }

        this.onLangLevelChanged();
    }

    onLangLevelChanged() {
        let bt = jq('#scanCentralBuildTypeForm > select').val();
        let ssv = jq('#buildToolVersionForm > input');

        if (bt === 'Python') {
            let ll = this.techStacks[techStackConsts.python].levels.find(e => e.value == jq('#languageLevelSelect').val());

            if (ll && ll.text) ssv.val(ll.text.replace(' (Django)', ''));

            ssv.data('python', true);
        } else if (ssv.data('python')) {
            ssv.removeData();
            ssv.val('');
        }

        this.populateHiddenFields();
    }

    onReleaseIdChanged() {
        if (this.overrideServerSettings) {
            this.releaseId = numberOrNull(jq('#releaseSelectionValue').val());

            if (this.releaseId < 1) this.releaseId = null;

            this.loadReleaseEntitlementSettings();
        } else {
            this.onAssessmentChanged();
            this.hideMessages();
            jq(fodpOverrideRowsSelector).hide();
        }
    }

    async onReleaseSelectionChanged() {
        let rs = jq('#releaseSelection').val();

        jq('.fodp-row-relid').hide();
        jq('.fodp-row-relid-ovr').hide();
        jq('.fodp-row-bsi').hide();
        jq('.fodp-row-autoProv').hide();
        jq('#releaseLookup').hide();

        jq('#releaseSelectionValue').show();
        this.autoProvMode = false;

        if (rs === '1') {
            jq('#overrideReleaseSettings').prop('checked', false);
            jq('.fodp-row-bsi').show();
        } else if (rs === '2') {
            this.hideMessages();
            if (!await this.retrieveCurrentSession()) {
                this.showMessage('Failed to retrieve auth data');
                return;
            }

            if (this.currentSession && this.currentSession.userId) jq('#autoProvOwnerAssignMe').show();
            else jq('#autoProvOwnerAssignMe').hide();

            jq('#overrideReleaseSettings').prop('checked', false);
            jq('#releaseSelectionValue').hide();
            this.autoProvMode = true;
            jq('.fodp-row-autoProv').show();
            this.onIsMicroserviceChanged();
        } else {
            jq('.fodp-row-relid').show();
            jq('#releaseLookup').show();
        }

        this.loadEntitlementOptions();
    }

    async retrieveCurrentSession() {
        if (this.currentSession === null) {
            try {
                this.currentSession = await this.api.getCurrentUserSession(getAuthInfo());
                return this.currentSession !== null;
            } catch (e) {
                return false;
            }
        }
        return true;
    }

    async loadEntitlementOptions() {
        if (this.autoProvMode) {
            this.hideMessages();

            let appName = jq('#autoProvAppName').val();
            let relName = jq('#autoProvRelName').val();
            let isMicro = jq('#autoProvIsMicro').prop('checked');
            let microName = jq('#autoProvMicroName').val();

            if (isNullOrEmpty(appName) || isNullOrEmpty(relName)) {
                this.showMessage('Enter Application and Release names', true);
                jq(fodpOverrideRowsSelector).hide();
            } else if (isMicro && isNullOrEmpty(microName)) {
                this.showMessage('Enter Microservice Name', true);
                jq(fodpOverrideRowsSelector).hide();
            } else if (await this.loadAutoProvEntitlementSettings(appName, relName, isMicro, microName)) jq(fodpOverrideRowsSelector).show();
            else jq(fodpOverrideRowsSelector).hide();
        } else {
            this.overrideServerSettings = jq('#overrideReleaseSettings').prop('checked');
            jq(fodpOverrideRowsSelector).show();
            this.onReleaseIdChanged();
        }

        this.onScanCentralChanged();
        this.populateHiddenFields();
    }

    onAssignMeClick() {
        if (this.currentSession) jq('#autoProvOwner').val(this.currentSession.userId);
    }

    onIsMicroserviceChanged() {
        let v = jq('#autoProvIsMicro').prop('checked');

        if (v) jq('.fodp-row-autoProv-micro').show();
        else jq('.fodp-row-autoProv-micro').hide()
    }

    onIsApplicationTypeChanged() {
        let v = jq('#autoProvAppType').val();
        if (v != 2) {
            jq('.fodp-row-autoProv-is-micro').show();
        } else {
            jq('.fodp-row-autoProv-is-micro').hide();
            jq('.fodp-row-autoProv-micro').hide();
        }
    }


    async onAuthChanged() {
        if (!this.uiLoaded) await this.init();
        else {
            this.currentSession = null;
            await this.onReleaseSelectionChanged();
        }
        this.populateHiddenFields();
    }

    getHiddenFieldSelectValue(sel) {
        let v = numberOrNull(jq(sel).val());

        return v && v > 0 ? v : '';
    }

    getHiddenFieldCheckValue(sel, def) {
        return jq(sel).prop('checked') ? 'true' : (def || '');
    }

    populateHiddenFields() {
        // Auth
        let un = '';
        let pat = '';
        let tid = '';

        // Release Selection
        let relId = '';
        let bsi = '';
        let entPref = '';
        let relVal = jq('#releaseSelectionValue').val();
        let relSel = jq('#releaseSelection').val();

        // Entitlement Options
        let entId = '';
        let freqId = '';
        let at = '';
        let ap = '';
        let ts = '';
        let ll = '';
        let son = '';

        // ScanCentral
        let ss = '';
        let ssit = '';
        let sssb = '';
        let ssbc = '';
        let ssbf = '';
        let ssbtv = '';
        let ssve = '';
        let ssrf = '';

        // Auto Provision
        let app = '';
        let bcrit = '';
        let appT = '';
        let attr = '';
        let ismic = '';
        let mic = '';
        let rel = '';
        let sdlc = '';
        let own = '';

        if (this.overrideAuth) {
            un = jq('#usernameField').val();
            pat = jq('#patField').val();
            tid = jq('#tenantIdField').val();
        }

        if (relSel === '1') {
            bsi = relVal;
            entPref = jq('#entitlementPref').val();
        } else if (relSel === '2') {
            app = jq('#autoProvAppName').val();
            bcrit = jq('#autoProvBussCrit').val();
            appT = jq('#autoProvAppType').val();

            for (let k of Object.keys(this.appAttributes)) {
                let a = this.appAttributes[k];

                if (a.length > 0) {
                    if (attr) attr += appAttributeKeyDelimiter;
                    attr += k + appAttributeKeyValueDelimiter + a;
                }
            }
            if (jq('#autoProvAppType').val() != 2) {
                ismic = jq('#autoProvIsMicro').prop('checked');
                if (ismic) mic = jq('#autoProvMicroName').val();
            }
            rel = jq('#autoProvRelName').val();
            sdlc = jq('#autoProvSdlc').val();
            own = numberOrNull(jq('#autoProvOwner').val());
            let {entitlementId, frequencyId} = parseEntitlementDropdownValue(jq('#entitlementSelect').val());

            at = jq('#assessmentTypeSelect').val();
            entId = entitlementId;
            freqId = frequencyId;
            ap = this.getHiddenFieldSelectValue('#auditPreferenceSelect');
            ts = this.getHiddenFieldSelectValue('#technologyStackSelect');
            ll = this.getHiddenFieldSelectValue('#languageLevelSelect');
            son = this.getHiddenFieldCheckValue('#sonatypeEnabled', 'false');
        } else if (this.overrideServerSettings) {
            relId = relVal;

            let entVal = jq('#entitlementSelect').val();
            let {entitlementId, frequencyId} = parseEntitlementDropdownValue(entVal);

            entId = entitlementId;
            freqId = frequencyId;

            let atVal = jq('#assessmentTypeSelect').val();

            at = this.assessments && this.assessments[atVal] ? atVal : '';

            ap = this.getHiddenFieldSelectValue('#auditPreferenceSelect');
            ts = this.getHiddenFieldSelectValue('#technologyStackSelect');
            ll = this.getHiddenFieldSelectValue('#languageLevelSelect');
            son = this.getHiddenFieldCheckValue('#sonatypeEnabled', 'false');
        } else relId = relVal;

        ss = jq('#scanCentralBuildTypeSelect').val();

        switch (ss) {
            case 'Gradle':
            case 'Maven':
                sssb = this.getHiddenFieldCheckValue('#scanCentralSkipBuildCheck');
                ssbc = jq('#scanCentralBuildCommandInput').val();
                ssbf = jq('#scanCentralBuildFileInput').val();
                break;
            case 'MSBuild':
                ssbc = jq('#scanCentralBuildCommandInput').val();
                ssbf = jq('#scanCentralBuildFileInput').val();
                break;
            case 'Python':
                ssve = jq('#scanCentralVirtualEnvInput').val();
                ssrf = jq('#scanCentralRequirementFileInput').val();
                break;
            case 'PHP':
                break;
            case 'None':
                ss = '';
                break;
        }

        // Auth
        jq('#username').val(un);
        jq('#personalAccessToken').val(pat);
        jq('#tenantId').val(tid);

        // Release Selection
        jq('#releaseId').val(relId);
        jq('#bsiToken').val(bsi);
        jq('#entitlementPreference').val(entPref);

        // Entitlement Options
        jq('#entitlementId').val(entId);
        jq('#frequencyId').val(freqId);
        jq('#assessmentType').val(at);
        jq('#auditPreference').val(ap);
        jq('#technologyStack').val(ts);
        jq('#languageLevel').val(ll);
        jq('#sonatype').val(son);

        // ScanCentral
        jq('#scanCentral').val(ss);
        jq('#scanCentralSkipBuild').val(sssb);
        jq('#scanCentralBuildCommand').val(ssbc);
        jq('#scanCentralBuildFile').val(ssbf);
        jq('#scanCentralBuildToolVersion').val(ssbtv);
        jq('#scanCentralVirtualEnv').val(ssve);
        jq('#scanCentralRequirementFile').val(ssrf);

        // Auto Provision
        jq('#applicationName').val(app);
        jq('#businessCriticality').val(bcrit);
        jq('#applicationType').val(appT);
        jq('#attributes').val(attr);
        jq('#isMicroservice').val(ismic);
        jq('#microserviceName').val(mic);
        jq('#releaseName').val(rel);
        jq('#sdlcStatus').val(sdlc);
        jq('#owner').val(own);
    }

    preinit() {
        jq('#fodp-authUser > input').attr('id', 'usernameField');
        jq('#fodp-authPAT select.credentials-select').attr('id', 'patField');
        jq('#fodp-authTenant > input').attr('id', 'tenantIdField');

        jq('.fodp-field')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.addClass('fodp-field-row');
                let vtr = getValidationErrRow(tr);

                if (vtr) vtr.addClass('fodp-field-row-verr');
            });
        jq('.fodp-row-hidden')
            .each((i, e) => {
                let jqe = jq(e);
                let tr = closestRow(jqe);

                tr.hide();
                let vtr = getValidationErrRow(tr);

                if (vtr) vtr.hide();
            });
        jq(fodpRowSelector).hide();

        // Move css classes prefixed with fodp-row to the row element
        jq('.fodp-field')
            .each((i, e) => {
                let jqe = jq(e);
                let classes = jqe.attr('class').split(' ');

                for (let c of classes) {
                    if (c.startsWith('fodp-row-')) {
                        jqe.removeClass(c);
                        let tr = jqe.closest('.fodp-field-row');

                        tr.addClass(c)

                        // Copy Scan Central and BSI css classes to validation-error-area and help-area rows
                        if (c.startsWith('fodp-row-sc') ||
                            c.startsWith('fodp-row-autoProv') ||
                            c === 'fodp-row-nonsc' ||
                            c === 'fodp-row-bsi' ||
                            c === 'fodp-row-langLev' ||
                            c === 'fodp-row-all' ||
                            c === 'fodp-row-relid') {
                            let vtr = getValidationErrRow(tr);
                            let htr = getHelpRow(tr);

                            if (vtr) vtr.addClass(c);
                            if (htr) htr.addClass(c);
                        }
                    }
                }
            });
        jq('.fodp-row-all').show();

        //
        // Hacks to fix alignment issues
        //

        // In the previous jenkins versions that use table layout,
        // this sets the field label min-width so selecting
        // Override Release Settings doesn't look janky because of Sonatype
        let jqe = jq('#scanCentralBuildTypeForm').parent();

        if (jqe.prop('tagName') === 'TD') jqe.prev('td').css('min-width', '300px');
        // &nbsp; breaks Jelly compilation
        jq('#releaseLookup').parent().append('&nbsp;');

        jq('[name="overrideGlobalConfig"]')
            .change(_ => {
                this.overrideAuth = jq('[name="overrideGlobalConfig"]').prop('checked');
                this.onAuthChanged();
            });

        jq('#usernameField')
            .change(_ => this.onAuthChanged());

        jq('#patField')
            .change(_ => this.onAuthChanged());

        jq('#tenantIdField')
            .change(_ => this.onAuthChanged());

        this.init();
    }

    onAddAppAttribute() {
        let key = jq('#autoProvAttrKey').val().trim();
        let val = jq('#autoProvAttrValue').val().trim();

        if (key !== '' && val != '') {
            jq('#autoProvAttrKey').removeAttr('style');
            let attr = this.appAttributes[key];

            if (attr) {
                jq('#autoProvAttrKey').css('border-color', 'red');
                return;
            }

            this.appAttributes[key] = val;

            let ul = jq('#autoProvAttr');

            ul.find('li').remove();

            for (let k of Object.keys(this.appAttributes).sort()) {
                let val = this.appAttributes[k];

                let li = ul.append(`<li value="${k}"><span>X</span>${k}:${val}</li>`);

                li.find('span').click(e => this.onRemoveAppAttribute(e));
            }

            jq('#autoProvAttrKey').val('');
            jq('#autoProvAttrValue').val('');
            this.populateHiddenFields();
        }
    }

    onRemoveAppAttribute(e) {
        let li = jq(e.target).parent();

        delete this.appAttributes[li.attr('value')];
        li.remove();
        this.populateHiddenFields();
    }

    async init() {
        try {
            this.techStacks = await this.api.getTechStacks(getAuthInfo());
            for (let k of Object.keys(this.techStacks)) {
                let ts = this.techStacks[k];

                // noinspection EqualityComparisonWithCoercionJS
                if (ts.value == techStackConsts.dotNetCore) {
                    for (let ll of ts.levels) {
                        ll.text = ll.text.replace(' (.NET Core)', '');
                    }
                }

                this.techStacksSorted.push(ts);
            }

            this.techStacksSorted = this.techStacksSorted.sort((a, b) => a.text.toLowerCase() < b.text.toLowerCase() ? -1 : 1);
        } catch (err) {
            this.techStacks = null;
            this.showMessage('Failed to retrieve api data', true);
            return;
        }

        this.hideMessages();
        this.showMessage('Set a release id');

        jq('#autoProvAppName')
            .change(_ => this.loadEntitlementOptions());
        jq('#autoProvBussCrit')
            .change(_ => this.populateHiddenFields());
        jq('#autoProvAppType')
            .change(_ => this.populateHiddenFields());
        jq('#autoProvIsMicro')
            .change(_ => this.loadEntitlementOptions());
        jq('#autoProvMicroName')
            .change(_ => this.loadEntitlementOptions());
        jq('#autoProvRelName')
            .change(_ => this.loadEntitlementOptions());
        jq('#autoProvSdlc')
            .change(_ => this.populateHiddenFields());
        jq('#autoProvOwner')
            .change(_ => this.populateHiddenFields());

        jq('#auditPreferenceSelect')
            .change(_ => this.populateHiddenFields());

        jq('#sonatypeEnabled')
            .change(_ => this.populateHiddenFields());

        jq('#scanCentralSkipBuildCheck')
            .change(_ => this.populateHiddenFields());

        jq('#scanCentralBuildCommandInput')
            .change(_ => this.populateHiddenFields());

        jq('#scanCentralBuildFileInput')
            .change(_ => this.populateHiddenFields());

        jq('#scanCentralBuildToolVersionInput')
            .change(_ => this.populateHiddenFields());

        jq('#scanCentralVirtualEnvInput')
            .change(_ => this.populateHiddenFields());

        jq('#scanCentralRequirementFileInput')
            .change(_ => this.populateHiddenFields());

        jq('#entitlementPref')
            .change(_ => this.populateHiddenFields());

        jq('#releaseSelection')
            .change(_ => this.onReleaseSelectionChanged());

        jq('#releaseSelectionValue')
            .change(_ => this.onReleaseIdChanged());

        jq('#scanCentralBuildTypeSelect')
            .change(_ => this.onScanCentralChanged());

        jq('#assessmentTypeSelect')
            .change(_ => this.onAssessmentChanged());

        jq('#entitlementSelect')
            .change(_ => this.onEntitlementChanged());

        jq('#technologyStackSelect')
            .change(_ => this.onTechStackChanged());

        jq('#languageLevelSelect')
            .change(_ => this.onLangLevelChanged());

        jq('#autoProvAttrAdd')
            .click(_ => this.onAddAppAttribute());

        jq('#autoProvAttrKey')
            .keypress(e => {
                if (this.isEnterPressed(e)) this.onAddAppAttribute();
            });

        jq('#autoProvAttrValue')
            .keypress(e => {
                if (this.isEnterPressed(e)) this.onAddAppAttribute();
            });

        jq('#autoProvIsMicro')
            .change(_ => this.onIsMicroserviceChanged());
        jq('#autoProvAppType')
            .change(_ => this.onIsApplicationTypeChanged());
        jq('#autoProvOwnerAssignMe')
            .click(e => {
                e.preventDefault();
                this.onAssignMeClick();
            });

        jq('#overrideReleaseSettings')
            .change(_ => this.loadEntitlementOptions());

        // jq('.yui-button.yui-push-button.submit-button.primary button')
        //     .click(_ => this.onGenScriptClick());
        //
        // jq('.yui-button.yui-push-button.submit-button.primary').parent().find('textarea')
        //     .change(_ => this.onGenScriptClick());

        this.populateTechStackDropdown();
        this.onReleaseSelectionChanged();
        this.onScanCentralChanged();

        this.uiLoaded = true;
    }

    // onGenScriptClick() {
    //     let v = jq('#prototypeText_id9').val();
    // }

    isEnterPressed(e) {
        return e.which === 13;
    }
}


const pipelineGenerator = new PipelineGenerator();

spinAndWait(() => jq('#sonatypeEnabledForm').val() !== undefined)
    .then(pipelineGenerator.preinit.bind(pipelineGenerator));
