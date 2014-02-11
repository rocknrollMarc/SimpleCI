package com.directmyfile.ci.plugins

import com.directmyfile.ci.utils.FileMatcher

class JarPluginProvider extends PluginProvider {
    @Override
    void loadPlugins() {
        def pluginsDir = new File(ci.configRoot, "plugins")
        FileMatcher.create(pluginsDir).withExtension("jar") { File file ->
            this.class.classLoader.addURL(file.toURI().toURL())
        }
    }
}
