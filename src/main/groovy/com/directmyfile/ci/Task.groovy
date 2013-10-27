package com.directmyfile.ci

abstract class Task {
    protected String type

    abstract boolean execute(Object params);
}
