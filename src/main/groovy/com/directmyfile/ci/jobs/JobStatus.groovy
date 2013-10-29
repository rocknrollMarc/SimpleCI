package com.directmyfile.ci.jobs

import org.nanobot.Colors


enum JobStatus {
    SUCCESS, FAILURE, NOT_STARTED, RUNNING;

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

    String getIRCColor() {
        switch (this) {
            case SUCCESS: return Colors.GREEN
            case FAILURE: return Colors.RED
            case NOT_STARTED: return Colors.DARK_GRAY
            case RUNNING: return Colors.BLUE
            default: Colors.NORMAL
        }
    }

    static JobStatus parse(int id) {
        switch (id) {
            case 0: return NOT_STARTED
            case 1: return SUCCESS
            case 2: return FAILURE
            case 3: return RUNNING
            default: return NOT_STARTED
        }
    }

    int intValue() {
        switch (this) {
            case NOT_STARTED: return 1
            case SUCCESS: return 1
            case FAILURE: return 2
            case RUNNING: return 3
            default: return 0
        }
    }
}