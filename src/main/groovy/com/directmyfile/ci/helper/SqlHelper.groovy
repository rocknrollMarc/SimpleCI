package com.directmyfile.ci.helper

import com.directmyfile.ci.CI
import groovy.sql.Sql

class SqlHelper {
    private CI ci
    private Sql sql
    private Map config

    SqlHelper(CI ci) {
        this.ci = ci
    }

    void init() {
        def url = "jdbc:mysql://${config['host']}:${config['port']}/${config['database']}"
        this.sql = Sql.newInstance(url, config['username'] as String, config['password'] as String)

        println("Connected to Database")
    }

    void setConfig(Map config) {
        this.config = config
    }

    Sql getSql() {
        if (sql.connection.closed) init()
        return sql
    }
}
