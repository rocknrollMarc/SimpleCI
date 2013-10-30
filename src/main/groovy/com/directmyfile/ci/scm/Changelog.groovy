package com.directmyfile.ci.scm

class Changelog {
    List<Entry> entries = []

    Entry newEntry() {
        def entry = new Entry()
        entries.add(entry)
        return entry
    }

    String generateHTML() {
        def out = ""

        for (entry in entries) {
            out += "<br/>${entry.author}: ${entry.message}"
        }

        return out
    }

    static class Entry {
        String revision
        String message
        String author
    }
}
