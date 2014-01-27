var jobName = window.location.pathname.replace("/job/", "");

var parseStatus = function(id) {
    switch (id) {
        case 0: return "Success";
        case 1: return "Failure";
        case 2: return "Not Started";
        default: return "Unknown";
    }
};

var getStatusClass = function(id) {
    switch (id) {
        case 0: return "success";
        case 1: return "danger";
        default: return "";
    }
};

$.getJSON("/api/changes/" + jobName, function(changes) {
    changes.forEach(function(change) {
        var rev = change["revision"];
        var msg = change["message"];
        var author = change["author"];
    });
});