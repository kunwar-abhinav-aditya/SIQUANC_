
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
            url: 'http://localhost:8090/buildservice',
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
                        url: 'http://localhost:8090/buildservice',
                        dataType: 'json',
                        contentType: "application/json; charset=utf-8",
                        data: JSON.stringify(payload),
                        success: function(queryResponse) {
                            $.each(queryResponse, function(i, obj) {
                              var div = document.createElement("div");
                              div.className = "col-*-* box";
                              var radioHtml = "<legend class=\"rcorner\">"+i+"</legend>";
                              $.each(obj, function(i, comp) {
                                radioHtml += "<div class=\"radio\"><label><input type=\"radio\" name=\""+obj+"\" value=\""+comp+"\">"+comp+"</label></div>";
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
        $('input:radio:checked').each(function() {
           var radioCon = $(this).val();
           buildComponents.push(radioCon);
        });
        localStorage.setItem("pipeline", buildComponents);
        window.location.replace("/query");
    }