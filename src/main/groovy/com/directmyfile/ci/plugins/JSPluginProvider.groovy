package com.directmyfile.ci.plugins
import com.directmyfile.ci.utils.FileMatcher

import javax.script.ScriptEngineManager

class JSPluginProvider extends PluginProvider {
    @Override
    void loadPlugins() {
        def manager = new ScriptEngineManager()
        def js = manager.getEngineByExtension("js")
        def pluginsDir = new File(ci.configRoot, "plugins")
        js.put("ci", ci)
        js.put("logger", ci.logger)
        new FileMatcher(pluginsDir).withExtension("js") { File file ->
            try {
                js.eval(file.newReader())
            } catch (e) {
                e.printStackTrace()
                System.exit(1)
            }
        }
    }
}
