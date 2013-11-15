package com.directmyfile.ci

import com.directmyfile.ci.core.CI
import com.directmyfile.ci.core.LogLayout
import com.directmyfile.ci.exception.UnexpectedExceptionHandler
import groovy.util.logging.Log4j
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

@Log4j('logger')
class Main {

    /* Running states */
    private static def ciRunning = true
    private static def botRunning = false

    @SuppressWarnings("GroovyEmptyStatementBody")
    static void main(String[] args) throws Exception {
        def consoleAppender = new ConsoleAppender()
        consoleAppender.activateOptions()
        consoleAppender.layout = new LogLayout("CORE")
        logger.setLevel(Level.INFO)
        logger.addAppender(consoleAppender)

        Thread.setDefaultUncaughtExceptionHandler(new UnexpectedExceptionHandler())
        System.addShutdownHook {
            logger.info "Shutdown sequence initiated"
            ciRunning = false

            /* Run through the states allowing them to shutdown */
            while (botRunning);

            logger.info "Shutdown sequence complete"
        }

        Logger.getRootLogger().setLevel(Level.INFO)
        def props = new Properties()
        props.load(Utils.resource("logging.properties"))
        PropertyConfigurator.configure(props)

        def ci = new CI()
        ci.start()

        def thread = new Thread(new Runnable() {
            @Override
            void run() {
                def reader = System.in.newReader()

                reader.eachLine {
                    def split = it.tokenize(' ')

                    if (split[0] == 'run') {
                        def jobName = split[1]

                        def job = ci.jobs.get(jobName)

                        if (job == null) {
                            println "No Such Job: ${jobName}"
                        } else {
                            ci.runJob(job)
                        }
                    } else if (split[0] == 'restart') {
                        ci.vertxManager.stopWebServer()
                        ci = null
                        System.gc()
                        ci = new CI()
                        ci.start()
                    }
                }
            }
        })
        thread.setDaemon(true)
        thread.start()

        // Must run on the main thread
        ci.startBot()

    }

    /* Shut down & startup methods */
    static boolean isRunning() {
        return ciRunning
    }

    static void setBotState(boolean running) {
        botRunning = running
    }
}
