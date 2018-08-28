
    var result;
    var components = new Array();
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
            components = localStorage.getItem("pipeline").split(",");
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
        var payload = { "queryRequestString" : $('#queryInput').val(), "components" : components};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:10000/query',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                $("#result").append(queryResponse['queryResponseString']);
                $("#result").attr("href", queryResponse['queryResponseString']);
            },
            error: function(error) {
            },
            complete: function(xhr, status) {
                $("#wait").css("display", "none");
                $("#result").fadeIn();
                $("#moreInfo").fadeIn();
            }
        });
        return;
    }