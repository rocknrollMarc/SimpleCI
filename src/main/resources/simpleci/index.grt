<html>
    <head>
        <title>SimpleCI</title>
        <link rel="stylesheet" href="css/index.css">
    </head>

    <body>
        <div class="centered">
            <h1>SimpleCI</h1>
        </div>
        <table border="1">
            <%
            import com.directmyfile.ci.Job

            ci.jobs.each { job ->
                println("<tr><td><a href=\"job/${job.name}\">${job.name}</a></td></tr>")
            }
            %>
        </table>
    </body>
</html>