package com.directmyfile.ci.utils

import groovy.io.FileType
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

import java.util.regex.Pattern

/**
 * Find files in a directory.
 */
class FileMatcher {
    private File parent

    /**
     * Create a File Matcher looking in the specified parent directory.
     * @param parent parent directory
     */
    FileMatcher(File parent) {
        this.parent = parent
    }

    /**
     * Create a File Matcher looking in the specified parent directory.
     * @param parent parent directory
     */
    FileMatcher(String parentPath) {
        this.parent = new File(parentPath)
    }

    /**
     * Find files/directories recursively
     * @param type directory or file or both
     * @return list of files
     */
    List<File> recursive(FileType type = FileType.ANY) {
        def files = []

        parent.eachFileRecurse(type) { file ->
            files += file
        }

        return files
    }

    /**
     * Calls the closure for files with the specified extension
     * @param extension file extension
     * @param closure closure to call
     */
    void withExtension(String extension,
                       @ClosureParams(value = SimpleType, options = "java.util.File") Closure closure) {
        def allFiles = recursive(FileType.FILES)
        List<File> matched = []
        allFiles.findAll { file ->
            if (file.name.endsWith(".${extension}")) {
                matched += file
            }
        }

        matched.each(closure)
    }

    /**
     * Gets a list of files with the specified extension
     * @param extension file extension
     * @return list of files
     */
    List<File> extension(String extension) {
        def files = []

        withExtension(extension) { file ->
            files += file
        }

        return files
    }

    /**
     * Gets a list of files matching the specified pattern
     * @param pattern Regular Expression
     * @return list of files
     */
    List<File> matching(Pattern pattern) {
        recursive(FileType.FILES).findAll {
            it.name.matches(pattern)
        }
    }

    static FileMatcher create(File parent) {
        return new FileMatcher(parent)
    }

    static FileMatcher create(String parent) {
        return new FileMatcher(parent)
    }
}
