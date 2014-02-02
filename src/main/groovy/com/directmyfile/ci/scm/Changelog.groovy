package com.directmyfile.ci.scm

import com.google.gson.annotations.Expose

class Changelog {
    List<Entry> entries = []

    Entry newEntry () {
        def entry = new Entry()
        entries.add(entry)
        return entry
    }

    static class Entry {
        @Expose
        String revision
        @Expose
        String message
        @Expose
        String author
    }
}
