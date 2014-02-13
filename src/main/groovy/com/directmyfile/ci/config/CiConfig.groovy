package com.directmyfile.ci.config

import com.directmyfile.ci.core.CI

class CiConfig extends GConfig {
    private final CI ci

    CiConfig(CI ci) {
        super(new File(ci.configRoot, "config.groovy"))
        this.ci = ci

        defaultConfig = this.class.classLoader.getResourceAsStream("defaultConfig.groovy").text
    }

    @Override
    void load() {
        super.load()

        def web = getProperty("web", [
                host: "0.0.0.0",
                port: 8080
        ])

        ci.host = web['host'] as String
        ci.port = web['port'] as int

        ci.sql.config = getProperty("sql", [
                host    : "localhost",
                port    : "3306",
                username: "root",
                password: "changeme",
                database: "ci"
        ]) as Map
    }

    Map ciSection() {
        return getProperty("ci", [
                queueSize: 2
        ]) as Map
    }

    Map loggingSection() {
        return getProperty("logging", [
                level: "INFO"
        ]) as Map
    }

    Map securitySection() {
        return getProperty("security", [
                enabled: false
        ]) as Map
    }
}
