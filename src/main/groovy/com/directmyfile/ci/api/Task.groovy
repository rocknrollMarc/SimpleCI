package com.directmyfile.ci.api

abstract class Task {
    protected String type

    abstract boolean execute(Object params);
}
