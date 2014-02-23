package com.directmyfile.ci.utils

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import org.codehaus.groovy.control.CompilerConfiguration

import java.security.MessageDigest
import java.security.SecureRandom

class Utils {

    static JsonSlurper jsonSlurper = new JsonSlurper()

    @CompileStatic
    static Process execute(List<String> command) {
        def builder = new ProcessBuilder()
        builder.command(command)
        return builder.start()
    }

    @CompileStatic
    @Memoized(maxCacheSize = 10)
    static File findCommandOnPath(String executableName) {
        def systemPath = System.getenv("PATH")
        def pathDirs = systemPath.split(File.pathSeparator)

        File executable = null
        for (pathDir in pathDirs) {
            def file = new File(pathDir, executableName)
            if (file.file && file.canExecute()) {
                executable = file
                break
            }
        }
        return executable
    }

    @CompileStatic
    static Script parseConfig(File file) {
        def cc = new CompilerConfiguration()

        return new GroovyShell(cc).parse(file)
    }

    @CompileStatic
    static InputStream resource(String path) {
        return Utils.class.classLoader.getResourceAsStream(path)
    }

    @CompileStatic
    @Memoized(maxCacheSize = 15)
    static def resourceToString(String path) {
        return resource(path).text
    }

    @CompileStatic
    @Memoized(maxCacheSize = 15)
    static def encodeBase64(String input) {
        return input.bytes.encodeBase64().writeTo(new StringWriter()).toString()
    }

    @CompileStatic
    @Memoized(maxCacheSize = 15)
    static def decodeBase64(String input) {
        return new String(input.decodeBase64())
    }

    @CompileStatic
    static byte[] generateSalt(int size) {
        def random = new SecureRandom()
        byte[] list = new byte[size]
        random.nextBytes(list)
        return list
    }

    @CompileStatic
    static String generateHash(byte[] input) {
        def messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(input)
        return toString(messageDigest.digest())
    }

    @CompileStatic
    static String toString(byte[] input) {
        return new BigInteger(1, input).toString(16).padLeft(40, '0')
    }

    @CompileStatic
    static def encodeJSON(Object object) {
        return new JsonBuilder(object).toPrettyString()
    }

    @CompileStatic
    static def parseJSON(String text) {
        return jsonSlurper.parseText(text)
    }
}
