package com.directmyfile.ci.plugins


import com.directmyfile.ci.utils.FileMatcher

import javax.script.ScriptEngineManager

/**
 * A Plugin Provider that uses the Java Scripting API to provide for all types of scripts.
 */
class ScriptPluginProvider extends PluginProvider {
    @Override
    void loadPlugins() {
        def manager = new ScriptEngineManager()
        manager.put("ci", ci)
        manager.put("logger", ci.logger)
        manager.engineFactories.each { factory ->
            FileMatcher.create(new File(ci.configRoot, "plugins")).withExtensions(factory.extensions) { File file ->
                factory.scriptEngine.eval(file.text)
            }
        }
    }
}
