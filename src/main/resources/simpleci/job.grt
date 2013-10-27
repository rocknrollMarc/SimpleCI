<html>
    <head>
        <title><% println job.name %> - SimpleCI</title>
    </head>
    <body>
        <h1><% println job.name %></h1>
        <h2>Logs</h2>
        <p>
            <% println job.logFile.readLines().join('<br/>') %>
        </p>
    </body>
</html>