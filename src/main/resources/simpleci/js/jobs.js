var jobList = $("#jobList");

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

$.getJSON("/api/jobs", function (jobs) {
    jobs.forEach(function (job) {
        jobList.append('<tr id="job-' + job.name + '"><td>' + '<a href="' + "/job/" + job.name + '">' + job.name + '</a></td></tr>');
        $("#job-" + job.name).append("<td>" + parseStatus(job.status) + "</td>").addClass(getStatusClass(job.status));
    });
});