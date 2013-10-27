package com.directmyfile.ci.exception

class ToolException extends Exception {
    ToolException (String message) {
        super(message)
    }

    ToolException (String message, Throwable cause) {
        super(message, cause)
    }
}
