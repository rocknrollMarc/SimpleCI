package com.directmyfile.ci.jobs

import groovy.sql.GroovyRowResult

import java.sql.Timestamp

class JobHistory {
    private List<Entry> entries = []
    private Job job

    JobHistory(Job job) {
        this.job = job
    }

    void load() {
        job.ci.sql.getSql().eachRow("SELECT * FROM `job_history` WHERE `job_id` = ${job.id}") {
            def entry = new Entry()
            entries.add(entry)
            def result = it.toRowResult() as GroovyRowResult
            entry.id = result['id'] as int
            entry.jobID = result['job_id'] as int
            entry.log = result['log'] as String
            entry.logTime = result['logged'] as Timestamp
            entry.status = result['status'] as int
            entry.number = result['number'] as int
        }
    }

    def getEntries() {
        return entries
    }

    def getLatestBuild() {
        return entries.isEmpty() ? null : entries.last()
    }

    def toHTML() {
        def html = ""
        entries.each {
            html += "<br/>${it.id}"
        }
        return html
    }

    static class Entry {
        int id, jobID, status, number
        String log
        Timestamp logTime
    }
}
