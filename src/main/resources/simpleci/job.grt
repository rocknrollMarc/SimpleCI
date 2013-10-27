<html>
    <head>
        <title><% print job.name %> - SimpleCI</title>
        <link rel="stylesheet" href="css/core.css">
        <link href="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="centered matchStrap">
        <h1 class="centered"><% print job.name %></h1>
        <h2>Logs</h2>
        <p class="panel <% print job.status.panelClass %>">
            <% print job.logFile.readLines().join('<br/>') %>
        </p>

        <table class="table centered table-striped" border="1">
            <% print job.generateArtifactList() %>
        </table>

        <script src="http://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
        <script src="http://netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
    </body>
</html>