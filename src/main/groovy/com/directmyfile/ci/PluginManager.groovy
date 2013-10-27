package com.directmyfile.ci

class PluginManager {
    CI ci
    GroovyClassLoader loader = new GroovyClassLoader()
    GroovyShell shell = new GroovyShell()

    PluginManager(CI ci) {
        this.ci = ci
    }

    void loadPlugins() {
        def pluginsDir = new File(ci.configRoot, "plugins")

        pluginsDir.mkdirs()

        pluginsDir.eachFileRecurse {
            if (it.isDirectory()) return

            if (it.name.endsWith(".groovy")) {
                shell.evaluate(it)
            } else if (it.name.endsWith(".jar")) {
                loader.addURL(it.toURI().toURL())
            }
        }
    }
}
