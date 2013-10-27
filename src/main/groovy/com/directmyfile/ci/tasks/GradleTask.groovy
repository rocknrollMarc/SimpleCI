package com.directmyfile.ci.tasks

import com.directmyfile.ci.CI
import com.directmyfile.ci.Job
import com.directmyfile.ci.Task

class GradleTask extends Task {
    @Override
    boolean execute(Object params) {
        def config = params as Map

        def ci = config['ci'] as CI

        def job = config['job'] as Job

        def tasks = config['tasks'] as List<String>

        return new CommandTask().execute([
                ci: ci,
                job: job,
                command: "gradle ${tasks.join(' ')}"
        ])
    }
}
