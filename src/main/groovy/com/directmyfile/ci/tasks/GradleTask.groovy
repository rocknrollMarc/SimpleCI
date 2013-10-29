package com.directmyfile.ci.tasks

import com.directmyfile.ci.CI
import com.directmyfile.ci.api.Task
import com.directmyfile.ci.jobs.Job

class GradleTask extends Task {
    @Override
    boolean execute(Object params) {
        def config = params as Map

        def ci = config['ci'] as CI

        def job = config['job'] as Job

        def opts = config.get("opts", []) as List<String>

        def tasks = config.get("tasks", []) as List<String>

        return new CommandTask().execute([
                ci: ci,
                job: job,
                command: "gradle ${opts.join(' ')} ${tasks.join(' ')}"
        ])
    }
}
