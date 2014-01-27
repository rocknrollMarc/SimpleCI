package com.directmyfile.ci.scm

import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.core.CI
import com.directmyfile.ci.exception.ToolException
import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.utils.Utils

class GitSCM extends SCM {

    private Map gitConfig

    GitSCM(CI ci) {
        this.gitConfig = ci.config.getProperty("git", [
                logLength: 4
        ]) as Map
    }

    @Override
    void clone(Job job) {

        def cmd = [findGit().absolutePath, "clone", "--recursive", job.getSCM().getUrl(), job.buildDir.absolutePath]

        def proc = execute(job, cmd)
        def log = job.logFile.newPrintWriter()
        proc.inputStream.eachLine {
            log.println(it)
            log.flush()
        }
        log.println()
        log.flush()
        log.close()
        def exitCode = proc.waitFor()
        if (exitCode != 0)
            throw new ToolException("Git failed to clone repository!")
        updateSubmodules(job)
    }

    @Override
    void update(Job job) {
        def cmd = [findGit().absolutePath, "pull", "--all"]

        updateSubmodules(job)

        def proc = execute(job, cmd)

        def log = job.logFile.newPrintWriter()
        proc.inputStream.eachLine {
            log.println(it)
            log.flush()
        }
        def exitCode = proc.waitFor()
        log.println()
        log.flush()
        log.close()
        if (exitCode != 0)
            throw new ToolException("Git failed to pull changes!")
    }

    @Override
    boolean exists(Job job) {
        def gitDir = new File(job.buildDir, ".git")

        return gitDir.exists()
    }

    @Override
    Changelog changelog(Job job) {
        def changelog = new Changelog()

        def proc = execute(job, [findGit().absolutePath, "log", "-${gitConfig['logLength'].toString()}".toString(), "--pretty=%H%n%an%n%s"])

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

    public static File findGit() {
        def gitCommand = Utils.findCommandOnPath("git")
        if (gitCommand == null) {
            throw new ToolException("Could not find Git on System!")
        }
        return gitCommand
    }

    private static boolean detectSubmodules(File dir) {
        return "${findGit().absolutePath} submodule status".execute([], dir) != ""
    }

    static boolean updateSubmodules(Job job) {
        return execute(job, [findGit().absolutePath, "submodule", "update", "--init", "--recursive"]).waitFor() == 0
    }

    private static Process execute(Job job, List<String> command) {
        def builder = new ProcessBuilder(command)
        builder.directory(job.buildDir)
        builder.redirectErrorStream(true)
        return builder.start()
    }
}
