package com.directmyfile.ci.tasks

import com.directmyfile.ci.api.Task
import com.directmyfile.ci.core.CI
import com.directmyfile.ci.jobs.Job

class MakeTask extends Task {

    @Override
    boolean execute(Object params) {
        def config = params as Map

        def ci = config['ci'] as CI

        def job = config['job'] as Job

        def targets = config['targets'] as List<String>

        return new CommandTask().execute([
                ci     : ci,
                job    : job,
                command: "make ${targets.join(' ')}"
        ])
    }
}
