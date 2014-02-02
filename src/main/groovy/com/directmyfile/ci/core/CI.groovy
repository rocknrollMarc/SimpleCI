package com.directmyfile.ci.core

import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.api.Task
import com.directmyfile.ci.config.CiConfig
import com.directmyfile.ci.exception.CIException
import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.jobs.JobStatus
import com.directmyfile.ci.notify.IRCBot
import com.directmyfile.ci.plugins.PluginManager
import com.directmyfile.ci.scm.GitSCM
import com.directmyfile.ci.security.CISecurity
import com.directmyfile.ci.tasks.CommandTask
import com.directmyfile.ci.tasks.GradleTask
import com.directmyfile.ci.tasks.MakeTask
import com.directmyfile.ci.utils.db.SqlHelper
import com.directmyfile.ci.utils.logging.LogLevel
import com.directmyfile.ci.utils.logging.Logger
import com.directmyfile.ci.web.VertxManager

import java.util.concurrent.LinkedBlockingQueue
import java.util.logging.Level as JavaLogLevel
import java.util.logging.Logger as JavaLogger

class CI {

    /**
     * Main CI Logger
     */
    static final Logger logger = Logger.getLogger("CI")

    /**
     * Configuration Root
     */
    def configRoot = new File(".")

    /**
     * CI Server Web Port
     */
    int port = 0

    /**
     * CI Server Web Host
     */
    String host = "0.0.0.0"

    /**
     * Plugin Manager
     */
    def pluginManager = new PluginManager(this)

    /**
     * CI Configuration
     */
    def config = new CiConfig(this)

    /**
     * SQL Functionality Provider
     */
    def sql = new SqlHelper(this)

    /**
     * CI IRC Bot
     */
    def ircBot = new IRCBot()

    /**
     * CI Security
     */
    def security = new CISecurity(this)

    /**
     * CI Task Types
     */
    Map<String, Task> taskTypes = [
            command: new CommandTask(),
            gradle: new GradleTask(),
            make: new MakeTask()
    ]

    /**
     * Source Code Manager Types
     */
    Map<String, SCM> scmTypes = [:]

    /**
     * CI Jobs
     */
    Map<String, Job> jobs = [:]

    /**
     * Job Queue System
     */
    LinkedBlockingQueue<Job> jobQueue

    /**
     * Vert.x Manager for managing Vert.x related systems
     */
    def vertxManager = new VertxManager(this)

    /**
     * CI Event Bus
     */
    def eventBus = new EventBus()

    /**
     * Starts CI Server
     */
    void start() {
        init()
        loadJobs()
        vertxManager.setupWebServer()
    }

    /**
     * Starts the IRC Bot
     * <p><b>NOTICE:</b> Must be run on Main Thread</p>
     */
    void startBot() {
        ircBot.start(this)
    }

    /**
     * Initializes this CI Server
     */
    private void init() {
        config.load()
        JavaLogger.getLogger("groovy.sql.Sql").setLevel(JavaLogLevel.OFF)

        def logLevel = LogLevel.parse(config.loggingSection().level.toString().toUpperCase())
        logger.setLevel(logLevel)

        jobQueue = new LinkedBlockingQueue<Job>(config.ciSection()['queueSize'] as int)
        sql.init()
        logger.info "Connected to Database"
        new File(configRoot, 'logs').mkdirs()
        pluginManager.loadPlugins()

        eventBus.dispatch(name: "ci/init", time: System.currentTimeMillis())

        scmTypes['git'] = new GitSCM(this)
    }

    /**
     * Loads Jobs from Database and Job Files
     */
    void loadJobs() {
        def jobRoot = new File(configRoot, "jobs")

        if (!jobRoot.exists()) {
            jobRoot.mkdir()
        }

        sql.dataSet("jobs").rows().each {
            def jobCfg = new File(jobRoot, "${it['name']}.json")

            if (!jobCfg.exists()) {
                logger.info "Job Configuration File '${jobCfg.name}' does not exist. Skipping."
                return
            }

            def job = new Job(this, jobCfg)
            jobs[job.name] = job
            job.id = it['id'] as int
            job.forceStatus(JobStatus.parse(it['status'] as int))
        }

        jobRoot.eachFile {
            if (it.isDirectory() || !it.name.endsWith(".json")) {
                return
            }

            def job = new Job(this, it)

            if (!jobs.containsKey(job.name)) { // This Job Config isn't in the Database yet.
                def r = sql.insert("INSERT INTO `jobs` (`id`, `name`, `status`, `lastRevision`) VALUES (NULL, '${job.name}', '${JobStatus.NOT_STARTED.ordinal()}', '');")
                job.status = JobStatus.NOT_STARTED
                job.id = r[0][0] as int
                jobs[job.name] = job
            }
        }

        logger.info "Loaded ${jobs.size()} jobs."
    }

    /**
     * Adds the Specified Job to the Queue
     * @param job Job to Add to Queue
     */
    void runJob(Job job) {
        Thread.start("Builder[${job.name}]") { ->
            def number = (job.history.latestBuild?.number ?: 0) + 1
            def lastStatus = number == 1 ? JobStatus.NOT_STARTED : job.status
            job.status = JobStatus.WAITING

            logger.debug "Job '${job.name}' has been queued"



            jobQueue.put(job)

            def checkJobInQueue = {
                return jobQueue.count {
                    it.name == job.name
                } != 1
            }
            while (checkJobInQueue()) {
                switch (job.status) {
                    case JobStatus.SUCCESS || JobStatus.FAILURE: break
                }
            }

            // Update Number
            number = (job.history.latestBuild?.number ?: 0) + 1

            eventBus.dispatch(name: "ci/job-running", jobName: job.name, lastStatus: lastStatus, number: number)

            def timer = new com.directmyfile.ci.utils.Timer()

            timer.start()

            def success = true
            def scmShouldRun = true
            def tasksShouldRun = true

            job.status = JobStatus.RUNNING
            logger.info "Job '${job.name}' is Running"

            if (scmShouldRun) {
                def scmConfig = job.getSCM()

                if (!scmTypes.containsKey(scmConfig.type)) {
                    logger.error "Job '${job.name}' is attempting to use a non-existant SCM Type '${scmConfig.type}!'"
                    success = false
                    tasksShouldRun = false
                }

                def scm = scmTypes[scmConfig.type]

                try {
                    if (scm.exists(job)) {
                        scm.update(job)
                    } else {
                        scm.clone(job)
                    }
                } catch (CIException e) {
                    logger.info "Job '${job.name}' (SCM): ${e.message}"
                    tasksShouldRun = false
                    success = false
                }
            }

            if (tasksShouldRun) {
                def tasks = job.tasks

                for (taskConfig in tasks) {
                    def id = tasks.indexOf(taskConfig) + 1
                    logger.info "Running Task ${id} of ${job.tasks.size()} for Job '${job.name}'"

                    try {
                        def taskSuccess = taskConfig.getTask().execute(taskConfig.params)

                        if (!taskSuccess) {
                            success = false
                            break
                        }
                    } catch (CIException e) {
                        logger.info "Job '${job.name}' (Task #${tasks.indexOf(taskConfig) + 1}): ${e.message}"
                    }
                }
                def artifacts = new File(artifactDir, "${job.name}/${number}")
                artifacts.mkdirs()
                job.artifactLocations.each {
                    def file = new File(job.buildDir, it)
                    if (!file.exists()) {
                        job.logFile.append("\nArtifact File: ${file.canonicalPath} does not exist")
                        logger.debug "Job '${job.name}' has non existent artifact: ${file.canonicalPath}"
                        return
                    }
                    new File(artifacts, file.name).bytes = file.bytes
                }
            }

            def buildTime = timer.stop()

            logger.debug "Job '${job.name}' completed in ${buildTime} milliseconds"

            if (!success) {
                logger.info "Job '${job.name}' has Failed"
                job.status = JobStatus.FAILURE
            } else {
                logger.info "Job '${job.name}' has Completed"
                job.status = JobStatus.SUCCESS
            }

            eventBus.dispatch(name: "ci/job-done", jobName: job.name, status: job.status, buildTime: buildTime, timeString: timer.toString(), number: number)

            def log = job.logFile.text

            def base64Log = log.bytes.encodeBase64().writeTo(new StringWriter()).toString()

            sql.insert("INSERT INTO `job_history` (`id`, `job_id`, `status`, `log`, `logged`, `number`) VALUES (NULL, ${job.id}, ${job.status.ordinal()}, '${base64Log}', CURRENT_TIMESTAMP, ${number});")
            jobQueue.remove(job)
            logger.debug "Job '${job.name}' removed from queue"
        }
    }

    /**
     * Updates all Jobs from the Database and parses Job Files
     */
    def updateJobs() {
        jobs.values()*.reload()
    }

    /**
     * Gets where artifacts are stored
     * @return Artifact Directory
     */
    def getArtifactDir() {
        def dir = new File(configRoot, "artifacts")
        dir.mkdir()
        return dir
    }
}
