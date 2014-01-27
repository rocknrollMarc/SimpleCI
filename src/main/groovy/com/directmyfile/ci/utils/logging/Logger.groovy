package com.directmyfile.ci.utils.logging

import java.text.SimpleDateFormat

class Logger {
    private static final Map<String, Logger> loggers = [:]
    private static final dateFormat = new SimpleDateFormat("yyyy-MM-dd H:m:s")

    private String name
    def level = LogLevel.DEBUG

    Logger(String name) {
        this.name = name
    }

    static Logger getLogger(String name) {
        if (loggers.containsKey(name))
            return loggers[name]
        else
            return loggers[name] = new Logger(name)
    }

    boolean canLog(LogLevel input) {
        return input == level || input == LogLevel.ERROR || input == LogLevel.INFO && level == LogLevel.DEBUG
    }

    String getName() {
        return name
    }

    void log(LogLevel level, String message) {
        if (canLog(level)) {
            println "[${dateFormat.format(new Date())}][${name}][${level.name()}] ${message}"
        }
    }

    void info(String message) {
        log(LogLevel.INFO, message)
    }

    void debug(String message) {
        log(LogLevel.DEBUG, message)
    }

    void error(String message) {
        log(LogLevel.ERROR, message)
    }

    void error(String message, Throwable e) {
        log(LogLevel.ERROR, message)
        e.printStackTrace()
    }
}