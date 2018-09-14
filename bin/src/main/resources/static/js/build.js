    $(document).ready(function() {
        $('#taskLabel').hide();
        $('#tasks').hide();
        $( "#1" ).click(function() {
            $('#taskLabel').show();
            loadOnes();
        });
        $( "#2" ).click(function() {
            $('#taskLabel').show();
            loadTwos();
        });
        $( "#3" ).click(function() {
            $('#taskLabel').show();
            loadThrees();
        });
        $( "#4" ).click(function() {
            $('#taskLabel').show();
            loadFours();
        });
        $( "#5" ).click(function() {
            $('#taskLabel').show();
            loadFives();
        });
    });

    function loadOnes() {
        var payload = {queryRequestString : $('#queryInput').val()};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:8080/build',
            contentType: "application/json; charset=utf-8",
            data: payload,
            success: function(queryResponse) {
                $('#result').append(queryResponse.queryResponseString);
           },
            error: function(error) {
                $('#result').append("Query did't fetch any result");
            }
        });
    }
