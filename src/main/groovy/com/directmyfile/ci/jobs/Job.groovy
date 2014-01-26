package com.directmyfile.ci.jobs

import com.directmyfile.ci.config.BuildConfig
import com.directmyfile.ci.config.SCMConfig
import com.directmyfile.ci.config.TaskConfig
import com.directmyfile.ci.core.CI
import com.directmyfile.ci.scm.Changelog

class Job {
    BuildConfig buildConfig
    CI ci

    private JobStatus status

    int id

    Job(CI ci, File file) {
        this.ci = ci
        this.buildConfig = new BuildConfig(file)
        getBuildDir().mkdirs()
    }

    def getName() {
        return buildConfig.name
    }

    def getTasks() {
        def taskConfig = buildConfig.getTasks()

        List<TaskConfig> tasks = []

        taskConfig.each {
            def type = it['type'] as String
            if (!ci.taskTypes.containsKey(type)) {
                throw new RuntimeException("Invalid Task Type: $type")
            }

            it['ci'] = ci
            it['job'] = this

            tasks.add(new TaskConfig(ci.taskTypes.get(type), it))
        }

        return tasks
    }

    def getBuildDir() {
        return new File(ci.configRoot, "build/${name}")
    }

    def getSCM() {
        return new SCMConfig(buildConfig.SCM['type'] as String, buildConfig.SCM['url'] as String)
    }

    def getArtifactLocations() {
        return buildConfig.artifacts
    }

    def getLogFile() {
        return new File(ci.configRoot, "logs/${name}.log")
    }

    def generateArtifactList() {
        def text = []
        if (history.latestBuild == null) return "No builds yet."
        def number = history.latestBuild.number
        def artifactDir = new File(ci.artifactDir, "${name}/${number}")
        if (!artifactDir.exists()) {
            return ""
        }
        artifactDir.eachFile {
            text.add("<tr><td><a href=\"/artifact/${this.name}/${number}/${it.name}\">${it.name}</a></tr></td>")
        }
        return text.join('\n')
    }

    void setStatus(JobStatus status) {
        this.status = status
        ci.sql.update("UPDATE `jobs` SET  `status` =  '${status.ordinal()}' WHERE  `jobs`.`id` = ${id};")
    }

    JobStatus getStatus() {
        return status
    }

    void reload() {
        this.buildConfig = new BuildConfig(buildConfig.file)

        this.status = JobStatus.parse(ci.sql.firstRow("SELECT `status` FROM `jobs` WHERE `id` = ${id};").status as int)
    }

    void forceStatus(JobStatus status) {
        this.status = status
    }

    Changelog getChangelog() {
        return ci.scmTypes[SCM.type].changelog(this)
    }

    def getHistory() {
        def history = new JobHistory(this)
        history.load()
        return history
    }

    def getNotifications() {
        return buildConfig.notify
    }
}
