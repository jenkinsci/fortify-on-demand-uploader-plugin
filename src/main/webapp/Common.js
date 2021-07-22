function dispatchEvent(type, payload) {
    document.dispatchEvent(new CustomEvent(type, {detail: payload}));
}

function subscribeToEvent(type, cb) {
    document.removeEventListener(type, cb);
    document.addEventListener(type, cb);
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