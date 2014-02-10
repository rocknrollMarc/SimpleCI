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
    void dispatch(data, useThread = false) {
        def name = data['name'] as String
        if (name == null || !handlers.containsKey(name)) {
            return
        }
        def handlers = handlers[name] as List<Closure>
        def execute = { ->
            handlers.each { Closure handler ->
                handler.call(data)
            }
        }
        if (useThread) {
            def thread = Thread.startDaemon("EventExecutor[${name}]", execute)
            thread.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                void uncaughtException(Thread t, Throwable e) {
                    e.printStackTrace()
                }
            }
        } else {
            execute()
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
        return handlers()[name]
    }
}