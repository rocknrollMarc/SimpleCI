package com.directmyfile.ci.api

/**
 * A CI build Task
 */
abstract class Task {
    /**
     * Specifies this Task Type Name
     */
    protected String type

    /**
     * Gets the Task Name
     * @return Task Name
     */
    String getName() {
        return type
    }

    /**
     * Sets Task Name
     * @param name Task Name
     */
    protected void setName(String name) {
        this.type = name
    }

    /**
     * Executes this Task
     * @param params The JSON object of this task - Includes two more types: job, and ci
     * @return
     */
    abstract boolean execute(Object params);
}
