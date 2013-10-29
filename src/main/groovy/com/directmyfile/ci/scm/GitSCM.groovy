package com.directmyfile.ci.scm

import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.Utils
import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.exception.ToolException

class GitSCM extends SCM {
    @Override
    void clone(Job job) {
        def cmd = [findGit().absolutePath, "clone", job.getSCM().url, job.buildDir.absolutePath]
        def builder = new ProcessBuilder()
        builder.command(cmd)
        builder.directory(job.buildDir)
        builder.redirectErrorStream(true)
        builder.redirectOutput(job.logFile)
        def proc = builder.start()
        def exitCode = proc.waitFor()
        if (exitCode!=0) throw new ToolException("Git failed to clone repository!")
    }

    @Override
    void update(Job job) {
        def cmd = [findGit().absolutePath, "pull", "--all"]
        def builder = new ProcessBuilder()
        builder.command(cmd)
        builder.directory(job.buildDir)
        builder.redirectErrorStream(true)
        def proc = builder.start()
        def log = job.logFile.newPrintWriter()
        proc.inputStream.eachLine {
            log.println(it)
            log.flush()
        }
        def exitCode = proc.waitFor()
        log.println()
        log.flush()
        log.close()
        if (exitCode!=0) throw new ToolException("Git failed to pull changes!")
    }

    @Override
    boolean exists(Job job) {
        def gitDir = new File(job.buildDir, ".git")

        return gitDir.exists()
    }

    private static File findGit() {
        def gitCommand = Utils.findCommandOnPath("git")
        if (gitCommand==null) {
            throw new ToolException("Could not find Git on System!")
        }
        return gitCommand
    }
}
