package com.directmyfile.ci.core

import org.apache.log4j.Layout
import org.apache.log4j.spi.LoggingEvent

class LogLayout extends Layout {
    @Override
    String format(LoggingEvent e) {
        return "[${e.level}] ${e.message}\n"
    }

    @Override
    boolean ignoresThrowable() {
        return false
    }

    @Override
    void activateOptions() {
        super.activateOptions()
    }
}
