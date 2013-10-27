package com.directmyfile.ci

import groovy.util.ConfigSlurper

class CiConfig {
   CI ci
   def cs = new ConfigSlurper()

   CiConfig(CI ci) {
       this.ci = ci
   }

   void load() {
       def configFile = new File(ci.configRoot, "config.groovy")
       if (!configFile.exists()) configFile.createNewFile()
       def config = cs.parse(configFile.toURI().toURL())
       ci.port = config.get('web.port', 8080)

       config.writeTo(configFile.newWriter())
   }
}
