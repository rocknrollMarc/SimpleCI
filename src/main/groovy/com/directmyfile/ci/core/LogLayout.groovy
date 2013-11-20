package com.directmyfile.ci.core

import org.apache.log4j.Layout
import org.apache.log4j.spi.LoggingEvent
import java.text.SimpleDateFormat

class LogLayout extends Layout {

    private static final sdf = new SimpleDateFormat("yyyy-MM-dd H:m:s")
    String name

    LogLayout(String name) {
        this.name = name
    }

    @Override
    String format(LoggingEvent e) {
        return "[${sdf.format(new Date())}][${name}][${e.level}] ${e.message}\n"
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
