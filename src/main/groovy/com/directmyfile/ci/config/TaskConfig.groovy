package com.directmyfile.ci.config

import com.directmyfile.ci.api.Task

class TaskConfig {
    Task task
    Object params

    TaskConfig(Task task, Object params) {
        this.task = task
        this.params = params
    }
}
