    $(document).ready(function() {
          $( "#showResult" ).click(function() {
            doQuery();
          });
    });

    function doQuery() {
        $('#result').empty();
        var payload = {queryRequestString : $('#queryInput').val()};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:8080/query',
            contentType: "application/json; charset=utf-8",
            data: payload,
            success: function(queryResponse) {
                $('#result').append(queryResponse.queryResponseString);
           },
            error: function(error) {
                $('#result').append("Query did't fetch any result");
            }
        });
        return;
    }