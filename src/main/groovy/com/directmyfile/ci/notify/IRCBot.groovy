package com.directmyfile.ci.notify

import com.directmyfile.ci.CI
import com.directmyfile.ci.jobs.JobStatus
import org.nanobot.Colors
import org.nanobot.NanoBot
import org.vertx.groovy.core.eventbus.Message

class IRCBot {

    CI ci

    NanoBot bot

    void start(CI ci) {
        this.ci = ci

        def ciConfig = ci.config

        def cfg = ciConfig.getProperty("ircConfig", [
                enabled: false,
                host: "irc.esper.net",
                port: 6667,
                nickname: "SimpleCI",
                username: "SimpleCI",
                channels: [
                        "#DirectMyFile"
                ]
        ])

        if (cfg['enabled']) {
            println "Loading IRC Bot"
        } else {
            return
        }

        this.bot = new NanoBot()

        bot.setServer(cfg['host'])
        bot.setPort(cfg['port'])
        bot.setNickname(cfg['nickname'])
        bot.setUserName(cfg['username'])
        bot.enableCommandEvent()

        def channels = cfg['channels']

        bot.on('ready') {
            channels.each {
                join(it)
            }
        }

        ci.eventBus.registerHandler("ci/job-running") { Message msg ->
            def e = msg.body() as Map
            def jobName = e.jobName as String

            channels.each {
                bot.msg(it, "> CI is building: ${jobName}")
            }
        }

        ci.eventBus.registerHandler("ci/job-done") { Message msg ->
            def e = msg.body() as Map
            def jobName = e.jobName as String
            def status = e.status as JobStatus
            def time = e.timeString as String

            channels.each {
                bot.msg(it, "> Job '${jobName}' completed with status '${status.IRCColor}${status}${Colors.NORMAL}' taking ${time}")
            }
        }

        bot.on('command') {
            def channel = it['channel'] as String
            def cmd = it['command'] as String
            def args = it['args'] as String[]

            if (cmd == 'listJobs') {
                msg(channel, "> ${ci.jobs.keySet().join(', ')}")
            } else if (cmd == 'build') {
                if (args.length != 1) {
                    msg(channel, '> Usage: !build JOB')
                    return
                }
                def jobName = args[0]
                def job = ci.jobs[jobName]
                if (job == null) {
                    msg(channel, "> No Such Job: ${jobName}")
                    return
                }
                ci.runJob(job)
            }
        }

        bot.connect()
    }
}
