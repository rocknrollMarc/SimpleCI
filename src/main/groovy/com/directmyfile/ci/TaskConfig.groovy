package com.directmyfile.ci

class TaskConfig {
    Task task
    Object params

    TaskConfig(Task task, Object params) {
        this.task = task
        this.params = params
    }
}
