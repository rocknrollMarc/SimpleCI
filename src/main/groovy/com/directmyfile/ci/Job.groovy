package com.directmyfile.ci

class Job {
    BuildConfig buildConfig
    CI ci

    JobStatus status = null

    Job(CI ci, File file) {
        this.ci = ci
        this.buildConfig = new BuildConfig(file)
        this.status = {
            if(logFile.exists()) {
                return JobStatus.SUCCESS
            } else {
                return JobStatus.NOT_STARTED
            }
        }()
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
        def artifactDir = new File(ci.artifactDir, name)
        if (!artifactDir.exists()) return ""
        artifactDir.eachFile {
            text.add("<tr><td><a href=\"/artifact/${this.name}/${it.name}\">${it.name}</a></tr></td>")
        }
        return text.join('\n')
    }
}
