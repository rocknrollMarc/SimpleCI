package com.directmyfile.ci.core

import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.api.Task
import com.directmyfile.ci.config.CiConfig
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
import groovy.util.logging.Log4j
import org.apache.log4j.ConsoleAppender

import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Level
import java.util.logging.Logger

@Log4j('logger')
class CI {

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

    Map<String, SCM> scmTypes = [:]

    LinkedBlockingQueue<Job> jobQueue

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
        def consoleAppender = new ConsoleAppender()
        consoleAppender.activateOptions()
        consoleAppender.layout = new LogLayout()
        Logger.getLogger("groovy.sql.Sql").setLevel(Level.OFF)
        logger.addAppender(consoleAppender)
        jobQueue = new LinkedBlockingQueue<Job>(config.ciSection()['queueSize'] as int)
        sql.init()
        new File(configRoot, 'logs').mkdirs()
        pluginManager.loadPlugins()

        eventBus.publish("ci/init", [
                time: System.currentTimeMillis()
        ])
        scmTypes['git'] = new GitSCM(this)
    }

    void loadJobs() {
        def jobRoot = new File(configRoot, "jobs")
        if (!jobRoot.exists())
            jobRoot.mkdir()

        sql.sql.dataSet("jobs").rows().each {
            def jobCfg = new File(jobRoot, "${it['name']}.json")

            if (!jobCfg.exists()) {
                throw new JobConfigurationException("Job File: ${jobCfg.absolutePath} does not exist!")
            }

            def job = new Job(this, jobCfg)
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

        logger.info "Loaded ${jobs.size()} jobs."
    }

    void runJob(Job job) {
        Thread.start("Builder[${job.name}]") {
            jobQueue.put(job)

            def number = (job.history.latestBuild?.number ?: 0) + 1

            def lastStatus = job.status

            eventBus.publish("ci/job-running", [
                    jobName: job.name,
                    lastStatus: lastStatus,
                    number: number
            ])

            def success = true
            job.status = JobStatus.RUNNING
            logger.info "Job '${job.name}' is Running"

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
                logger.info "Running Task ${id} of ${job.tasks.size()} for Job '${job.name}'"
                def taskSuccess = task.task.execute(task.params)

                if (!taskSuccess) {
                    success = false
                    break
                }
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
                logger.info "Job '${job.name}' has Failed"
                job.status = JobStatus.FAILURE
            } else {
                logger.info "Job '${job.name}' has Completed"
                job.status = JobStatus.SUCCESS
            }

            jobQueue.remove(job)

            eventBus.publish("ci/job-done", [
                    jobName: job.name,
                    status: job.status,
                    buildTime: buildTime,
                    timeString: timer.toString(),
                    number: number
            ])

            sql.executeSQL("INSERT INTO `job_history` (`id`, `job_id`, `status`, `log`, `logged`, `number`) VALUES (NULL, ${job.id}, ${job.status.intValue()}, '${job.logFile.text}', CURRENT_TIMESTAMP, ${number});")
        }
    }

    def getArtifactDir() {
        def dir = new File(configRoot, "artifacts")
        dir.mkdir()
        return dir
    }
}
