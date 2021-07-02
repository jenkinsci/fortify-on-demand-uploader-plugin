function showWithSpinner(id) {
    jq(id).show();
    jq(id + ' .spinner-container').addClass('spinner');
}

function hideSpinner(id) {
    jq(id + ' .spinner-container').removeClass('spinner');
}