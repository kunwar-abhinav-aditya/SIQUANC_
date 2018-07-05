    $(document).ready(function() {
        alert("hi");
          $( "#showResult" ).click(function() {
                  doQuery();
          });
    });

    function doQuery() {
        $.ajax({
            type: 'POST',
            url: 'http://localhost:8080/query',
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            data: {
                    "query" : $('#queryInput').val()
                    },
            success: function(queryResponse) {
                    $('#result').append(queryResponse);
           },
            error: function(error) {
                $('#result').append("Query did't fetch any result");
            }
        });
        return;
    }