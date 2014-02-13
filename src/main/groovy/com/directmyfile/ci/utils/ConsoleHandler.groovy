package com.directmyfile.ci.utils

class ConsoleHandler {
    static BufferedReader reader = System.in.newReader()
    static boolean looping = false

    static void readLine(Closure handler = {}) {
        def line = reader.readLine()
        if (line == null || line.trim() == "") {
            return
        }
        def split = line.tokenize()
        def cmd = split[0]
        def args = split.drop(1)
        handler(cmd, args)
    }

    static void loop(Closure handler = {}, Closure<Boolean> stopHandler = { false }) {
        if (looping) {
            throw new IllegalStateException("Console is already looping.")
        }
        // Asynchronous Loop
        Thread.start("Console") { ->
            looping = true
            while (!stopHandler()) {
                readLine(handler)
            }
            looping = false
        }
    }
}