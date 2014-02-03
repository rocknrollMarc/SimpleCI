package com.directmyfile.ci.api

/**
 * A tool installer will install things like Build Systems etc.
 */
abstract class ToolInstaller {
    abstract boolean install();
    abstract boolean remove();
}
