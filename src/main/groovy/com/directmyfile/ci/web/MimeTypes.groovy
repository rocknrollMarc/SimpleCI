package com.directmyfile.ci.web

class MimeTypes {
    static Map<String, List<String>> types = [
            "font/x-woff": [".woff"],
            "text/html": [".html", ".htm"],
            "application/json": [".json"],
            "application/javascript": [".js"],
            "text/css": [".css"]
    ]

    static String get(String fileName) {
        def extension

        def split = fileName.tokenize("\\.")

        if (split.size() == 1) {
            extension = ""
        } else {
            extension = ".${split.last()}"
        }

        def type = "text/plain"

        for (entry in types) {
            if (entry.value.contains(extension)) {
                type = entry.key
            }
        }

        return type
    }
}
