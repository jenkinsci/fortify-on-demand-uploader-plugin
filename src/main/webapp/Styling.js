function showWithSpinner(id) {
    jq(id).show();
    jq(id + ' .spinner-container').addClass('spinner');
}

function hideSpinner(id) {
    jq(id + ' .spinner-container').removeClass('spinner');
}

let __spinnerSupported = false;

for (let ss of document.styleSheets) {
    for (let r of ss.cssRules) {
        if (r.selectorText) {

            for (let c of r.selectorText.matchAll(/.[^.:, ]+/gm)) {
                for (let itr of c) {
                    if (itr === '.spinner-container' || itr === '.spinner') {
                        __spinnerSupported = true;
                        break;
                    }
                }

            }
        }
    }
}

if (!__spinnerSupported) {
    jq('head').append(jq(`
<style>
    /*.spinner-container.spinner::before {*/
    /*    content: 'Loading...'*/
    /*}*/
    
    /* HTML: <div class="loader"></div> */
    .spinner-container.spinner {
      width: 25px;
      aspect-ratio: 1;
      display: grid;
      border-radius: 50%;
      background:
        linear-gradient(0deg ,rgb(0 0 0/50%) 30%,#0000 0 70%,rgb(0 0 0/100%) 0) 50%/8% 100%,
        linear-gradient(90deg,rgb(0 0 0/25%) 30%,#0000 0 70%,rgb(0 0 0/75% ) 0) 50%/100% 8%;
      background-repeat: no-repeat;
      animation: l23 1s infinite steps(12);
    }
    .spinner-container.spinner::before,
    .spinner-container.spinner::after {
       content: "";
       grid-area: 1/1;
       border-radius: 50%;
       background: inherit;
       opacity: 0.915;
       transform: rotate(30deg);
    }
    .spinner-container.spinner::after {
       opacity: 0.83;
       transform: rotate(60deg);
    }
    @keyframes l23 {
      100% {transform: rotate(1turn)}
    }
</style>`));
}