package com.directmyfile.ci.core

/**
 * The Groovy Event Bus.
 */
class EventBus {
    private Map<String, List<Closure<?>>> handlers = [:]

    /**
     * Hook into an Event
     * @param name Event Name
     * @param handler Event Handler
     */
    void on(String name, Closure handler) {
        if (!handlers.containsKey(name)) {
            handlers[name] = []
        }
        handlers[name].add(handler)
    }

    /**
     * Dispatch an Event
     * @param data Event Data
     * @param useThread Threaded Execution
     */
    void dispatch(Map<String, Object> data, boolean useThread) {
        if (!data.containsKey('name')) {
            throw new IllegalArgumentException("Dispatching event requires the 'name' parameter to specify event name.")
        }

        if (!handlers.containsKey(data['name'])) {
            return
        }

        for (c in handlers[data['name'] as String]) {
            if (useThread) {
                Thread.startDaemon("${data['name']}[Executor]") {
                    c(data)
                }
            } else {
                c(data)
            }
        }
    }

    /**
     * Dispatch an Event - Threaded
     * @param data Event Data
     */
    void dispatch(Map<String, Object> data) {
        dispatch(data, true)
    }

    /**
     * Get all Handlers
     * @return Handlers
     */
    Map<String, List<Closure<?>>> handlers() {
        return handlers
    }

    /**
     * Gets all Handlers for Event
     * @param name Event Name
     * @return Handlers
     */
    List<Closure<?>> handlers(String name) {
        return handlers().get(name)
    }
}