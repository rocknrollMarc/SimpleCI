package com.directmyfile.ci.tasks

import com.directmyfile.ci.api.Task
import com.directmyfile.ci.scm.GitSCM

class GitTask extends Task {
    @Override
    boolean execute(Object params) {
        def pre_args = params["pre-args"] ?: ""
        def command = params["command"] ?: ""
        def arguments = params["args"] ?: ""
        return new CommandTask().execute([
                ci     : params['ci'],
                job    : params['job'],
                command: "${GitSCM.findGit().absolutePath} ${pre_args} ${command} ${arguments}"
        ])
    }
}
