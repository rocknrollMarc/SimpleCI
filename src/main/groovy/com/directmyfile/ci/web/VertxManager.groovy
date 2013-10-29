package com.directmyfile.ci.web
import com.directmyfile.ci.CI
import org.vertx.groovy.core.Vertx
import org.vertx.groovy.core.eventbus.EventBus

class VertxManager {

    def vertx = newVertx()
    CI ci

    WebServer webServer

    VertxManager(CI ci) {
        this.ci = ci
    }

    void setupWebServer() {
        this.webServer = new WebServer(ci)
        webServer.start(ci.port)
    }

    void stopWebServer() {
        webServer.server.close()
    }

    EventBus getEventBus() {
        return vertx.eventBus
    }

    static Vertx newVertx() {
        return Vertx.newVertx()
    }
}
