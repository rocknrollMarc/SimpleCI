package com.directmyfile.ci.exception

import com.directmyfile.ci.Main

class UnexpectedExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    void uncaughtException(Thread t, Throwable e) {
        Main.logger.error("An unexpected error occurred in SimpleCI")
        e.printStackTrace(System.err)
    }
}
