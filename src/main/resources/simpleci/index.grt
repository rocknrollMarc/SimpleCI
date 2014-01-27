<!DOCTYPE html>
<html>
<head>
    <title>SimpleCI</title>
    <link rel="stylesheet" href="/css/core.css">
    <link href="/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/todc-bootstrap.min.css" rel="stylesheet">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>

<body class="matchStrap">

<nav class="navbar navbar-masthead navbar-default navbar-fixed-top" role="navigation">
    <div class="navbar-header"><a class="navbar-brand" href="#">SimpleCI</a></div>
    <ul class="nav navbar-nav" id="navigate">
        <li class="active"><a href="#">Home</a></li>
        <li><a href="/jobs">Jobs</a></li>
    </ul>
    <ul class="nav navbar-nav pull-right" id="navigate-right">
        <li><a href="/login">Login</a></li>
    </ul>
</nav>
<div class="centered jumbotron">
    <div class="container">
        <br/>

        <img src="/img/logo.png" height="200">
    </div>
</div>
<table class="centered table table-bordered job-table" border="1">
    <%
    import com.directmyfile.ci.jobs.Job

    ci.jobs.values().each { job ->
        println("<tr class=" + job.status.cssContextClass() + "><td><a href=\"job/${job.name}\">${job.name}</a></td></tr>")
    }
    %>
</table>

<script src="/js/bootstrap.min.js"></script>
</body>
</html>
