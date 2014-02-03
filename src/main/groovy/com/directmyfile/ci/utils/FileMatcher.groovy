package com.directmyfile.ci.utils

import groovy.io.FileType
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

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

    void withExtension(String extension, @ClosureParams(value = SimpleType, options =  "java.util.File" ) Closure closure) {
        def allFiles = recursive(FileType.FILES)
        List<File> matched = []
        allFiles.findAll { file ->
            if (file.name.endsWith(".${extension}")) {
                matched += file
            }
        }

        matched.each(closure)
    }

    List<File> extension(String extension) {
        def files = []

        withExtension(extension) { file ->
            files += file
        }

        return files
    }
}
