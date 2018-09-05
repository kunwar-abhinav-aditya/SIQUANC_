
    $(document).ready(function() {
        $("#inputGroupFile01").change(function() {
            $('#upload-file-info').empty();
            $('#upload-file-info').append("<i class='fa fa-file-excel-o'></i>&nbsp;");
            $('#upload-file-info').append($("#inputGroupFile01")[0].files[0].name);
            $('#upload-file-info').append("<br><button class=\"btn btn-primary btn-sm\" type=\"button\" id=\"queryBulk\" onclick=\"queryBulk()\">Query this bulk!</button>");
        });
    });

    function queryBulk() {
        $(".jumbotron").addClass('blurdiv');
        $("#wait").css("display", "block");
        var documentData = new FormData();
        documentData.append('file', $("#inputGroupFile01")[0].files[0]);
        $.ajax({
            url: 'http://localhost:10000/query/bulk',
            type: 'POST',
            data: documentData,
            contentType: false,
            processData: false,
            success: function (response) {
                $("#wait").css("display", "none");
                $(".jumbotron").removeClass('blurdiv');
            },
            error: function(error) {
                alert("Document could not be uploaded");
            }
        });
        return;
    }