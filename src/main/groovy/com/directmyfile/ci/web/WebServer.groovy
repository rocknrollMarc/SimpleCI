package com.directmyfile.ci.web

import com.directmyfile.ci.core.CI
import com.directmyfile.ci.utils.Utils
import groovy.json.JsonBuilder
import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

import java.sql.Timestamp

class WebServer {
    HttpServer server
    CI ci

    WebServer(CI ci) {
        this.ci = ci
        server = ci.vertxManager.vertx.createHttpServer()
    }

    def start(int port, String ip) {
        server.requestHandler(configure(new RouteMatcher()).asClosure())
        server.listen(port, ip)
    }

    private def configure(RouteMatcher matcher) {
        matcher.get('/') { HttpServerRequest r ->
            writeResource(r, "index.html")
        }

        matcher.get('/css/:file') { HttpServerRequest r ->
            writeResource(r, "css/${r.params['file']}")
        }

        matcher.get('/js/:file') { HttpServerRequest r ->
            writeResource(r, "js/${r.params['file']}")
        }

        matcher.get('/img/:file') { HttpServerRequest r ->
            writeImage(r, "img/${r.params['file']}")
        }

        matcher.get('/fonts/:file') { HttpServerRequest r ->
            writeImage(r, "fonts/${r.params['file']}")
        }

        matcher.get('/job/:name') { HttpServerRequest r ->
            writeResource(r, "job.html")
        }

        matcher.get('/api/log/:job') { HttpServerRequest request ->
            def jobName = request.params['job'] as String

            if (!ci.jobs.containsKey(jobName)) {
                writeResource(request, "404.html"); return
            }

            def job = ci.jobs.get(jobName)

            if (!job.logFile.exists()) {
                writeResource(request, "404.html")
            } else {
                request.response.sendFile(job.logFile.absolutePath)
            }
        }

        matcher.get('/hook/:name') {
            def jobName = it.params['name'] as String
            it.response.end('')

            if (!ci.jobs.containsKey(jobName)) {
                it.response.end(new JsonBuilder([
                        error: "Job does not exist!"
                ]).toPrettyString())
            }

            it.response.end(new JsonBuilder([
                    error: null,
            ]).toPrettyString())

            def job = ci.jobs[jobName]

            ci.logger.info "Job Hook executing job ${jobName}"

            ci.runJob(job)
        }

        matcher.get('/artifact/:job/:id/:name') { HttpServerRequest request ->
            def jobName = request.params['job'] as String
            def artifact = request.params['name'] as String
            def id = request.params['id'] as String
            if (!ci.jobs.containsKey(jobName)) {
                writeResource(request, "404.html"); return
            }

            def artifactFile = new File(ci.artifactDir, "${jobName}/${id}/${artifact}")

            if (!artifactFile.exists()) {
                writeResource(request, "404.html"); return
            }

            request.response.sendFile(artifactFile.absolutePath)
        }

        matcher.get('/jobs') { HttpServerRequest r ->
            writeResource(r, "jobs.html")
        }

        matcher.get('/api/jobs') { HttpServerRequest r ->
            def jobInfo = []

            ci.jobs.values().each { job ->
                jobInfo += [
                        name  : job.name,
                        status: job.status.ordinal()
                ]
            }

            r.response.end(Utils.encodeJSON(jobInfo) as String)
        }

        matcher.post('/github/:name') {
            def jobName = it.params['name'] as String
            it.response.end('')

            if (!ci.jobs.containsKey(jobName)) return

            def job = ci.jobs[jobName]

            ci.logger.info "GitHub Hook executing job ${jobName}"

            ci.runJob(job)
        }

        matcher.get('/api/changes/:name') { HttpServerRequest r ->
            def jobName = r.params['name'] as String

            if (!ci.jobs.containsKey(jobName)) {
                writeResource(r, "404.html")
                return
            }

            def job = ci.jobs[jobName]

            def changelog = job.changelog.entries

            r.response.end(Utils.encodeJSON(changelog))
        }

        matcher.get('/api/history/:name') { HttpServerRequest r ->
            def jobName = r.params['name'] as String

            if (!ci.jobs.containsKey(jobName)) {
                writeResource(r, "404.html")
                return
            }

            def job = ci.jobs[jobName]

            def history = job.history

            def out = [:]

            out["length"] = history.entries.size()

            def histories = new ArrayList<Map<String, Object>>(history.latestBuild?.number ?: 0)

            history.entries.each { entry ->
                histories.add([
                        id       : entry.id,
                        number   : entry.number,
                        status   : entry.status,
                        log      : entry.log,
                        timestamp: (entry.logTime as Timestamp)
                ])
            }

            out["history"] = histories

            r.response.end(Utils.encodeJSON(out))
        }

        matcher.get('/login') { HttpServerRequest r ->
            writeResource(r, "login.html")
        }

        /*matcher.post("/login") { HttpServerRequest request ->
            request.expectMultiPart = true
            request.endHandler { end ->
                println request
                def user = request.formAttributes.get("username")
                def pass = request.formAttributes.get("password")
                if (!user || !pass) {
                    request.response.statusCode = 400
                    request.response.end(Utils.encodeJSON([
                            error: "Invalid Request",
                            info: [
                                    "Both Username and Password is required."
                            ]
                    ]))
                }
            }
        }*/

        matcher.noMatch { HttpServerRequest r ->
            writeResource(r, "404.html")
        }
    }

    private def writeResource(HttpServerRequest r, String path) {

        def stream = getStream(path)

        if (stream == null) {
            writeResource(r, "404.html")
        }

        r.response.end(stream.text)
    }

    private void writeImage(HttpServerRequest request, String path) {
        request.response.putHeader("content-type", "image/*")

        def stream = getStream(path)

        if (stream == null) {
            request.response.statusCode = 404
            writeResource(request, "404.html")
        } else {
            request.response.end(new Buffer().appendBytes(stream.bytes))
        }
    }

    private def getStream(String path) {
        def dir = new File(ci.configRoot, "www")
        InputStream stream
        if (!dir.exists()) {
            stream = Utils.resource("simpleci/${path}")
        } else {
            def file = new File(dir, path)
            if (!file.exists()) return null
            stream = file.newInputStream()
        }
        return stream
    }
}
