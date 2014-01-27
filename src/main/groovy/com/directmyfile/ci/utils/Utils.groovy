package com.directmyfile.ci.utils

import groovy.transform.Memoized
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

import java.security.MessageDigest
import java.security.SecureRandom

class Utils {
    static Process execute(List<String> command) {
        def builder = new ProcessBuilder()
        builder.command(command)
        return builder.start()
    }

    @Memoized(maxCacheSize = 10)
    static File findCommandOnPath(String executableName) {
        def systemPath = System.getenv("PATH")
        def pathDirs = systemPath.split(File.pathSeparator)

        def executable = null
        for (pathDir in pathDirs) {
            def file = new File(pathDir, executableName)
            if (file.file && file.canExecute()) {
                executable = file
                break
            }
        }
        return executable
    }

    static Script parseConfig(File file) {
        def cc = new CompilerConfiguration()
        def scc = new SecureASTCustomizer()
        scc.with {
            closuresAllowed = false
            methodDefinitionAllowed = false
            importsWhitelist = []
            staticImportsWhitelist = []
            starImportsWhitelist = []
            constantTypesClassesWhiteList = [
                    Integer,
                    GString,
                    String,
                    Float,
                    Double,
                    Long,
                    Float.TYPE,
                    Double.TYPE,
                    Long.TYPE,
                    int,
                    long,
                    float,
                    double,
                    Object,
                    Boolean.TYPE,
                    Boolean,
                    boolean
            ].asImmutable()
        }

        cc.addCompilationCustomizers(scc)

        return new GroovyShell(cc).parse(file)
    }

    static def resource(String path) {
        return Utils.class.classLoader.getResourceAsStream(path)
    }

    @Memoized(maxCacheSize = 15)
    static def resourceToString(String path) {
        return resource(path).text
    }

    @Memoized(maxCacheSize = 15)
    static def decodeBase64(String input) {
        return new String(input.decodeBase64())
    }

    static def newProperties() {
        return new Properties()
    }

    static byte[] generateSalt(int size) {
        SecureRandom random = new SecureRandom()
        byte[] list = new byte[size]
        random.nextBytes(list)
        return list
    }

    static String generateHash(byte[] input) {
        def messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(input)
        return new BigInteger(1, messageDigest.digest()).toString(16).padLeft(40, '0')
    }

    static String toString(byte[] input) {
        return new BigInteger(1, input).toString(16).padLeft(40, '0')
    }
}
