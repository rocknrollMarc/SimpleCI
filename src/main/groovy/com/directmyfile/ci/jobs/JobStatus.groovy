package com.directmyfile.ci.jobs

import com.directmyfile.ci.notify.Colors


enum JobStatus {
    SUCCESS, FAILURE, NOT_STARTED, RUNNING, WAITING;

    @Override
    String toString() {
        return this.name().toLowerCase().capitalize().replace('_', ' ')
    }

    String getPanelClass() {
        switch (this) {
            case SUCCESS: return "panel-success"
            case FAILURE: return "panel-danger"
            case NOT_STARTED: return "panel-default"
            case RUNNING: return "panel-info"
            default: return "panel-default"
        }
    }

    String getIrcColor() {
        switch (this) {
            case SUCCESS: return Colors.GREEN
            case FAILURE: return Colors.RED
            case NOT_STARTED: return Colors.DARK_GRAY
            case RUNNING: return Colors.BLUE
            case WAITING: return Colors.DARK_BLUE
            default: Colors.NORMAL
        }
    }

    static JobStatus parse(int id) {
        if (id < 0 || id >= values().size()) {
            return NOT_STARTED
        }
        return values()[id]
    }
}