package com.directmyfile.ci

import com.directmyfile.ci.exception.JobConfigurationException
import groovy.json.JsonSlurper

class BuildConfig {

    private def json

    BuildConfig(File file) {

        if (!file.exists()) {
            throw new JobConfigurationException("No Such Job Configuration File: ${file.absolutePath}")
        }

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

    def getNotify() {
        return json['notify'] ?: [:]
    }
}
