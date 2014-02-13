# SimpleCI

SimpleCI is a Continuous Integration Server written in [Groovy](http://groovy.codehaus.org/)

[![Build Status](https://travis-ci.org/DirectMyFile/SimpleCI.png?branch=master)](https://travis-ci.org/DirectMyFile/SimpleCI)

## Links

[Wiki](https://github.com/DirectMyFile/SimpleCI/wiki)
[Issues](https://github.com/DirectMyFile/SimpleCI/issues)

## Features

- Jobs
- Job Queue
- Job Hooks
- REST API
- Tasks
- SCM
- Plugins
- Web Interface
- IRC Bot

Plugins can be written in Groovy/JavaScript and can add Task Types and SCM support. SimpleCI has support for Gradle, GNU Make, Commands for tasks and Git for SCMs built-in.

## Building

Building SimpleCI requires that the [dmd](http://dlang.org/) compiler be installed.

To build SimpleCI, execute the following command:
```./gradlew jar```

The Jar File will be located at build/libs/SimpleCI.jar

## Running

Execute the following command:
```java -jar SimpleCI.jar```

## Configuration

Edit the config.groovy file to match your configuration.

SimpleCI requires a MySQL-like (Be it MariaDB or MySQL) server to store CI information.

A blank database needs to be created, and SimpleCI will create all the tables necessary on first run.
