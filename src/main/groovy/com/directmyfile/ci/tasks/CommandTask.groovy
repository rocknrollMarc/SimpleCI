package com.directmyfile.ci.tasks

import com.directmyfile.ci.CI
import com.directmyfile.ci.Job
import com.directmyfile.ci.Task

class CommandTask extends Task {

    CommandTask() {
        type = 'command'
    }

    @Override
    boolean execute(Object params) {
        def config = params as Map

        def ci = config['ci'] as CI

        def job = config['job'] as Job

        if (!config.containsKey("command")) {
            return false
        }

        def command = config['command'] as String
        def procBuild = new ProcessBuilder().command(command.tokenize(' '))
        procBuild.directory(job.buildDir)
        procBuild.redirectErrorStream(true)
        def proc = procBuild.start()
        def log = new PrintStream(job.logFile.newOutputStream())
        proc.in.eachLine {
            log.println(it)
            log.flush()
        }
        log.println("Process Exited with Status ${proc.waitFor()}")
        log.flush()
        log.close()

        return proc.exitValue()==0
    }
}
