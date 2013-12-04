package com.directmyfile.ci.core

import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject

class PluginManager {
    CI ci
    GroovyClassLoader loader = new GroovyClassLoader()
    GroovyShell shell = new GroovyShell()

    PluginManager(CI ci) {
        this.ci = ci
    }

    void loadPlugins() {

        ci.logger.info "Loading Plugins"

        def pluginsDir = new File(ci.configRoot, "plugins")

        pluginsDir.mkdirs()

        shell.setVariable("ci", ci)

        pluginsDir.eachFileRecurse {
            if (it.isDirectory()) return

            ci.logger.debug "Checking if file ${it.canonicalPath} is a plugin."

            if (it.name.endsWith(".groovy")) {
                shell.evaluate(it)
            } else if (it.name.endsWith(".jar")) {
                loader.addURL(it.toURI().toURL())
            } else if (it.name.endsWith(".js")) {
                def cx = Context.enter()
                def scope = cx.initStandardObjects()
                def jsOut = Context.javaToJS(System.out, scope)
                ScriptableObject.putProperty(scope, "out", jsOut)
                ScriptableObject.putProperty(scope, "ci", ci)
                cx.evaluateReader(scope, it.newReader(), "JSPlugin", 1, null)
                cx.exit()
            } else {
                return
            }
            ci.logger.debug "Plugin ${it.canonicalPath} was loaded."
        }
    }
}
