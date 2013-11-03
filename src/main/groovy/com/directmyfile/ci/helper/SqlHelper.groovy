package com.directmyfile.ci.helper

import com.directmyfile.ci.Utils
import com.directmyfile.ci.core.CI
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

        executeSQL(Utils.resource("sql/init.sql"))

        ci.logger.info("Connected to Database")
    }

    void setConfig(Map config) {
        this.config = config
    }

    Sql getSql() {
        if (sql.connection.closed) init()
        return sql
    }

    boolean executeSQL(String fullQuery) {
        fullQuery.tokenize(";").each {
            if (!sql.execute(it)) return false
        }
        return true
    }

    boolean executeSQL(InputStream stream) {
        return executeSQL(stream.text)
    }

    boolean executeSQL(File file) {
        return executeSQL(file.text)
    }
}
