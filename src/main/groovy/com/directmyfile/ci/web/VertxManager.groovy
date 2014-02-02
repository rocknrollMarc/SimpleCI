package com.directmyfile.ci.web

import com.directmyfile.ci.core.CI
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.EventBus

/**
 * Manages Vert.x Instances
 */
class VertxManager {

    def vertx = Vertx.newVertx()
    CI ci

    WebServer webServer

    VertxManager (CI ci) {
        this.ci = ci
    }

    void setupWebServer () {
        this.webServer = new WebServer(ci)
        webServer.start(ci.port, ci.host)
    }

    void stopWebServer () {
        webServer.server.close()
    }

    EventBus getEventBus () {
        return vertx.eventBus
    }

}
