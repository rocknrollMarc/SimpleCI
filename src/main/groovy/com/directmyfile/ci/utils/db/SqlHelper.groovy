package com.directmyfile.ci.utils.db

import com.directmyfile.ci.utils.Utils
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
        sql.cacheStatements = true
        ci.logger.debug "Running Initialization Queries"
        ci.logger.debug "Groovy Sql instance created and Connection Established"
        execute(Utils.resource("sql/init.sql"))
        ci.logger.debug "Initialization Queries Complete"
    }

    void setConfig(Map config) {
        this.config = config
    }

    def getSql() {
        if (sql.connection.closed) {
            init()
        }
        return sql
    }

    def execute(String fullQuery) {
        def success = true
        fullQuery.tokenize(";").each {
            def query = it.replaceAll("\n", "").trim()
            ci.logger.debug "Executing SQL: ${query}"
            if (!getSql().execute(query)) {
                success = false
            }
        }
        return success
    }

    def rows(String query) {
        ci.logger.debug "Executing SQL: ${query}"
        return getSql().rows(query)
    }

    def eachRow(String query, Closure closure) {
        getSql().eachRow(query, closure)
    }

    def dataSet(String table) {
        return getSql().dataSet(table)
    }

    def firstRow(String query) {
        return getSql().firstRow(query)
    }

    def query(String query, Closure closure) {
        getSql().query(query, closure)
    }

    def insert(String query) {
        return getSql().executeInsert(query)
    }

    def update(String query) {
        return getSql().executeUpdate(query)
    }

    boolean execute(InputStream stream) {
        return execute(stream.text)
    }

    boolean execute(File file) {
        return execute(file.text)
    }
}
