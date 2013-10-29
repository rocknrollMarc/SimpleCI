package com.directmyfile.ci.api

import com.directmyfile.ci.jobs.Job

abstract class SCM {
    abstract void clone(Job job);

    abstract void update(Job job);

    abstract boolean exists(Job job);
}
