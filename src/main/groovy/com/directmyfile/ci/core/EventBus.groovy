package com.directmyfile.ci.core

import com.directmyfile.ci.utils.MultiMap
import jpower.core.Task as PowerTask
import jpower.core.WorkerPool

/**
 * A Groovy Event Bus that uses JPower's Worker Pool
 */
class EventBus {
    private MultiMap<Closure<?>> handlers = new MultiMap<Closure<?>>()
    private WorkerPool workerPool = new WorkerPool(4)

    /**
     * Hook into an Event
     * @param name Event Name
     * @param handler Event Handler
     */
    void on(String name, Closure handler) {
        handlers[name].add(handler)
    }

    /**
     * Dispatch an Event
     * @param data Event Data
     */
    void dispatch(String eventName, Map<String, Object> options = [:]) {
        def eventHandlers = handlers[eventName]
        if (eventHandlers.empty) { // No Event Handlers to call
            return
        }
        workerPool.submit(new PowerTask() {
            @Override
            void execute() {
                eventHandlers.each { Closure handler ->
                    handler(options)
                }
            }
        })
    }

    /**
     * Get all Handlers
     * @return Handlers
     */
    MultiMap<Closure<?>> handlers() {
        return handlers
    }

    /**
     * Gets all Handlers for Event
     * @param name Event Name
     * @return Handlers
     */
    List<Closure<?>> handlers(String name) {
        return handlers()[name]
    }
}