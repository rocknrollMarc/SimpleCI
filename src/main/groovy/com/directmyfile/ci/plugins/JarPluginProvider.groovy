package com.directmyfile.ci.plugins

import com.directmyfile.ci.exception.CIException
import com.directmyfile.ci.utils.FileMatcher

import java.util.jar.JarFile

class JarPluginProvider extends PluginProvider {
    @Override
    void loadPlugins() {
        def pluginsDir = new File(ci.configRoot, "plugins")
        FileMatcher.create(pluginsDir).withExtension("jar") { File file ->
            this.class.classLoader.addURL(file.toURI().toURL()) // This method will exist at runtime
            def jar = new JarFile(file)
            def manifest = jar.manifest.mainAttributes
            if ("Plugin" in manifest.keySet()) { // A Class that extends Plugin
                def className = manifest.getValue("Plugin")
                def clazz = this.class.classLoader.loadClass(className)
                if (!clazz.isAssignableFrom(Plugin)) {
                    throw new CIException("Plugin Jar's Class is not an instance of ${Plugin.class.name}")
                }
                def instance = clazz.newInstance() as Plugin
                instance.apply(ci)
            }
        }
    }
}
