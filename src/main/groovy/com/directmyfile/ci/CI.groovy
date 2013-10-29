package com.directmyfile.ci

import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.api.Task
import com.directmyfile.ci.exception.JobConfigurationException
import com.directmyfile.ci.helper.SqlHelper
import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.jobs.JobStatus
import com.directmyfile.ci.notify.IRCBot
import com.directmyfile.ci.scm.GitSCM
import com.directmyfile.ci.tasks.CommandTask
import com.directmyfile.ci.tasks.GradleTask
import com.directmyfile.ci.tasks.MakeTask
import com.directmyfile.ci.web.VertxManager
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger

class CI {

    def logger = {
        BasicConfigurator.configure()
        def logger = Logger.getLogger(this.class.name)
        logger.setLevel(Level.INFO)
        return logger
    }()

    def configRoot = new File(".")
    int port = 0
    def pluginManager = new PluginManager(this)
    def config = new CiConfig(this)
    def sql = new SqlHelper(this)

    def ircBot = new IRCBot()

    Map<String, Task> taskTypes = [
            command: new CommandTask(),
            gradle: new GradleTask(),
            make: new MakeTask()
    ]

    Map<String, SCM> scmTypes = [
            git: new GitSCM()
    ]

    Map<String, Job> jobs = [:]

    def vertxManager = new VertxManager(this)

    def eventBus = vertxManager.eventBus

    def start() {
        init()
        loadJobs()
        vertxManager.setupWebServer()
        ircBot.start(this)
    }

    private void init() {
        config.load()
        sql.init()
        new File(configRoot, 'logs').mkdirs()
        pluginManager.loadPlugins()
    }

    private def loadJobs() {
        def jobRoot = new File(configRoot, "jobs")
        sql.sql.dataSet("jobs").rows().each {
            def jobCfg = new File(jobRoot, "${it['name']}.json")

            def job = new Job(this, new File(jobRoot, "${it['name']}.json"))
            jobs[job.name] = job
            job.id = it['id'] as int
            job.forceStatus(JobStatus.parse(it['status'] as int))
        }

        jobRoot.eachFile {
            if (it.isDirectory() || !it.name.endsWith(".json")) return

            def job = new Job(this, it)

            if (!jobs.containsKey(job.name)) { // This Job Config isn't in the Database yet.
                def r = sql.sql.executeInsert("INSERT INTO `jobs` (`id`, `name`, `status`) VALUES (NULL, ${job.name}, '1');")
                job.status = JobStatus.NOT_STARTED
                println r
                job.id = r[0][0] as int
            }
        }

        println "Loaded ${jobs.size()} jobs."
    }

    void runJob(Job job) {
        Thread.start("Builder[${job.name}]") {

            eventBus.publish("ci/job-running", [
                    jobName: job.name
            ])

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

            def timer = new com.directmyfile.ci.helper.Timer()

            timer.start()

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

            def buildTime = timer.stop()

            if (!success) {
                println 'Job has Failed'
                job.status = JobStatus.FAILURE
            } else {
                println 'Job has Completed'
                job.status = JobStatus.SUCCESS
            }

            eventBus.publish("ci/job-done", [
                    jobName: job.name,
                    status: job.status,
                    buildTime: buildTime,
                    timeString: timer.toString()
            ])
        }
    }

    def getArtifactDir() {
        def dir = new File(configRoot, "artifacts")
        dir.mkdir()
        return dir
    }
}
