package com.directmyfile.ci.jobs

import groovy.sql.GroovyRowResult
import groovy.transform.ToString

import java.sql.Timestamp

class JobHistory {
    private final List<Entry> entries = []
    private final Job job

    JobHistory(Job job) {
        this.job = job
    }

    void load() {
        job.ci.sql.eachRow("SELECT * FROM `job_history` WHERE `job_id` = ${job.id}") {
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
        entries.empty ? null : entries.last()
    }

    @Override
    String toString() {
        entries.join("\n")
    }

    @ToString
    static class Entry {
        int id, jobID, status, number
        String log
        Timestamp logTime
    }
}
