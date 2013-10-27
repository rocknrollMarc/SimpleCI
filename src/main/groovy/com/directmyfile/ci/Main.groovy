package com.directmyfile.ci

import org.apache.log4j.BasicConfigurator

class Main {
    static void main(String[] args) {
        BasicConfigurator.configure()
        def ci = new CI()
        ci.start()
        def reader = System.in.newReader()

        reader.eachLine {
            def split = it.tokenize(' ')

            if (split[0]=='run') {
                def jobName = split[1]

                def job = ci.jobs.get(jobName)

                if (job==null) {
                    println "No Such Job: ${jobName}"
                } else {
                    ci.runJob(job)
                }
            } else if (split[0]=='restart') {
                ci.server.server.close()
                ci = null
                System.gc()
                ci = new CI()
                ci.start()
            }
        }
    }
}
