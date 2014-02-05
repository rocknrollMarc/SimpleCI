package com.directmyfile.ci.logging

import java.text.SimpleDateFormat

class Logger {
    private static final Map<String, Logger> loggers = [:]
    private static final dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private String name
    def level = LogLevel.INFO

    Logger(String name) {
        this.name = name
    }

    static Logger getLogger(String name) {
        if (name in loggers) {
            return loggers[name]
        } else {
            return loggers[name] = new Logger(name)
        }
    }

    boolean canLog(LogLevel input) {
        return level != LogLevel.DISABLED && input == level || input == LogLevel.ERROR || input == LogLevel.INFO && level == LogLevel.DEBUG
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

    void warning(String message) {
        log(LogLevel.WARNING, message)
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
