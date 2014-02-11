package com.directmyfile.ci.tasks

import com.directmyfile.ci.api.Task
import com.directmyfile.ci.scm.GitSCM

class GitTask extends Task {
    @Override
    boolean execute(Object params) {
        return new CommandTask().execute([
                ci     : params['ci'],
                job    : params['job'],
                command: "${GitSCM.findGit().absolutePath} ${params["pre-args"]} ${params["command"]} ${params["arguments"]}"
        ])
    }
}
