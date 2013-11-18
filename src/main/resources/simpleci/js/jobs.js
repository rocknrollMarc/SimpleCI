function writeJobList() {
    $.getJSON("/jobs/json", function(jobs) {
        var list = $('#jobList');
        jobs.each(function(job) {
            list.append("<tr><td><a href=\"job/\"" + job + ">" + job + "</a></td></tr>");
        });
    });
}

writeJobList();