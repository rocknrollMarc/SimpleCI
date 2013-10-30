package com.directmyfile.ci.api

import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.scm.Changelog

abstract class SCM {
    abstract void clone(Job job);

    abstract void update(Job job);

    abstract boolean exists(Job job);

    abstract Changelog changelog(Job job);
}
