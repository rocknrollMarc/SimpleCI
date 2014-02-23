package com.directmyfile.ci.tools

import com.directmyfile.ci.api.ToolInstaller

class GradleToolInstaller extends ToolInstaller {
    @Override
    boolean install() {
        return false
    }

    @Override
    boolean remove() {
        return false
    }
}
