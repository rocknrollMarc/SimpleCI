package com.directmyfile.ci.api

/**
 * A CI build Task
 */
abstract class Task {
    /**
     * Executes this Task
     * @param params The JSON object of this task - Includes two more types: job, and ci
     * @return
     */
    abstract boolean execute(Object params);

    static File file(File parent = new File("."), String name) {
        return new File(parent, name)
    }
}
