    var result;
    $(document).ready(function() {
        $("#result").hide();
        $("#moreInfo").hide();
        $("#showResult").click(function() {
            $("#wait").css("display", "block");
            doQuery();
        });
    });

    function doQuery() {
        $("#result").empty();
        var payload = { "queryRequestString" : $('#queryInput').val()};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:8090/query',
            dataType: 'xml',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
           },
            error: function(error) {
            },
            complete: function(xhr, status) {
                $("#wait").css("display", "none");
                $("#result").show();
                $("#moreInfo").show();
                $("#result").load("query/result");
            }
        });
        return;
    }