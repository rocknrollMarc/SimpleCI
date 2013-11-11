package com.directmyfile.ci.web

import com.directmyfile.ci.core.CI
import groovy.json.JsonBuilder
import groovy.text.SimpleTemplateEngine
import org.vertx.groovy.core.http.HttpServer
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

class WebServer {
    HttpServer server
    CI ci

    def templateEngine = new SimpleTemplateEngine()

    WebServer(CI ci) {
        this.ci = ci
        server = ci.vertxManager.vertx.createHttpServer()
    }

    def start(int port) {
        server.requestHandler(configure(new RouteMatcher()).asClosure())
        server.listen(port)
    }

    private def configure(RouteMatcher matcher) {
        matcher.get('/') { HttpServerRequest r ->
            writeTemplate(r, "index.grt")
        }

        matcher.get('/css/:file') { HttpServerRequest r ->
            writeResource(r, "css/${r.params['file']}")
        }

        matcher.get('/js/:file') { HttpServerRequest r ->
            writeResource(r, "js/${r.params['file']}")
        }

        matcher.get('/job/:name') { HttpServerRequest r ->
            writeTemplate(r, "job.grt", [
                    jobName: r.params['name'],
                    job: ci.jobs.get(r.params['name'])
            ])
        }

        matcher.get('/log/:job') { HttpServerRequest request ->
            def jobName = request.params['job'] as String
            if (!ci.jobs.containsKey(jobName)) { writeResource(request, "404.html") ; return }

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
            if (!ci.jobs.containsKey(jobName)) { writeResource(request, "404.html") ; return }

            def artifactFile = new File(ci.artifactDir, "${jobName}/${id}/${artifact}")

            if (!artifactFile.exists()) { writeResource(request, "404.html") ; return }

            request.response.sendFile(artifactFile.absolutePath)
        }

        matcher.get('/jobs') { HttpServerRequest it ->
            writeTemplate(it, "jobs.grt")
        }

        matcher.post('/github/:name') {
            def jobName = it.params['name'] as String
            it.response.end('')

            if (!ci.jobs.containsKey(jobName)) return

            def job = ci.jobs[jobName]

            ci.logger.info "GitHub Hook executing job ${jobName}"

            ci.runJob(job)
        }

        matcher.get('/changes/:name') { HttpServerRequest r ->
            def jobName = r.params['name'] as String

            if (!ci.jobs.containsKey(jobName)) {
                writeResource(r, "404.html")
                return
            }

            def job = ci.jobs[jobName]

            def changelog = job.changelog.entries

            def out = ""

            for (entry in changelog) {
                out = "${out}${entry.revision}: ${entry.message}\n"
            }

            r.response.end(out)
        }

        matcher.noMatch { HttpServerRequest r ->
            writeResource(r, "404.html")
        }
    }

    private def writeResource(HttpServerRequest r, String path) {
        
        def stream = getStream(path)

        if (stream==null) {
            writeResource(r, "404.html")
        }

        r.response.end(stream.readLines().join('\n'))
    }

    private void writeTemplate(HttpServerRequest request, String path, Map binding) {
        binding.put("ci", ci)
        binding.put("request", request)
        def stream = getStream(path)

        if (stream==null) {
            writeResource(request, "404.html")
        }

        def text = stream.text

        def out = new StringWriter()
        templateEngine.createTemplate(text).make(binding).writeTo(out)
        request.response.end(out.toString())
    }

    private def getStream(String path) {
        def dir = new File(ci.configRoot, "www")
        InputStream stream
        if (!dir.exists()) {
            stream = this.class.classLoader.getResourceAsStream("simpleci/${path}")
        } else {
            def file = new File(dir, path)
            if (!file.exists()) return null
            stream = file.newInputStream()
        }
        return stream
    }

    private void writeTemplate(HttpServerRequest request, String path) {
        writeTemplate(request, path, [:])
    }
}
