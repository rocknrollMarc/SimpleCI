package com.directmyfile.ci.exception

class JobConfigurationException extends Exception {
    JobConfigurationException(String message) {
        super(message)
    }

    JobConfigurationException(String message, Throwable cause) {
        super(message, cause)
    }
}
