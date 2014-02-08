package com.directmyfile.ci.api

import com.directmyfile.ci.core.CI

/**
 * A tool installer will install things like Build Systems etc.
 */
abstract class ToolInstaller {
    protected CI ci

    abstract boolean install();

    abstract boolean remove();
}
