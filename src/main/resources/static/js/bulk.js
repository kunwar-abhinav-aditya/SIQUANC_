    var selectedTasks = [];
    var dynamicpipeline = false;
    $('#timer').hide();
    $('#timer').runner();
    var components = new Array();
    var modal = document.getElementById('sampleModal');
    $(document).ready(function() {
            $("#openmodal").click(function() {
                modal.style.display = "block";
            });
            $(".close").click(function() {
                modal.style.display = "none";
            });
            var colorOrig=$(".fa-github").css('color');
            $(".fa-github").hover(
            function() {
                //mouse over
                $(this).css('color', '#FFD700')
            }, function() {
                //mouse out
                $(this).css('color', colorOrig)
            });
        if (localStorage.getItem("pipeline") != null) {
            dynamicpipeline = true;
            adddynamicinfo();
        }
        else {
            $("#pipelineType").append("Querying using a fixed pipeline. To run the query using your own custom pipeline, go to the <a href=\"/build\">build</a> module!");
            localStorage.removeItem("context");
            localStorage.setItem("context","bulk");
            document.getElementById("pipelineType").style.color = "#354B82";
        }
        $("#inputGroupFile01").change(function() {
            resetinfo();
            showselectedCsv();
        });
    });

    function queryBulk() {
        disable();
        $(".jumbotron").addClass('blurdiv');
        $("#wait").css("display", "block");
        $("#timer").css("display", "block");
        $("#timer").fadeIn();
        $('#timer').runner('start');
        var documentData = new FormData();
        var requiresQueryBuilding = false;
        if (localStorage.getItem("selectedTasks") != null) {
            selectedTasks = localStorage.getItem("selectedTasks").split(",");
        }
        if (selectedTasks[selectedTasks.length-1] == "Query Builder") {
            requiresQueryBuilding = true;
        }
        documentData.append('file', $("#inputGroupFile01")[0].files[0]);
        documentData.append('components', components);
        documentData.append('requiresQueryBuilding', requiresQueryBuilding);
        deletePreviousOnes();
        $.ajax({
            url: 'http://frankenstein.qanary-qa.com/query/bulk',
            //url: 'http://localhost:10000/query/bulk',
            type: 'POST',
            data: documentData,
            contentType: false,
            processData: false,
            success: function(response) {
                $('#fileandbutton').empty();
                $("#wait").css("display", "none");
                $('#timer').runner('stop');
                $("#timer").fadeOut();
                enable();
                $(".jumbotron").removeClass('blurdiv');
                var timerhead = document.createElement('div');
                timerhead.id = "timerhead";
                document.getElementById('pipelineType').appendChild(timerhead);
                $("#timerhead").addClass("heads");
                $("#timerhead").append("Time taken by pipeline");
                var timeTaken = $("#timer").text();
                $('#timer').runner('reset', true);
                $("#pipelineType").append(timeTaken+ " seconds.");
                $('#pipelineType').append("<br><br>Questions have been uploaded and <code>.ttl(s)</code> generated. Download the results now!");
                $('#pipelineType').append("<br><a class=\"btn btn-info btn-sm\" href=\"http://frankenstein.qanary-qa.com/query/bulk/"+response+"\" role=\"button\" id=\"downloadBulkResults\">Download Results!</button>");
                $('#pipelineType').append("<br><a href=\"#\" onclick=\"resetinfo()\">Search Again</a>");
                $("#fileform")[0].reset();
            },
            error: function(error) {
                alert("Server Error!");
                $('#fileandbutton').empty();
                $("#wait").css("display", "none");
                $('#timer').runner('stop');
                $('#timer').runner('reset', true);
            }
        });
        return;
    }

    function resetinfo() {
        if (dynamicpipeline == true) {
            adddynamicinfo();
        }
        else {
            searchagain();
        }
    }

    function searchagain() {
        $('#pipelineType').empty();
        $("#pipelineType").append("Querying using a fixed pipeline. To run the query using your own custom pipeline, go to the <a href=\"/build\">build</a> module!");
    }
    function deletePreviousOnes() {
        $.ajax({
            url: 'http://frankenstein.qanary-qa.com/query/bulk',
            //url: 'http://localhost:10000/query/bulk',
            type: 'DELETE',
            success: function(response) {
                console.log(response);
            },
            error: function(error) {
                console.log(error);
            }
        });
        return;
    }

    function str2bytes (str) {
        var bytes = new Uint8Array(str.length);
        for (var i=0; i<str.length; i++) {
            bytes[i] = str.charCodeAt(i);
        }
        return bytes;
    }

    function backToBasics() {
        localStorage.removeItem("pipeline");
        localStorage.removeItem("selectedTasks");
        window.location.reload();
    }

    function buildNewdynamic() {
        localStorage.removeItem("pipeline");
        localStorage.removeItem("selectedTasks");
        window.location.replace("/build");
    }

    function disable(){
        $("#queryBulk").prop("disabled",true);
        $("#inputGroupFile01").prop("disabled",true);
    }

    function enable(){
        $("#queryBulk").prop("disabled",false);
        $("#inputGroupFile01").prop("disabled",false);
    }

    function showselectedCsv(){
        var fileandbutton = document.createElement('fileandbutton');
        fileandbutton.id = "fileandbutton";
        document.getElementById('pipelineType').appendChild(fileandbutton);
        $('#fileandbutton').append("<hr>");
        $('#fileandbutton').append("<i class='fa fa-file-excel-o'></i>&nbsp;");
        $('#fileandbutton').append($("#inputGroupFile01")[0].files[0].name);
        $('#fileandbutton').append("<br><button class=\"btn btn-primary btn-sm\" type=\"button\" id=\"queryBulk\" onclick=\"queryBulk()\">Upload my questions</button>");
    }

    function adddynamicinfo(){
            $('#pipelineType').empty();
            $("#pipelineType").append("Querying using the below pipeline");
            document.getElementById("pipelineType").style.color = "#354B82";
            $("#pipelineType").append("<br>");
            components = localStorage.getItem("pipeline").split(",");
            for(var i =0;i < components.length; i++)
            {
                var opt = components[i];
                $('#pipelineType').append("<button type=\"button\" class=\"btn btn-outline-dark btn-sm box\" disabled>"+opt+"</button>");
                if (i < components.length - 1) {
                    $('#pipelineType').append("<i class=\"fa fa-arrow-right box\"></i>");
                }
            }
            $("#pipelineType").append("<br>Go back to querying with the <button class=\"link\" id=\"gotofixed\" onclick=\"backToBasics()\">fixed pipeline</button> or build a new <button class=\"link\" id=\"gotodynamic\" onclick=\"buildNewdynamic()\">dynamic pipeline</button>");
    }