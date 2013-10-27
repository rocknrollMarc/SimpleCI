package com.directmyfile.ci

import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.api.Task
import com.directmyfile.ci.exception.JobConfigurationException
import com.directmyfile.ci.helper.SqlHelper
import com.directmyfile.ci.scm.GitSCM
import com.directmyfile.ci.tasks.CommandTask
import com.directmyfile.ci.tasks.GradleTask
import com.directmyfile.ci.tasks.MakeTask
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.vertx.groovy.core.Vertx

class CI {

    def logger = {
        BasicConfigurator.configure()
        def logger = Logger.getLogger(this.class.name)
        logger.setLevel(Level.INFO)
        return logger
    }()

    def vertx = Vertx.newVertx()
    def server = new WebServer(this)
    def configRoot = new File(".")
    int port = 0
    def pluginManager = new PluginManager(this)
    def config = new CiConfig(this)
    def sql = new SqlHelper(this)

    Map<String, Task> taskTypes = [
            command: new CommandTask(),
            gradle: new GradleTask(),
            make: new MakeTask()
    ]

    Map<String, SCM> scmTypes = [
            git: new GitSCM()
    ]

    Map<String, Job> jobs = [:]

    def start() {
        init()
        server.start(port)
        loadJobs()
    }

    private void init() {
        config.load()
        sql.init()
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

            def scmConfig = job.getSCM()

            if (!scmTypes.containsKey(scmConfig.type)) throw new JobConfigurationException("Unkown SCM Type ${scmConfig.type}")

            def scm = scmTypes.get(scmConfig.type)

            if (scm.exists(job)) {
                scm.update(job)
            } else {
                scm.clone(job)
            }

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
