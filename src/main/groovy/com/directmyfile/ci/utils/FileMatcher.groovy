package com.directmyfile.ci.utils

import groovy.io.FileType

class FileMatcher {
    private File parent

    FileMatcher(File parent) {
        this.parent = parent
    }

    List<File> recursive(FileType type = FileType.ANY) {
        def files = []

        parent.eachFileRecurse(type) { file ->
            files += file
        }

        return files
    }

    void withExtension(String extension, Closure closure) {
        def allFiles = recursive(FileType.FILES)
        def matched = []

        allFiles.findAll { file ->
            if (file.name.endsWith(".${extension}")) {
                matched += file
            }
        }

        matched.each(closure)
    }
}
