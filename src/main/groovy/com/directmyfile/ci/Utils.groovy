package com.directmyfile.ci

import com.directmyfile.ci.exception.CIException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

class Utils {
    static Process execute(List<String> command) {
        def builder = new ProcessBuilder()
        builder.command(command)
        return builder.start()
    }

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
        def res = Utils.class.classLoader.getResourceAsStream(path)

        if (res == null) {
            throw new CIException("Tried to get a resource that is not available! ${path}")
        }

        return res
    }
}
