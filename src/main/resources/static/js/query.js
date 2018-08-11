    var result;
    $(document).ready(function() {
        $("#result").hide();
        $("#moreInfo").hide();
        $("#showResult").click(function() {
            $("#wait").css("display", "block");
            doQuery();
        });
        if (localStorage.getItem("pipeline") != null) {
            $("#pipelineType").append("Querying using the below pipeline");
            $("#pipelineType").append("<br>");
            var components = localStorage.getItem("pipeline").split(",");
            for(var i =0;i < components.length; i++)
            {
                var opt = components[i];
                $('#pipelineType').append("<button type=\"button\" class=\"btn btn-secondary btn-sm box\" disabled>"+opt+"</button>");
                if (i < components.length - 1) {
                    $('#pipelineType').append("<i class=\"fa fa-chevron-circle-right box\"></i>");
                }
            }
        }
        else {
            $("#pipelineType").append("Querying using a fixed pipeline. To run it against your own custom pipeline, go to the <a href=\"/build\">build</a> module!");
        }
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
                $("#result").fadeIn();
                $("#moreInfo").fadeIn();
                $("#result").load("query/result");
            }
        });
        return;
    }