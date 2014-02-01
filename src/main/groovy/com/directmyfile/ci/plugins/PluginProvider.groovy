package com.directmyfile.ci.plugins

import com.directmyfile.ci.core.CI

/**
 * A Plugin Provider will allow extending the plugin system to include multiple types of plugins.
 */
abstract class PluginProvider {
    CI ci
    abstract void loadPlugins();
}
