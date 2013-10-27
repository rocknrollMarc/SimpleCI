package com.directmyfile.ci

import com.directmyfile.ci.tasks.CommandTask
import com.directmyfile.ci.tasks.GradleTask

import org.vertx.groovy.core.Vertx

class CI {
    def vertx = Vertx.newVertx()
    def server = new WebServer(this)
    def configRoot = new File(".")
    def pluginManager = new PluginManager(this)
    Map<String, Task> taskTypes = [
            command: new CommandTask(),
            gradle: new GradleTask()
    ]

    Map<String, Job> jobs = [:]

    def start() {
        init()
        server.start(8080)
        loadJobs()
    }

    private void init() {
        new File(configRoot, 'logs').mkdirs()
        pluginManager.loadPlugins()
    }

    private def loadJobs() {
        def jobRoot = new File(configRoot, "jobs")
        for (File file : jobRoot.listFiles()) {
            if (file.name.endsWith(".json")) {
                def job = new Job(this, file)
                jobs.put(job.name, job)
            }
        }
        println "Loaded ${jobs.size()} jobs."
    }

    void runJob(Job job) {
        Thread.start("Builder[${job.name}]") {
            def success = true
            job.status = JobStatus.RUNNING
            println "Running Job: ${job.name}"

            job.getSCM().clone(job.buildDir)

            def tasks = job.tasks

            for (task in tasks) {
                def id = tasks.indexOf(task) + 1
                println "Running Task ${id} of ${job.tasks.size()}"
                def taskSuccess = task.task.execute(task.params)

                if (!taskSuccess) {
                    println 'Task has Failed'
                    success = false
                    break
                }

                println 'Task has Completed'
            }
            def artifacts = new File(artifactDir, "${job.name}")
            artifacts.mkdir()
            job.artifactLocations.each {
                def file = new File(job.buildDir, it)
                if (!file.exists()) {
                    job.logFile.append("\nArtifact File: ${file.canonicalPath} does not exist")
                    return
                }
                new File(artifacts, file.name).bytes = file.bytes
            }

            if (!success) {
                println 'Job has Failed'
                job.status = JobStatus.FAILURE
            } else {
                println 'Job has Completed'
                job.status = JobStatus.SUCCESS
            }
        }
    }

    def getArtifactDir() {
        def dir = new File(configRoot, "artifacts")
        dir.mkdir()
        return dir
    }
}
