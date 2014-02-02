package com.directmyfile.ci.utils

import java.util.concurrent.TimeUnit

class Timer {
    long time
    private long startTime
    private long stopTime

    void start () {
        this.startTime = System.currentTimeMillis()
    }

    long stop () {
        this.stopTime = System.currentTimeMillis()

        return this.time = stopTime - startTime
    }

    @Override
    String toString () {
        return String.format("%d minutes %d seconds", TimeUnit.MILLISECONDS.toMinutes(time), TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)))
    }

}
