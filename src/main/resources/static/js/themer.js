var mainCSS = localStorage.getItem('mainCSS');
if (mainCSS == null) {
    $('#mainCSS').attr('href', '/css/themes/grey.css');
} else {
    $('#mainCSS').attr('href', mainCSS);
}