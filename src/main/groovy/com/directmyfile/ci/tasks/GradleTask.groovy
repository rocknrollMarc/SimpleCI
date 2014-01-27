package com.directmyfile.ci.tasks

import com.directmyfile.ci.api.Task
import com.directmyfile.ci.core.CI
import com.directmyfile.ci.exception.JobConfigurationException
import com.directmyfile.ci.exception.ToolException
import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.utils.Utils

class GradleTask extends Task {
    @Override
    boolean execute(Object params) {
        def config = params as Map

        def ci = config['ci'] as CI

        def job = config['job'] as Job

        def wrapper = config['wrapper'] as boolean

        def opts = config.get("opts", []) as List<String>

        def tasks = config.get("tasks", []) as List<String>

        def gradleCommand = "gradle"

        if (wrapper) {
            if (!new File(job.buildDir, "gradlew").exists())
                throw new JobConfigurationException("Gradle Wrapper not found in Job: ${job.name}")
            gradleCommand = "sh gradlew"
        } else {
            def c = Utils.findCommandOnPath("gradle")
            if (c == null) throw new ToolException("Gradle not found on this system.")
        }

        return new CommandTask().execute([
                ci: ci,
                job: job,
                command: "${gradleCommand} ${opts.join(' ')} ${tasks.join(' ')}"
        ])
    }
}
