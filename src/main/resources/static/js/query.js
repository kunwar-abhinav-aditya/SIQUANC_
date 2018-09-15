    var selectedTasks = [];
    var static = false;
    var components = new Array();
    $("#thanks").hide();
    $("#rating").hide();
    $("#result").hide();
    $("#moreInfo").hide();
    $('#timer').hide();
    $('#timer').runner();
    window.sr = ScrollReveal();
    sr.reveal('.logo-asknow', {
        duration: 2000,
        origin:'left',
        distance:'200px',
        viewFactor: 0.8
    });
    sr.reveal('.toReveal', {
        duration: 500,
        origin:'bottom',
        distance:'50px',
        viewFactor: 0.2
    });
    $(document).ready(function() {
            var colorOrig=$(".fa-github").css('color');
            $(".fa-github").hover(
            function() {
                //mouse over
                $(this).css('color', '#FFD700')
            }, function() {
                //mouse out
                $(this).css('color', colorOrig)
            });
        $("#showResult").click(function() {
            if ($("#queryInput").val() == "") {
                alert("Either fill a question in the text field or select one from the footer");
            }
            else {
                doQuery();
            }
        });
        $(".link").click(function() {
            doQuery();
        });
        if (localStorage.getItem("pipeline") != null) {
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
        }
        else {
            $("#pipelineType").append("Querying using a fixed pipeline. To run the query using your own custom pipeline, go to the <a href=\"/build\">build</a> module!");
            localStorage.removeItem("context");
            localStorage.setItem("context","simple");
            document.getElementById("pipelineType").style.color = "#354B82";
        }

        $(".btnrating").on('click',(function(e) {
        	var previous_value = $("#selected_rating").val();
        	var selected_value = $(this).attr("data-attr");
        	rating = selected_value;
        	$("#selected_rating").val(selected_value);

        	$(".selected-rating").empty();
        	$(".selected-rating").html(selected_value);

        	for (i = 1; i <= selected_value; ++i) {
        	    $("#rating-star-"+i).toggleClass('btn-warning');
        	    $("#rating-star-"+i).toggleClass('btn-default');
        	}

        	for (ix = 1; ix <= previous_value; ++ix) {
        	    $("#rating-star-"+ix).toggleClass('btn-warning');
        	    $("#rating-star-"+ix).toggleClass('btn-default');
        	}
        }));
        $("#submitRating").on('click',(function(e) {
            submitRating();
        }));
    });

    function doQuery() {
        var resourceURLs = [];
        disable();
        $(".jumbotron").addClass('blurdiv');
        $("#wait").css("display", "block");
        $("#timer").css("display", "block");
        $("#timer").fadeIn();
        $('#timer').runner('start');
        $(".selected-rating").empty();
        $(".selected-rating").html(0);
        $("#selected_rating").val(0);
        for (i = 1; i <= rating; ++i) {
            $("#rating-star-"+i).removeClass('btn-warning');
            $("#rating-star-"+i).addClass('btn-default');
        }
        $("#result").empty();
        $("#thanks").hide();
        $("#rating").hide();
        var requiresQueryBuilding = false;
        if (localStorage.getItem("selectedTasks") != null) {
            selectedTasks = localStorage.getItem("selectedTasks").split(",");
        }
        if (selectedTasks[selectedTasks.length-1] == "Query Builder" || selectedTasks.length == 0) {
            requiresQueryBuilding = true;
        }
        if (localStorage.getItem("defaultQuestion") != null) {
            question = localStorage.getItem("defaultQuestion");
        }
        else {
            question = $('#queryInput').val();
        }
        var payload = { "queryRequestString" : question, "components" : components, "requiresQueryBuilding" : requiresQueryBuilding, "tasks" : selectedTasks};
        $.ajax({
            type: 'POST',
            url: 'http://frankenstein.sda.tech/query',
            //url: 'http://localhost:10000/query',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                enable();
                $("#result").append("<br>");
                var ques = document.createElement('div');
                ques.id = "ques";
                document.getElementById('result').appendChild(ques);
                $("#ques").addClass("heads");
                $("#ques").append("Question");
                $("#result").append(question);
                $("#result").append("<br><br>");
                var outputs = document.createElement('div');
                outputs.id = "outputs";
                document.getElementById('result').appendChild(outputs);
                $("#outputs").addClass("heads");
                $("#outputs").append("Outputs");
                if (components.length == 0) {
                    static = true;
                    components.push("AmbiverseNed");
                    components.push("EarlRelationLinking");
                    components.push("QueryBuilder");
                }
                for(var i=0;i < queryResponse['queryResponseStrings'].length; i++) {
                    if (i < components.length) {
                        $("#result").append("<font color=\"#354B82\">Output of</font> ");
                        $("#result").append("<b>"+components[i]+"</b>");
                        $("#result").append("<br>");
                    }
                    var propRes = document.createElement('a');
                    propRes.id = "propRes"+i;
                    document.getElementById('result').appendChild(propRes);
                    $("#propRes"+i).addClass("abig");
                    if (queryResponse['queryResponseStrings'][i].includes("localhost") || queryResponse['queryResponseStrings'][i].includes("file")) {
                        $("#propRes"+i).append("");
                    } else {
                        $("#propRes"+i).append("<code>"+queryResponse['queryResponseStrings'][i]+"</code>");
                    }
                    if (queryResponse['queryResponseStrings'][i].includes("http://dbpedia.org")) {
                        $("#propRes"+i).attr("href", queryResponse['queryResponseStrings'][i]);
                    }
                    if (i >= (components.length-1)){
                        resourceURLs.push(queryResponse['queryResponseStrings'][i]);
                    }
                    $("#result").append("<br><br>");
                }
                if (static == true) {
                    components = []
                }
                $("#result").append("<br>");

            },
            error: function(error) {
            },
            complete: function(xhr, status) {
                localStorage.removeItem("defaultQuestion");
                $(".jumbotron").removeClass('blurdiv');
                $("#wait").css("display", "none");
                $('#timer').runner('stop');
                $("#timer").fadeOut();
                var timerhead = document.createElement('div');
                timerhead.id = "timerhead";
                document.getElementById('result').appendChild(timerhead);
                $("#timerhead").addClass("heads");
                $("#timerhead").append("Time taken by pipeline");
                var timeTaken = $("#timer").text();
                $('#timer').runner('reset', true);
                $("#result").append(timeTaken+ " seconds.");
                $("#result").append("<br><br>");
                var fullresponse = document.createElement('div');
                fullresponse.id = "fullresponse";
                document.getElementById('result').appendChild(fullresponse);
                $("#fullresponse").addClass("heads");
                $("#fullresponse").append("Full response");
                $("#result").append("<a href=\"/query/result\">here</a>");
                $("#result").append("<br><br>Go back to querying with the <button class=\"link\" id=\"gotofixed\" onclick=\"backToBasics()\">fixed pipeline</button> or build a new <button class=\"link\" id=\"gotodynamic\" onclick=\"buildNewdynamic()\">dynamic pipeline</button>");
                $("#result").fadeIn();
                $("#moreInfo").fadeIn();
                $("#rating").fadeIn();
                if (selectedTasks[selectedTasks.length-1] == "Query Builder" || selectedTasks[selectedTasks.length-1] == "NED" || selectedTasks.length == 0) {
                    if (resourceURLs[resourceURLs.length-1].includes("http://dbpedia.org")) {
                        var win = window.open('/resource', '_blank');
                        if (win) {
                            localStorage.removeItem("resourceURLs");
                            localStorage.setItem("resourceURLs",resourceURLs);
                            resourceURLs=[];
                            win.focus();
                        } else {
                            //Browser has blocked it
                            alert('Please enable popups');
                        }
                    }
                }
            }
        });
        return;
    }

    function submitRating() {
        var payload = { "rating" : rating, "components" : components, "question" : question};
        $.ajax({
            type: 'POST',
            url: 'http://frankenstein.sda.tech/query/feedback',
            //url: 'http://localhost:10000/query/feedback',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                $("#rating").hide();
                $("#thanks").fadeIn();
            },
            error: function(error) {
            }
        });
        return;
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

    function setdefaultquestion(value) {
        localStorage.removeItem("defaultQuestion");
        localStorage.setItem("defaultQuestion", value);
    }

    function disable(){
        $("#showResult").prop("disabled",true);
        $("#queryInput").prop("disabled",true);
    }

    function enable(){
        $("#showResult").prop("disabled",false);
        $("#queryInput").prop("disabled",false);
    }