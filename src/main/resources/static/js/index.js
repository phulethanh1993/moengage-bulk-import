$(document).ready(function () {
    $("#fetch-from-Redshift").submit(function (e) {
        e.preventDefault();
        pollServerRedshift();
    })
    function pollServerRedshift() {
        $.ajax({ // this is a json object inside the function
            url: "/import-redshift",
            type: 'POST',
            contentType: "application/json",
            async: false,
            cache: false,
            success: function (res) {
                $("#respRedshift").html(res);
            },
            error: function (res) {
                $("#respRedshift").html(res);
                },
            // // removed for clarity
            // complete: function(){
            //     setTimeout(pollServerRedshift, 20000);
            // }
        });
    }
});