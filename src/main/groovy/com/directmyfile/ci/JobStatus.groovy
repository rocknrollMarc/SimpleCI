package com.directmyfile.ci


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
}