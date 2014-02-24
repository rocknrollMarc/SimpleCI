package com.directmyfile.ci.config

import com.directmyfile.ci.exception.JobConfigurationException
import groovy.json.JsonSlurper

class BuildConfig {

    private final Object json
    File file

    BuildConfig(File file) {

        this.file = file

        if (!file.exists()) {
            throw new JobConfigurationException("No Such Job Configuration File: ${file.absolutePath}")
        }

        this.json = new JsonSlurper().parse(file.newReader())
    }

    String getName() {
        return json['name']
    }

    List<Object> getTasks() {
        return json['tasks'] as List<Object>
    }

    def getSCM() {
        return json['scm'] as Map<String, Object>
    }

    def getArtifacts() {
        return json['artifacts'] as List<String>
    }

    def getNotify() {
        return json['notify'] ?: [:]
    }

    def getRequirements() {
        return json['requirements'] ?: [:]
    }
}
