
    var result;
    var rating;
    var question;
    var components = new Array();
    $("#thanks").hide();
    $("#rating").hide();
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
        $("#result").hide();
        $("#moreInfo").hide();
        $("#showResult").click(function() {
            doQuery();
        });
        $("#defaultQuestion1").click(function() {
            doQuery();
        });
        $("#defaultQuestion2").click(function() {
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
            $("#pipelineType").append("<br><br>Go back to querying with the <button class=\"link\" id=\"gotofixed\" onclick=\"backToBasics()\">fixed pipeline</button>");
        }
        else {
            $("#pipelineType").append("Querying using a fixed pipeline. To run it against your own custom pipeline, go to the <a href=\"/build\">build</a> module!");
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
        $(".jumbotron").addClass('blurdiv');
        $("#wait").css("display", "block");
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
        if (localStorage.getItem("defaultQuestion") != null) {
            question = localStorage.getItem("defaultQuestion");
            var payload = { "queryRequestString" : localStorage.getItem("defaultQuestion"), "components" : components};
        }
        else {
            question = $('#queryInput').val();
            var payload = { "queryRequestString" : $('#queryInput').val(), "components" : components};
        }
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
                localStorage.removeItem("defaultQuestion");
                $(".jumbotron").removeClass('blurdiv');
                $("#wait").css("display", "none");
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


    function backToBasics() {
        localStorage.removeItem("pipeline");
        window.location.reload();
    }

    function setdefaultquestion1() {
        localStorage.removeItem("defaultQuestion");
        localStorage.setItem("defaultQuestion", $("#defaultQuestion1").text());
    }

    function setdefaultquestion2() {
        localStorage.removeItem("defaultQuestion");
        localStorage.setItem("defaultQuestion", $("#defaultQuestion2").text());
    }