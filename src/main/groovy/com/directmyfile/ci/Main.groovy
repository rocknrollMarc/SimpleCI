package com.directmyfile.ci

class Main {
    static void main(String[] args) {
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
            }
        }
    }
}
