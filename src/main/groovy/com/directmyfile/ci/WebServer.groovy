package com.directmyfile.ci

import groovy.text.SimpleTemplateEngine
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.http.HttpServerRequest
import org.vertx.groovy.core.http.RouteMatcher

class WebServer {
    def vertx = Vertx.newVertx()
    def server = vertx.createHttpServer()
    CI ci

    def templateEngine = new SimpleTemplateEngine()

    WebServer(CI ci) {
        this.ci = ci
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

            if (!ci.jobs.containsKey(jobName)) return

            def job = ci.jobs[jobName]

            println "Job Hook executing job ${jobName}"

            ci.runJob(job)
        }

        matcher.get('/artifact/:job/:name') { HttpServerRequest request ->
            def jobName = request.params['job'] as String
            def artifact = request.params['name'] as String

            if (!ci.jobs.containsKey(jobName)) { writeResource(request, "404.html") ; return }

            def artifactFile = new File(ci.artifactDir, "${jobName}/${artifact}")

            if (!artifactFile.exists()) { writeResource(request, "404.html") ; return }

            request.response.sendFile(artifactFile.absolutePath)
        }

        matcher.noMatch { HttpServerRequest r ->
            writeResource(r, "404.html")
        }
    }

    private def writeResource(HttpServerRequest r, String path) {
        def stream = this.class.classLoader.getResourceAsStream("simpleci/" + path)

        if (stream==null) {
            writeResource(r, "404.html")
        }

        r.response.end(stream.readLines().join('\n'))
    }

    private void writeTemplate(HttpServerRequest request, String path, Map binding) {
        binding.put("ci", ci)
        binding.put("request", request)
        def stream = this.class.classLoader.getResourceAsStream("simpleci/" + path)

        if (stream==null) {
            writeResource(request, "404.html")
        }

        def text = stream.readLines().join('\n')

        def out = new StringWriter()
        templateEngine.createTemplate(text).make(binding).writeTo(out)
        request.response.end(out.toString())
    }

    private void writeTemplate(HttpServerRequest request, String path) {
        writeTemplate(request, path, [:])
    }
}
