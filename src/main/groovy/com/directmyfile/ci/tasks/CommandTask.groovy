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
        def proc = command.execute([], job.buildDir)
        def exitCode = proc.waitFor()
        // Write Logs
        job.logFile.write(proc.inputStream.readLines().join('\n'))

        job.logFile.append("\nProcess Exited with Status ${exitCode}")

        return exitCode==0
    }
}
