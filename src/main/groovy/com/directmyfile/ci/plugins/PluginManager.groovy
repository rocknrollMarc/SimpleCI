package com.directmyfile.ci.plugins
import com.directmyfile.ci.core.CI

class PluginManager {
    CI ci
    final List<PluginProvider> providers = []

    PluginManager(CI ci) {
        this.ci = ci
        providers.add(new GroovyPluginProvider())
        providers.add(new JSPluginProvider())
    }

    void loadPlugins() {
        providers.each { provider ->
            provider.ci = ci
            provider.loadPlugins()
        }
    }
}
