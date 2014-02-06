package com.directmyfile.ci.plugins

import com.directmyfile.ci.utils.FileMatcher

class GroovyPluginProvider extends PluginProvider {
    GroovyShell shell = new GroovyShell()

    @Override
    void loadPlugins() {
        shell.setVariable("ci", ci)
        def pluginsDir = new File(ci.configRoot, "plugins")
        FileMatcher.create(pluginsDir).withExtension("groovy") { File file ->
            try {
                def script = shell.parse(file)
                script.run()
            } catch (e) {
                e.printStackTrace()
                System.exit(1)
            }
        }
    }
}
