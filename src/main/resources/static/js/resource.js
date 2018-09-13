    $(document).ready(function() {
        fetchDetails();
    });

    function fetchDetails() {
        getLeadAndAbstract();
    }

    function getLeadAndAbstract(){
        var resourceURL = localStorage.getItem("resourceURL");
        var payload = { "resourceURL" : resourceURL};
        $.ajax({
            type: 'POST',
            url: 'http://localhost:10000/query/resource',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                var name = queryResponse['name'];
                $("#name").append(name);
                var leadText = queryResponse['leadText'];
                $("#leadText").append(leadText);
                var abstractText = queryResponse['abstractText'];
                $("#abstractText").append(abstractText);
            },
            error: function(error) {
            },
            complete: function(xhr, status) {
                $("#rating").fadeIn();
            }
        });
        return;
    }