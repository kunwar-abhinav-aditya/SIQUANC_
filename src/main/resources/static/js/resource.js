    $(document).ready(function() {
        fetchDetails();
    });

    function fetchDetails() {
        getLeadAndAbstract();
    }

    function getLeadAndAbstract(){
        var resourceURLList = localStorage.getItem("resourceURLs");
        var resourceURLs = resourceURLList.split(",");
        var payload = resourceURLs;
        $.ajax({
            type: 'POST',
            //url: 'http://frankenstein.sda.tech/query/resource',
            url: 'http://localhost:10000/query/resource',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: JSON.stringify(payload),
            success: function(queryResponse) {
                for(var i=0;i < queryResponse.length; i++)
                {
                    if (queryResponse[i]['name'].includes("http://dbpedia.org/")) {
                        var adiv = document.createElement('div');
                        adiv.id = "adiv"+i;
                        document.getElementById('result').appendChild(adiv);
                        $("#adiv"+i).addClass("namenotexists");

                        var name = document.createElement('a');
                        name.id = "name"+i;
                        document.getElementById('adiv'+i).appendChild(name);

                        $("#name"+i).addClass("namenotexists");
                        $("#name"+i).append(queryResponse[i]['name']);
                        $("#name"+i).attr("href", queryResponse[i]['name']);

                        var noinfo = document.createElement('div');
                        noinfo.id = "noinfo"+i;
                        document.getElementById('result').appendChild(noinfo);
                        $("#noinfo"+i).addClass("noinfo");
                        $("#noinfo"+i).append("This resource does not have any information on DBpedia yet");
                    } else {
                        var name = document.createElement('div');
                        name.id = "name"+i;
                        document.getElementById('result').appendChild(name);
                        $("#name"+i).addClass("name");
                        $("#name"+i).append(queryResponse[i]['name']);
                    }

                    var leadText = document.createElement('div');
                    leadText.id = "leadText"+i;
                    document.getElementById('result').appendChild(leadText);
                    $("#leadText"+i).addClass("leadText");
                    $("#leadText"+i).append(queryResponse[i]['leadText']);

                    var abstractText = document.createElement('div');
                    abstractText.id = "abstractText"+i;
                    document.getElementById('result').appendChild(abstractText);
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