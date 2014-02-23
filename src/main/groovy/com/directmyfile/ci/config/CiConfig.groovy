package com.directmyfile.ci.config

import com.directmyfile.ci.core.CI
import groovy.transform.CompileStatic

@CompileStatic
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
        ]) as Map<String, Object>

        ci.host = web['host'] as String
        ci.port = web['port'] as int

        ci.sql.config = getProperty("sql", [
                host    : "localhost",
                port    : "3306",
                username: "root",
                password: "changeme",
                database: "ci"
        ]) as Map<String, Object>
    }

    Map<String, Object> ciSection() {
        return getProperty("ci", [
                queueSize: 2
        ]) as Map<String, Object>
    }

    Map<String, Object> loggingSection() {
        return getProperty("logging", [
                level: "INFO"
        ]) as Map<String, Object>
    }

    Map<String, Object> securitySection() {
        return getProperty("security", [
                enabled: false
        ]) as Map<String, Object>
    }
}
