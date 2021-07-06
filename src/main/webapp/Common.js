function debounce(func, wait, immediate) {
    var timeout;
    return function() {
        var context = this, args = arguments;
        var later = function() {
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
            if(fn()) res();
            else setTimeout(elementsLoaded, 50);
        };

        elementsLoaded();
    });
}