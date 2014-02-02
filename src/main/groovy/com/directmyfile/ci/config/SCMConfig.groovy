package com.directmyfile.ci.config

class SCMConfig {
    String type
    String url

    SCMConfig (String type, String url) {
        this.type = type
        this.url = url
    }
}
