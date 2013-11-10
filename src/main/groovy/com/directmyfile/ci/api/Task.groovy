package com.directmyfile.ci.api

/**
 * A build Task
 */
abstract class Task {
    protected String type

    /**
     * Executes this Task
     * @param params The JSON object of this task - Includes two more types: job, and ci
     * @return
     */
    abstract boolean execute(Object params);
}
