
    var static = false;
    var components = [];
    var selectedTasks = [];
    var resourceURLs = [];
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
        $("#defaultQuestion1").click(function() {
            doQuery();
        });
        $("#defaultQuestion2").click(function() {
            doQuery();
        });

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
        disable();
        $("#result").empty();
        var parent = document.getElementById('resources');
        while(parent.firstChild){
            parent.removeChild(parent.firstChild);
        }
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
        $("#thanks").hide();
        $("#rating").hide();
        var requiresQueryBuilding = true;
        if (localStorage.getItem("defaultQuestion") != null) {
            question = localStorage.getItem("defaultQuestion");
            localStorage.removeItem("defaultQuestion");
        }
        else {
            question = $('#queryInput').val();
        }
        var payload = { "queryRequestString" : question, "components" : components, "requiresQueryBuilding" : requiresQueryBuilding, "tasks" : selectedTasks};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:10000/query',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                for(var i=0;i < queryResponse['queryResponseStrings'].length; i++) {
                    if (i >= 2){
                        resourceURLs.push(queryResponse['queryResponseStrings'][i]);
                    }
                }
                enable();


            },
            error: function(error) {
            },
            complete: function(xhr, status) {
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
                fetchDetails();
                $("#result").fadeIn();
                $("#moreInfo").fadeIn();
                $("#rating").fadeIn();
            }
        });
        return;
    }

    function submitRating() {
        var payload = { "rating" : rating, "components" : components, "question" : question};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:10000/query/feedback',
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

    function setdefaultquestion1() {
        localStorage.removeItem("defaultQuestion");
        localStorage.setItem("defaultQuestion", $("#defaultQuestion1").text());
    }

    function setdefaultquestion2() {
        localStorage.removeItem("defaultQuestion");
        localStorage.setItem("defaultQuestion", $("#defaultQuestion2").text());
    }

    function disable(){
        $("#showResult").prop("disabled",true);
        $("#queryInput").prop("disabled",true);
    }

    function enable(){
        $("#showResult").prop("disabled",false);
        $("#queryInput").prop("disabled",false);
    }

    function fetchDetails() {
        getLeadAndAbstract();
    }

    function getLeadAndAbstract(){
        var payload = resourceURLs;
        $.ajax({
            type: 'POST',
            url: 'http://localhost:10000/query/resource',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                resourceURLs = [];
                for(var i=0;i < queryResponse.length; i++)
                {
                    if (queryResponse[i]['name'].includes("http://dbpedia.org/")) {
                        var adiv = document.createElement('div');
                        adiv.id = "adiv"+i;
                        document.getElementById('resources').appendChild(adiv);
                        $("#adiv"+i).addClass("namenotexists");

                        var name = document.createElement('a');
                        name.id = "name"+i;
                        document.getElementById('adiv'+i).appendChild(name);

                        $("#name"+i).addClass("namenotexists");
                        $("#name"+i).append(queryResponse[i]['name']);
                        $("#name"+i).attr("href", queryResponse[i]['name']);

                        var noinfo = document.createElement('div');
                        noinfo.id = "noinfo"+i;
                        document.getElementById('resources').appendChild(noinfo);
                        $("#noinfo"+i).addClass("noinfo");
                        $("#noinfo"+i).append("This resource does not have any information on DBpedia yet");
                    } else {
                        var name = document.createElement('div');
                        name.id = "name"+i;
                        document.getElementById('resources').appendChild(name);
                        $("#name"+i).addClass("name");
                        $("#name"+i).append(queryResponse[i]['name']);
                    }

                    var leadText = document.createElement('div');
                    leadText.id = "leadText"+i;
                    document.getElementById('resources').appendChild(leadText);
                    $("#leadText"+i).addClass("leadText");
                    $("#leadText"+i).append(queryResponse[i]['leadText']);

                    var abstractText = document.createElement('div');
                    abstractText.id = "abstractText"+i;
                    document.getElementById('resources').appendChild(abstractText);
                    $("#abstractText"+i).addClass("abstractText");
                    $("#abstractText"+i).append(queryResponse[i]['abstractText']);
                }
            },
            error: function(error) {
            },
            complete: function(xhr, status) {
                $("#rating").fadeIn();
            }
        });
        return;
    }