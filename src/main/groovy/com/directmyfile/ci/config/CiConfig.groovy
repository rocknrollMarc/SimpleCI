package com.directmyfile.ci.config

import com.directmyfile.ci.core.CI

class CiConfig extends GConfig {
    private CI ci

    CiConfig(CI ci) {
        super(new File(ci.configRoot, "config.groovy"))
        this.ci = ci

        setDefaultConfig(this.class.classLoader.getResourceAsStream("defaultConfig.groovy").text)
    }

    @Override
    void load() {
        super.load()
        def web = getProperty("web", [
                port: 8080
        ])

        ci.port = web['port'] as int

        ci.sql.setConfig(getProperty("sql", [
                host: "localhost",
                port: "3306",
                username: "root",
                password: "changeme",
                database: "ci"
        ]) as Map)
    }

    Map ciSection() {
        return getProperty("ci", [
                queueSize: 2
        ]) as Map
    }
}
