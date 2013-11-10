package com.directmyfile.ci.scm

import com.directmyfile.ci.core.CI
import com.directmyfile.ci.Utils
import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.exception.ToolException
import com.directmyfile.ci.jobs.Job

class GitSCM extends SCM {

    private Map gitConfig

    GitSCM(CI ci) {
        this.gitConfig = ci.config.getProperty("git", [
                logLength: 4
        ]) as Map
    }

    @Override
    void clone(Job job) {
        def cmd = [findGit().absolutePath, "clone", "--recursive", job.getSCM().url, job.buildDir.absolutePath]
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
        log.println()
        log.flush()
        log.close()
        def exitCode = proc.waitFor()
        if (exitCode != 0) throw new ToolException("Git failed to clone repository!")
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
        if (exitCode != 0) throw new ToolException("Git failed to pull changes!")
    }

    @Override
    boolean exists(Job job) {
        def gitDir = new File(job.buildDir, ".git")

        return gitDir.exists()
    }

    @Override
    Changelog changelog(Job job) {
        def changelog = new Changelog()

        def dir = job.buildDir
        def builder = new ProcessBuilder()

        builder.directory(dir)
        builder.command([findGit().absolutePath, "log", "-${gitConfig['logLimit'].toString()}".toString(), "--pretty='format:%H%n%an%n%s'"])
        // Makes Git Log Lines into Groovy List

        def proc = builder.start()

        proc.waitFor()

        def log = proc.text.readLines()

        def current = changelog.newEntry()
        def type = 1
        for (entry in log) {
            switch (type) {
                case 1:
                    type++
                    current.revision = entry
                    break
                case 2:
                    type++
                    current.author = entry
                    break
                case 3:
                    type = 1
                    current.message = entry
                    current = changelog.newEntry()
                    break
            }
        }

        return changelog
    }

    private static File findGit() {
        def gitCommand = Utils.findCommandOnPath("git")
        if (gitCommand == null) {
            throw new ToolException("Could not find Git on System!")
        }
        return gitCommand
    }
}
