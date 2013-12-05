package com.directmyfile.ci.core

/**
 * The Groovy Event Bus.
 */
class EventBus {
    private Map<String, List<Closure>> handlers = [:]

    void on(String name, Closure handler) {
        if (!handlers.containsKey(name)) {handlers[name] = []}
        handlers[name].add(handler)
    }

    void dispatch(Map<String, Object> data, boolean useThread) {
        if (!data.containsKey('name'))
            throw new IllegalArgumentException("Dispatching event requires the 'name' parameter to specify event name.")

        if (!handlers.containsKey(data['name']))
            return

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

    void dispatch(Map<String, Object> data) {
        dispatch(data, true)
    }

    Map<String, List<Closure>> handlers() {
        return handlers
    }

    List<Closure> handlers(String name) {
        return handlers().get(name)
    }
}