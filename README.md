# SimpleCI

SimpleCI is a Continuous Integration Server written in [Groovy](http://groovy.codehaus.org/)

[![Build Status](https://travis-ci.org/DirectMyFile/SimpleCI.png?branch=master)](https://travis-ci.org/DirectMyFile/SimpleCI)

## Links

[Issue Tracker](https://directmyfile.atlassian.net/browse/CI)

## Building

Building SimpleCI requires that the [dmd](http://dlang.org/) compiler be installed.

To build SimpleCI, execute the following command:
```./gradlew jar```

The jar file will be in build/libs/

## Running

Execute the following command:
```java -jar SimpleCI.jar```

## Configuration

Edit the config.groovy file to match your configuration.

SimpleCI requires a MySQL-like (Be it MariaDB or MySQL) server to store CI information.

A blank database needs to be created, and SimpleCI will create all the tables necessary on first run.
