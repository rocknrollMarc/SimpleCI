<html>
<head>
    <title><% print job.name %> - SimpleCI</title>
    <link rel="stylesheet" href="css/core.css">
    <link href="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="centered matchStrap">

<nav class="navbar navbar-default navbar-fixed-top navbar-inverse" role="navigation">
    <div class="navbar-header"><a class="navbar-brand" href="#">SimpleCI</a></div>
    <ul class="nav navbar-nav" id="navigate">
        <li><a href="/">Home</a></li>
        <li class="active"><a href="/jobs">Jobs</a></li>
    </ul>
</nav>
<br/>
<br/>

<h1 class="centered"><% print job.name %></h1>
<br/>

<div class="panel <% print job.status.panelClass %>">
    <div class="panel-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#buildLog">
            <h2 class="panel-title">Build Log</h2>
        </a>
    </div>
    <div id="buildLog" class="panel-content panel-collapse collapse">
        <% if (job.logFile.exists()) {print job.logFile.readLines().join('<br/>')} else {print "&nbsp;&nbsp;No Log Found"} %>
    </div>
</div>

<div class="panel panel-info">
    <div class="panel-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#changes">
            <h2 class="panel-title">Recent Changes</h2>
        </a>
    </div>
    <div id="changes" class="panel-content panel-collapse collapse">
        <%
        print job.changelog.generateHTML()
        %>
    </div>
</div>

<div class="panel panel-info">
    <div class="panel-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion" href="#builds">
            <h2 class="panel-title">Recent Builds</h2>
        </a>
    </div>
    <div id="builds" class="panel-content panel-collapse collapse">
        <%
        def h = job.history
        print h.toHTML()
        %>
    </div>
</div>

<table class="table centered table-striped" border="1">
    <% print job.generateArtifactList() %>
</table>

<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
<script src="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
</body>
</html>
