package com.directmyfile.ci.utils.logging

enum LogLevel {
    ERROR, INFO, DEBUG, DISABLED;

    static LogLevel parse(String name) {
        if (!(values()*.name().contains(name))) {
            return DISABLED
        } else {
            return values().find {
                it.name() == name
            }
        }
    }
}