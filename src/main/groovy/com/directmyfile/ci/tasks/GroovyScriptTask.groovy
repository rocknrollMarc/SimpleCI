package com.directmyfile.ci.tasks

import com.directmyfile.ci.api.Task
import com.directmyfile.ci.jobs.Job
import org.codehaus.groovy.control.CompilerConfiguration

class GroovyScriptTask extends Task {
    @Override
    boolean execute(Object params) {
        def job = params["job"] as Job
        def scriptFile = file(job.buildDir, params["script"] as String)

        def compiler = new CompilerConfiguration()

        compiler.output = job.logFile.newPrintWriter()

        def shell = new GroovyShell()
        try {
            def script = shell.parse(scriptFile)
            script.run()
        } catch (e) {
            e.printStackTrace(compiler.output)
            return false
        }
        return true
    }
}
