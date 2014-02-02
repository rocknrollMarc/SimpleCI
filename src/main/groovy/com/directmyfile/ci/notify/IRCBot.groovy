package com.directmyfile.ci.notify

import com.directmyfile.ci.core.CI
import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.jobs.JobStatus

class IRCBot {
    CI ci
    Object cfg

    void start(CI ci) {
        this.ci = ci
        NativeManager.manager = new NativeManager(this)

        def ciConfig = ci.config
        cfg = ciConfig.getProperty("irc", [
                enabled: false,
                host: "irc.esper.net",
                port: 6667,
                nickname: "SimpleCI",
                username: "SimpleCI",
                channels: [
                        "#DirectMyFile"
                ],
                commandPrefix: "!"
        ])

        if (!cfg['enabled']) {
            return
        }

        ci.logger.info "Loading IRC Bot"
        NativeManager.loadNatives()
        NativeManager.init(cfg['host'] as String, cfg['port'] as short, cfg['nickname'] as String, cfg['username'] as String, cfg['commandPrefix'] as String)

        def channels = cfg['channels'] as List<String>
        def admins = cfg['admins'] as List<String>

        admins.each {
            NativeManager.addAdmin(it)
        }

        ci.eventBus.on("ci/job-running") { Map<String, Object> e ->
            def jobName = e.jobName as String
            def job = ci.jobs[jobName]
            def status = e['lastStatus'] as JobStatus

            getNotifyChannels(job, channels).each { String channel ->
                if (!NativeManager.isInChannel(channel)) {
                    NativeManager.join(channel)
                }
                NativeManager.msg(channel, "> Build #${e['number']} for ${jobName} starting (Last Status: ${status.ircColor}${status}${Colors.NORMAL})")
            }
        }

        ci.eventBus.on("ci/job-done") { Map<String, Object> e ->
            def jobName = e.jobName as String
            def status = e.status as JobStatus
            def time = e.timeString as String
            def job = ci.jobs[jobName]

            getNotifyChannels(job, channels).each { String channel ->
                if (!NativeManager.isInChannel(channel)) {
                    NativeManager.join(channel)
                }
                NativeManager.msg(channel, "> Build #${e['number']} for ${jobName} completed with status ${status.ircColor}${status}${Colors.NORMAL} taking ${time}")
            }
        }
        NativeManager.startLoop()
    }

    static def getNotifyChannels(Job job, List<String> defaults) {
        def irc = job.notifications['irc'] ?: [:]
        return irc['channels'] ?: defaults
    }

    /* JNI called methods */

    void listJobs(String channel) {
        NativeManager.msg(channel, "> ${ci.jobs.keySet().join(', ')}")
    }

    void loadJobs(String channel) {
        NativeManager.msg(channel, "> Reloading Jobs")
        ci.loadJobs()
        NativeManager.msg(channel, "> Jobs Reloaded: CI has ${ci.jobs.size()} jobs")
    }

    void build(String channel, String jobName) {
        if (jobName == null) {
            NativeManager.msg(channel, '> Usage: !build JOB')
            return
        }
        def job = ci.jobs[jobName]
        if (job == null) {
            NativeManager.msg(channel, "> No Such Job: ${jobName}")
            return
        }
        ci.runJob(job)
    }

    void status(String channel, String job) {
        def jobList = []
        if (job == null) {
            ci.jobs.values().each {
                jobList.add("${it.status.ircColor}${it.name}${Colors.NORMAL} (${it.history.latestBuild?.number ?: "Not Started"})")
            }
        } else {
            def it = ci.jobs[job]
            if (it == null) {
                NativeManager.msg(channel, "> No such job: ${job}")
                return
            }
            jobList.add("${it.status.ircColor}${it.name}${Colors.NORMAL} (${it.history.latestBuild?.number ?: "Not Started"})")
        }
        NativeManager.msg(channel, "> ${jobList.join(', ')}")
    }

    void onReady() {
        def channels = cfg['channels'] as List<String>
        channels.each {
            NativeManager.join(it)
        }
        ci.eventBus.dispatch(name: "irc/ready", time: System.currentTimeMillis())
    }
}
