
    var taskAndComponents;
    var selectedTasks = [];
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
        $("#buttonRow").hide();
        getAllTasks();
        $("#showResult").click(function() {
            $("#components").empty();
            getCombinations();
        });
        $("#buildPipeline").click(function() {
            createPipeline();
        });

    });

    function getAllTasks() {
        var tasks = document.getElementById("tasks");
        $.ajax({
            type: 'GET',
            url: 'http://localhost:10000/build/tasks',
            success: function(allTasks) {
                $('#tasks').append("<legend class=\"rcorner\">Choose one or more tasks</legend>");
                for(var i =0;i < allTasks.length; i++)
                {
                    var opt = allTasks[i];
                    $('#tasks').append("<label class=\"checkbox-inline\"><input type=\"checkbox\" id=\""+opt+"\" name=\"tasks\" value=\""+opt+"\">"+opt+"</label>");
                }
           },
            error: function(error) {
            }
        });
    }

    function getCombinations() {
        selectedTasks = [];
        var components = document.getElementById("components");
        if( $('#NED').is(":checked")) {
            var ner = $('#NER');
            ner.attr('checked','checked');
        }
        if( $('#tasks :checked').length > 0){
            $("#wait").css("display", "block");
            //build an array of selected values
            $('#tasks :checked').each(function(i, checked) {
                selectedTasks.push($(checked).val());
            });
            var payload = { "selectedTasks" : selectedTasks};
            //post data to handler script. note the JSON.stringify call
            $.ajax({
                        type: 'POST',
                        url: 'http://localhost:10000/build',
                        dataType: 'json',
                        contentType: "application/json; charset=utf-8",
                        data: JSON.stringify(payload),
                        success: function(queryResponse) {
                            taskAndComponents = queryResponse;
                            $.each(queryResponse, function(i, obj) {
                              rank = 0;
                              var div = document.createElement("div");
                              div.className = "col-*-* box";
                              var radioHtml = "<legend class=\"rcorner\">"+i+"</legend>";
                              $.each(obj, function(j, comp) {
                                rank += 1;
                                radioHtml += "<div class=\"radio a"+rank+"\"><label><input type=\"radio\" name=\""+i+"\" value=\""+comp+"\" onclick=\"radioClick(this.name, this.value);\"";
                                if (i == "NER") {
                                    radioHtml += "disabled=\"true\"";
                                }
                                radioHtml += "><a href=\"/stars\">"+comp+"</a></label></div>";
                              });
                              div.innerHTML = radioHtml;
                              components.appendChild(div);
                            });
                       },
                        error: function(error) {
                        },
                        complete: function(xhr, status) {
                            getBestPipelines();
                            $("#wait").css("display", "none");
                        }
            });
            $("#buttonRow").fadeIn();
        }
    }

    function getBestPipelines() {
        var payload = taskAndComponents;
        $.ajax({
            type: 'POST',
            url: 'http://localhost:10000/build/bestpipelines',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(bestPipelines) {
                $('#bestPipelines').empty();
                $('#bestPipelines').append("<br>The best pipelines to run<br>");
                for(var i=0;i < bestPipelines.length; i++)
                {
                    var opt = bestPipelines[i];
                    $('#bestPipelines').append("<br>");
                    for(var j=0;j< opt.length; j++)
                    {
                        var comp = opt[j];
                        $('#bestPipelines').append("<button type=\"button\" class=\"btn btn-outline-dark btn-sm box\" disabled>"+comp+"</button>");
                        if (j < opt.length - 1) {
                            $('#bestPipelines').append("<i class=\"fa fa-arrow-right box\"></i>");
                        }
                    }
                    $('#bestPipelines').append("&nbsp;&nbsp;<button type=\"button\" value=\""+opt+"\" class=\"btn btn-primary btn-sm box\"onclick=\"usethis(this.value);\">USE</button>");
                    $('#bestPipelines').append("<br>");
                }
           },
            error: function(error) {
            }
        });
    }


    function usethis(opt) {
        localStorage.removeItem("pipeline");
        localStorage.setItem("pipeline", opt);
        localStorage.removeItem("selectedTasks");
        localStorage.setItem("selectedTasks", selectedTasks);
        window.location.replace("/query");
    }

    function createPipeline() {
        var buildComponents = [];
        var checked = 0;
        //enabled:checked added to only build pipeline with checked and enabled
        //this is for the case if we click on agdistis first to enable the
        //NER components, select a NER component and then click on a different
        //NED component, all the NER components become disabled but the one
        //we checked is still checked, although disabled. We thus need to add this
        $('input:radio:enabled:checked').each(function() {
           checked=checked+1;
           var radioCon = $(this).val();
           buildComponents.push(radioCon);
        });
        if (checked > 0) {
            localStorage.setItem("pipeline", buildComponents);
            if (checked != selectedTasks.length) {
                selectedTasks.shift();
            }
            localStorage.setItem("selectedTasks", selectedTasks);
            if(localStorage.getItem("context") == "simple" || localStorage.getItem("context") == null) {
                window.location.replace("/query");
            }
            if(localStorage.getItem("context") == "bulk") {
                window.location.replace("/bulk");
            }
        }
    }

    function radioClick(name, value) {
        selectedTasks.push(name);
        if (name == "NED") {
            if (value == "NED-AGDISTIS") {
                var elements = document.getElementsByName("NER");
                for(index = 0; index < elements.length; index++){
                    elements[index].disabled = false;
                }
                elements[0].checked = true;
            }
            else {
                var elements = document.getElementsByName("NER");
                for(index = 0; index < elements.length; index++){
                    elements[index].disabled = true;
                }
            }
        }
    }