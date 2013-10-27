package com.directmyfile.ci.helper

import com.directmyfile.ci.CI
import groovy.sql.Sql

class SqlHelper {
    private String url
    private CI ci
    private Sql sql
    private def auth = [
            username: "",
            password: ""
    ]

    SqlHelper(CI ci) {
        this.ci = ci
    }

    void init() {
        this.sql = Sql.newInstance(url, auth.username, auth.password)
        sql.executeUpdate("CREATE TABLE IF NOT EXISTS jobs;")
    }

    void setURL(String url) {
        this.url = url
    }

    void setAuth(Map<String, String> authInfo) {
        this.auth = authInfo
        println("Password: ${authInfo['password']}")
    }
}
