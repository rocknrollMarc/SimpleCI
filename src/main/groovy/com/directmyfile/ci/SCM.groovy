package com.directmyfile.ci

class SCM {
    private String type
    private String url

    SCM(String type, String url) {
        this.type = type
        this.url = url
    }

    boolean clone(File file) {
        if (type=='git') {
            def command
            if (new File(file, '.git').exists()) {
                command = "git pull --all"
            } else {
                command = "git clone ${url} ${file.getAbsolutePath()}"
            }
            def proc = new ProcessBuilder(command.tokenize(' ')).directory(file).start()
            def exit = proc.waitFor()
            return exit==0
        } else {
            throw new RuntimeException("Unknown SCM Type: ${type}")
        }
    }
}
