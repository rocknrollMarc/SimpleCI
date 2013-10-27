package com.directmyfile.ci

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
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
        def staticCompile = new ASTTransformationCustomizer(CompileStatic)
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
                    Long.TYPE
            ].asImmutable()
        }

        cc.addCompilationCustomizers(staticCompile, scc)

        return new GroovyShell(cc).parse(file)
    }
}
