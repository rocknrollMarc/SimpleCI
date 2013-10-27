<html>
    <head>
        <title><% print job.name %> - SimpleCI</title>
    </head>
    <body>
        <h1><% print job.name %></h1>
        <h2>Logs</h2>
        <p>
            <% print job.logFile.readLines().join('<br/>') %>
        </p>

        <table border="1">
            <% print job.generateArtifactList() %>
        </table>
    </body>
</html>