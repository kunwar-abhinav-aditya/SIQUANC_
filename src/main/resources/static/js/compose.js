
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
        getTaskList();
        getInputList();
        getOutputList();
        $("#create").click(function() {
            addComponent();
        });
    });

    function getTaskList() {
        var tasks = document.getElementById("tasks");
        $.ajax({
            type: 'GET',
            url: 'http://frankenstein.sda.tech/build/tasks',
            success: function(allTasks) {
                for(var i =0;i < allTasks.length; i++)
                {
                    var opt = allTasks[i];
                    $('#tasks').append("<li class=\"checkbox keep-open\"><label><input type=\"checkbox\" id=\""+opt+"\" name=\"tasks\" value=\""+opt+"\">"+opt+"</label></li>");
                }
           },
            error: function(error) {
            }
        });
    }

    function getInputList() {
        var tasks = document.getElementById("inputs");
        $.ajax({
            type: 'GET',
            url: 'http://frankenstein.sda.tech/compose/inputs',
            success: function(allInputs) {
                for(var i =0;i < allInputs.length; i++)
                {
                    var opt = allInputs[i];
                    $('#inputs').append("<li class=\"checkbox keep-open\"><label><input type=\"checkbox\" id=\""+opt+"\" name=\"inputs\" value=\""+opt+"\">"+opt+"</label></li>");
                }
           },
            error: function(error) {
            }
        });
    }

    function getOutputList() {
        var tasks = document.getElementById("outputs");
        $.ajax({
            type: 'GET',
            url: 'http://frankenstein.sda.tech/compose/outputs',
            success: function(allOutputs) {
                for(var i =0;i < allOutputs.length; i++)
                {
                    var opt = allOutputs[i];
                    $('#outputs').append("<li class=\"checkbox keep-open\"><label><input type=\"checkbox\" id=\""+opt+"\" name=\"outputs\" value=\""+opt+"\">"+opt+"</label></li>");
                }
           },
            error: function(error) {
            }
        });
    }

    function addComponent() {

        if( $('#tasks :checked').length > 0 && $('#inputs :checked').length > 0 && $('#outputs :checked').length > 0){
            //build an array of selected values
            var tasks = [];
            var inputs = [];
            var outputs = [];
            $('#tasks :checked').each(function(i, checked) {
                tasks[i] = $(checked).val();
            });
            $('#inputs :checked').each(function(i, checked) {
                inputs[i] = $(checked).val();
            });
            $('#outputs :checked').each(function(i, checked) {
                outputs[i] = $(checked).val();
            });
            var payload = {
                "componentName" : $('#nameInput').val(),
                "tasks" : tasks,
                "inputs" : inputs,
                "outputs" : outputs
            };
            $.ajax({
                type: 'POST',
                url: 'http://frankenstein.sda.tech/compose',
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(payload),
                success: function(composeResponse) {
                    alert("Added");
                    $("#result").append("Component added successfully. Go to the build module to see it in action!");
                    $("#result").fadeIn();
                },
                error: function(error) {
                },
                complete: function(xhr, status) {
                }
            });
        }
    }