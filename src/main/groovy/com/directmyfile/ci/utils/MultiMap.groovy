package com.directmyfile.ci.utils

class MultiMap<V> {
    private Map<String, List<V>> delegate = [:]

    @Override
    List<V> getAt(String key) {
        return get(key)
    }

    @Override
    void putAt(String key, Object value) {
        delegate[key] = value
    }

    List<V> get(String key) {
        if (!(key in delegate.keySet())) {
            delegate[key] = []
        }
        return delegate[key]
    }

    void add(String key, Object value) {
        this[key].add(value)
    }
}