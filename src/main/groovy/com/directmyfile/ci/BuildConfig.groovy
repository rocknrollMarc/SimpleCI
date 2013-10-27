package com.directmyfile.ci

import groovy.json.JsonSlurper

class BuildConfig {

    private def json

    BuildConfig(File file) {
        this.json = new JsonSlurper().parse(file.newReader())
    }

    String getName() {
        return json['name']
    }

    Object[] getTasks() {
        return json['tasks']
    }

    def getSCM() {
        return json['scm']
    }

    def getArtifacts() {
        return json['artifacts'] as List<String>
    }
}
