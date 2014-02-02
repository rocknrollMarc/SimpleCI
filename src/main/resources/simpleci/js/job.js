var jobName = window.location.pathname.replace("/job/", "");

document.title = jobName + " - SimpleCI";
$(".job-name").html(jobName);

var parseStatus = function (id) {
    switch (id) {
        case 0:
            return "Success";
        case 1:
            return "Failure";
        case 2:
            return "Not Started";
        default:
            return "Unknown";
    }
};

var getStatusClass = function (id) {
    switch (id) {
        case 0:
            return "success";
        case 1:
            return "danger";
        default:
            return "";
    }
};

$(document).ready(function () {
    // Provide class='active' switching on the tabs.
    $(".nav-tabs li").each(function (index, tab) {
        tab = $(tab);
        console.log("Applying Active Switch");
        $(tab).click(function () {
            $(".nav-tabs li").each(function (i, t) {
                t = $(t);
                t.removeClass("active");
                $("#" + t.attr("data-target")).hide();
            });
            tab.addClass("active");
            var target = tab.attr("data-target");
            $("#" + target).show();
        });
    });

    $.getJSON("/api/changes/" + jobName, function (changes) {
        var $changes = $("#changelog");

        changes.forEach(function (change) {
            var rev = change["revision"];
            var msg = change["message"];
            var author = change["author"];
            $changes.append("<p class=\"list-group-item\">" + msg + " by " + author.bold() + "</p>");
        });
    });

    $.getJSON("/api/history/" + jobName, function (history) {
        var $history = $("#history");

        history["history"].forEach(function (entry) {
            var status = entry["status"];
            var number = entry["number"];
            var timestamp = entry["timestamp"];

            var msg = number + " - " + parseStatus(status);

            $history.append("<p>" + msg + "</p>");
        });
    });
});