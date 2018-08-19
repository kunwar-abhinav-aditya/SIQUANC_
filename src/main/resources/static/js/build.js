
    $(document).ready(function() {
        $("#buttonRow").hide();
        getAllTasks();
        $("#showResult").click(function() {
            $("#wait").css("display", "block");
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
                    $('#tasks').append("<label class=\"checkbox-inline\"><input type=\"checkbox\" name=\"tasks\" value=\""+opt+"\">"+opt+"</label>");
                }
           },
            error: function(error) {
            }
        });
    }

    function getCombinations() {
        var components = document.getElementById("components");
        if( $('#tasks :checked').length > 0){
            //build an array of selected values
            var selectedTasks = [];
            $('#tasks :checked').each(function(i, checked) {
                selectedTasks[i] = $(checked).val();
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
                                radioHtml += ">"+comp+"</label></div>";
                              });
                              div.innerHTML = radioHtml;
                              components.appendChild(div);
                            });
                       },
                        error: function(error) {
                        },
                        complete: function(xhr, status) {
                                        $("#wait").css("display", "none");
                        }
            });
        }
        $("#buttonRow").fadeIn();
    }

    function createPipeline() {
        var buildComponents = [];
        //enabled:checked added to only build pipeline with checked and enabled
        //this is for the case if we click on agdistis first to enable the
        //NER components, select a NER component and then click on a different
        //NED component, all the NER components become disabled but the one
        //we checked is still checked, although disabled. We thus need to add this
        $('input:radio:enabled:checked').each(function() {
           var radioCon = $(this).val();
           buildComponents.push(radioCon);
        });
        localStorage.setItem("pipeline", buildComponents);
        window.location.replace("/query");
    }

    function radioClick(name, value) {
        if (name == "NED") {
            if (value == "agdistis") {
                var elements = document.getElementsByName("NER");
                for(index = 0; index < elements.length; index++){
                    elements[index].disabled = false;
                }
            }
            else {
                var elements = document.getElementsByName("NER");
                for(index = 0; index < elements.length; index++){
                    elements[index].disabled = true;
                }
            }
        }
    }