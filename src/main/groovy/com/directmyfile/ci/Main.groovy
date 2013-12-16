package com.directmyfile.ci

import com.directmyfile.ci.core.CI
import com.directmyfile.ci.logging.Logger
import org.apache.log4j.Level as Log4jLevel
import org.apache.log4j.Logger as Log4j

class Main {

    /* Running states */
    private static def ciRunning = true
    private static def botRunning = false

    private static logger = Logger.getLogger("Console")

    @SuppressWarnings("GroovyEmptyStatementBody")
    static void main(String[] args) throws Exception {

        /* Configure log4j to fix warnings */
        Log4j.getRootLogger().setLevel(Log4jLevel.OFF)

        Thread.defaultUncaughtExceptionHandler = [
                uncaughtException: { Thread thread, Throwable e ->
                    logger.error("An unexpected error occurred in SimpleCI", e)
                }
        ] as Thread.UncaughtExceptionHandler

        System.addShutdownHook {
            logger.info "Shutdown sequence initiated"
            ciRunning = false

            /* Run through the states allowing them to shutdown */
            while (botRunning);

            logger.info "Shutdown sequence complete"
        }

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
                    } else if (split[0] == 'stop') {
                        System.exit(0)
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
