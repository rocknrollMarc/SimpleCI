package com.directmyfile.ci.scm

import com.directmyfile.ci.api.SCM
import com.directmyfile.ci.jobs.Job

class NoneSCM extends SCM {
    @Override
    void clone(Job job) {
        job.buildDir.mkdir()
    }

    @Override
    void update(Job job) {
        /* Empty */
    }

    @Override
    boolean exists(Job job) {
        return true
    }

    @Override
    Changelog changelog(Job job) {
        return new Changelog()
    }
}
