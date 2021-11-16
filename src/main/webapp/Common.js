/**
 * @typedef {jQuery} jQueryExtended
 * @property {function([value:string]): string} val
 */
/**
 * @callback jQueryFunc
 * @param {string|Object} selector
 * @return {jQueryExtended}
 * */
/** @type {jQueryFunc} */
const jq = jQuery;

const techStackConsts = {
    none: -1,
    dotNet: 1,
    dotNetCore: 23,
    java: 7,
    php: 9,
    python: 10
};

const _scanCentralBuildTypes = {
    "None": "None",
    "Gradle": "Gradle",
    "Maven": "Maven",
    "MSBuild": "MSBuild",
    "PHP": "PHP",
    "Python": "Python"
};

function dispatchEvent(type, payload) {
    document.dispatchEvent(new CustomEvent(type, {detail: payload}));
}

function subscribeToEvent(type, cb) {
    document.removeEventListener(type, cb);
    document.addEventListener(type, cb);
}

function unsubscribeEvent(type, cb) {
    document.removeEventListener(type, cb);
}

function debounce(func, wait, immediate) {
    var timeout;
    return function () {
        var context = this, args = arguments;
        var later = function () {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        var callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(context, args);
    };
};

function getEntitlementDropdownValue(id, freq) {
    return `${id}-${freq}`;
}

function parseEntitlementDropdownValue(val) {
    let entitlementId = '';
    let frequencyId = '';

    if (val) {
        let spl = val.split('-');

        if (spl.length === 2) {
            entitlementId = numberOrNull(spl[0]);
            frequencyId = numberOrNull(spl[1]);
        }
    }

    return { entitlementId, frequencyId };
}

function spinAndWait(fn) {
    return new Promise((res, rej) => {
        let elementsLoaded;

        elementsLoaded = () => {
            if (fn()) res();
            else setTimeout(elementsLoaded, 50);
        };

        elementsLoaded();
    });
}

function closestRow(selector) {
    let jqe = selector instanceof jQuery ? selector : jq(selector);
    let tr =  jqe.closest('tr');

    if (tr.length == 0) {
        tr = jqe.closest('.tr');
    }

    return tr;
}

function nextRow(elem) {
    return elem.is('tr') ? elem.next('tr') : elem.next('.tr') ;
}

function getValidationErrRow(row) {
    let vtr = nextRow(row);

    if (vtr.length > 0 && vtr.hasClass('validation-error-area')) return vtr;

    return null;
}

function getHelpRow(row) {
    let vtr = nextRow(row);

    if (vtr.length > 0 && vtr.hasClass('validation-error-area')) {
        let htr = nextRow(vtr);

        if (htr.length > 0 && htr.hasClass('help-area')) return htr;
    } else if (vtr.length > 0 && vtr.hasClass('help-area')) return vtr;

    return null;
}

function createDialog(dialog) {
    spinAndWait(() => jq('#' + dialog._formId).html())
        .then(() => dialog.init());
}

function isNullOrEmpty(str) {
    if (typeof str === 'string' && str !== ''&& str !== ' ') return str.trim() ? false : true;

    return !str;
}

class Dialog {

    constructor(dialogId, formId) {
        this._dialogId = dialogId;
        this._formId = formId;
        this._bindToWindow();
    }

    _bindToWindow() {
        window[this._dialogId] = window[this._dialogId] || {'dialog': null, 'body': null};
        window[this._dialogId].init = function () {
            if (!(window[this._dialogId].dialog)) {
                var div = document.createElement("DIV");
                document.body.appendChild(div);
                div.innerHTML = "<div id='" + this._dialogId + "'><div id='abc' class='bd'></div></div>";
                window[this._dialogId].body = $(this._dialogId);
                window[this._dialogId].body.innerHTML = $(this._formId).innerHTML;
                jq('#' + this._dialogId).prepend('<div class="hd"><span id="dialogTitle"></span> <span class="spinner" id="dialogSpinner" style="display: none;"></span></div>');
                jq('#' + this._dialogId).prepend('<div class="mask" id="modal_mask" style="z-index: 1000; height: 100%; width: 100%; display: none;"> </div>');
                window[this._dialogId].dialog = new YAHOO.widget.Panel(window[this._dialogId].body, {
                    fixedcenter: true,
                    close: true,
                    draggable: true,
                    zindex: 1000,
                    modal: true,
                    visible: false,
                    keylisteners: [
                        new YAHOO.util.KeyListener(document, {keys:27}, {
                            fn:(function() {window[this._dialogId].dialog.hide();}),
                            scope:document,
                            correctScope:false
                        })
                    ]
                });
                window[this._dialogId].dialog.render();
            }
        };
    }

    jqDialog(selector) {
        return jq('#' + this._dialogId + ' ' + selector);
    }

    init() {
        jq('#' + this._formId).hide();
        if (this.onInit) {
            this.onInit();
        }
    }

    spawnDialog(title, data) {
        window[this._dialogId].init.bind(this)();
        window[this._dialogId].dialog.show();

        this.jqDialog('#dialogTitle')[0].innerHTML = title;
        this.stopSpinning();

        if (this.onDialogSpawn) {
            this.onDialogSpawn(data);
        }
    }

    closeDialog() {
        window[this._dialogId].dialog.close.click();

        if (this.onDialogClose) {
            this.onDialogClose();
        }
    }

    startSpinning() {
        this.jqDialog('#modal_mask').show();
        this.jqDialog('#dialogSpinner').show();
    }

    stopSpinning() {
        this.jqDialog('#dialogSpinner').hide();
        this.jqDialog('#modal_mask').hide();
    }
}

function _guidTsFnFallback() {
    return new Date().getTime() + (Math.random() * 10000); // compensates for the lack of ms
}

function _guidTsFnPreferred() {
    return Math.floor(performance.now() * 1000);
}

const _guidTsFn = (performance && performance.now) ? _guidTsFnPreferred : _guidTsFnFallback;

function newGuid() {
    let ts = _guidTsFn();

    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        let r = Math.random() * 16;//random number between 0 and 16

        r = (ts + r)%16 | 0;
        ts = Math.floor(ts/16);

        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

function partitionArray(arr, elementsPerPartition) {
    const partitions = [];
    for (let i = 0; i * elementsPerPartition < arr.length; i++) {
        partitions.push(arr.slice(i * elementsPerPartition, i * elementsPerPartition + elementsPerPartition));
    }

    return partitions;
}

function numberOrNull(str) {
    let res = Number(str);

    return Number.isInteger(res) ? res : null;
}
