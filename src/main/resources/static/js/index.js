
    $(document).ready(function() {
        $('[data-toggle="tooltip"]').tooltip();
        var colorOrig=$(".fa-github").css('color');
        $(".fa-github").hover(
        function() {
            //mouse over
            $(this).css('color', '#FFD700')
        }, function() {
            //mouse out
            $(this).css('color', colorOrig)
        });
        $(".star").hover(
        function() {
            //mouse over
            $(this).css('color', '#FFD700')
        }, function() {
            //mouse out
            $(this).css('color', colorOrig)
        });
    });