<html>
<head>
    <title>SimpleCI</title>
    <link rel="stylesheet" href="css/core.css">
    <link href="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet">
</head>

<body class="matchStrap">

<nav class="navbar navbar-default navbar-fixed-top navbar-inverse" role="navigation">
    <div class="navbar-header"><a class="navbar-brand" href="#">SimpleCI</a></div>
    <ul class="nav navbar-nav" id="navigate">
        <li><a href="/">Home</a></li>
        <li class="active"><a href="/jobs">Jobs</a></li>
    </ul>
</nav>

<br/>
<table class="centered table table-bordered" border="1">
    <%
    import com.directmyfile.ci.Job

    ci.jobs.values().each { job ->
    println("<tr><td><a href=\"job/${job.name}\">${job.name}</a></td></tr>")
    }
    %>
</table>

<script src="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
</body>
</html>