
(function ($) {
    "use strict";
    var mainAppNew = {

        main_fun: function () {

            /*=====================================
             THEME SWITCHER SCRIPTS
            ===================================*/
            jQuery('#switch-panel').click(function () {
                if (jQuery(this).hasClass('show-panel')) {
                    jQuery('.switcher').css({ 'left': '-50px' });
                    jQuery('#switch-panel').removeClass('show-panel');
                    jQuery('#switch-panel').addClass('hide-panel');
                } else if (jQuery(this).hasClass('hide-panel')) {
                    jQuery('.switcher').css({ 'left': 0 });
                    jQuery('#switch-panel').removeClass('hide-panel');
                    jQuery('#switch-panel').addClass('show-panel');
                }
            });


            $('#green').click(function () {
                $('#mainCSS').attr('href', '/css/themes/green.css');
                localStorage.removeItem('mainCSS');
                localStorage.setItem('mainCSS', '/css/themes/green.css');
            });
            $('#aqua').click(function () {
                $('#mainCSS').attr('href', '/css/themes/aqua.css');
                localStorage.removeItem('mainCSS');
                localStorage.setItem('mainCSS', '/css/themes/aqua.css');
            });
            $('#grey').click(function () {
                $('#mainCSS').attr('href', '/css/themes/grey.css');
                localStorage.removeItem('mainCSS');
                localStorage.setItem('mainCSS', '/css/themes/grey.css');
            });
            $('#blue').click(function () {
                $('#mainCSS').attr('href', '/css/themes/blue.css');
                localStorage.removeItem('mainCSS');
                localStorage.setItem('mainCSS', '/css/themes/blue.css');
            });
            $('#lightgrey').click(function () {
                $('#mainCSS').attr('href', '/css/themes/lightgrey.css');
                localStorage.removeItem('mainCSS');
                localStorage.setItem('mainCSS', '/css/themes/lightgrey.css');
            });
        },

        initialization: function () {
            mainAppNew.main_fun();

        }

    }
    // Initializing ///

    $(document).ready(function () {
        mainAppNew.main_fun();
    });

}(jQuery));