package com.directmyfile.ci

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

class Utils {
    static Process execute(List<String> command) {
        def builder = new ProcessBuilder()
        builder.command(command)
        return builder.start()
    }

    static File findCommandOnPath(String executableName) {
        String systemPath = System.getenv("PATH")
        String[] pathDirs = systemPath.split(File.pathSeparator)

        File fullyQualifiedExecutable = null
        for (pathDir in pathDirs) {
            def file = new File(pathDir, executableName)
            if (file.isFile()) {
                fullyQualifiedExecutable = file
                break
            }
        }
        return fullyQualifiedExecutable
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
}
