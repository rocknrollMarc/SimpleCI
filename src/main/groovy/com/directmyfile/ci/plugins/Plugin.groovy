package com.directmyfile.ci.plugins

abstract class Plugin<V> {
    abstract void apply(V obj);
}
