package com.directmyfile.ci

class CiConfig {
    private CI ci

    CiConfig(CI ci) {
        this.ci = ci
    }

    void load() {
        def configFile = new File(ci.configRoot, "config.groovy")
        if (!configFile.exists()) {
            configFile.write(this.class.classLoader.getResourceAsStream("defaultConfig.groovy").text)
        }
        def configScript = Utils.parseConfig(configFile)

        configScript.run()

        def config = configScript.binding

        ci.port = config.getVariable("webPort") as int
        ci.sql.setURL(config.getVariable("sqlUrl") as String)
        ci.sql.setAuth(username: config.getVariable("sqlUsername") as String, password: config.getVariable("sqlPassword") as String)
    }
}
