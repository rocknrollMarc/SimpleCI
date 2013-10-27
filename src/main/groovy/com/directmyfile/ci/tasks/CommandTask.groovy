package com.directmyfile.ci.tasks

import com.directmyfile.ci.CI
import com.directmyfile.ci.Job
import com.directmyfile.ci.api.Task

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
        def builder = new ProcessBuilder().command(command.tokenize(' '))
        builder.directory(job.buildDir)
        builder.redirectErrorStream(true)
        def proc = builder.start()
        def log = job.logFile.newPrintWriter()
        proc.inputStream.eachLine {
            log.println(it)
            log.flush()
        }
        def exitCode = proc.waitFor()
        log.println("Process Exited with Status ${exitCode}")
        log.flush()
        log.close()

        return exitCode==0
    }
}
