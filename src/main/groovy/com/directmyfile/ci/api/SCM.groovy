package com.directmyfile.ci.api

import com.directmyfile.ci.jobs.Job
import com.directmyfile.ci.scm.Changelog

/**
 * A Source Code Manager
 */
abstract class SCM {
    /**
     * Clones a Jobs SCM Repository
     * @param job Job to Clone
     */
    abstract void clone(Job job);

    /**
     * Updates a Jobs SCM Repository
     * @param job Job to Update
     */
    abstract void update(Job job);

    /**
     * Determines whether this SCM is cloned
     * @param job Job to Check
     * @return If it is already Cloned
     */
    abstract boolean exists(Job job);

    /**
     * Makes Changelog from SCM
     * @param job Job to Changelog
     * @return SCM Changelog
     */
    abstract Changelog changelog(Job job);
}
