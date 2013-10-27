package com.directmyfile.ci

class CiConfig {
    private CI ci

    CiConfig(CI ci) {
        this.ci = ci
    }

    void load() {
        def configFile = new File(ci.configRoot, "config.groovy")
        Script configScript
        if (!configFile.exists()) {
            configFile.write(this.class.classLoader.getResourceAsStream("defaultConfig.groovy").text)
        }
        configScript = Utils.parseConfig(configFile)

        ci.port = configScript.getProperty("webPort") as int
        ci.sql.setURL(configScript.getProperty("sqlUrl") as String)
        ci.sql.setAuth(username: configScript.getProperty("sqlUsername") as String, password: configScript.getProperty("sqlPassword") as String)
    }
}
