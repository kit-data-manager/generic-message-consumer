# KIT Data Manager - Generic Message Consumer

![Build Status](https://img.shields.io/travis/kit-data-manager/generic-message-consumer.svg)
[![codecov](https://codecov.io/gh/kit-data-manager/generic-message-consumer/branch/master/graph/badge.svg)](https://codecov.io/gh/kit-data-manager/generic-message-consumer)
[![CodeQL](https://github.com/kit-data-manager/generic-message-consumer/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/kit-data-manager/generic-message-consumer/actions/workflows/codeql-analysis.yml)
![License](https://img.shields.io/github/license/kit-data-manager/generic-message-consumer.svg)


Helper module for KIT DM 2.0 services providing capabilities to receive AMQP messages, e.g. sent by the repository or the authentication service. 
It allows to trigger actions in case of a specific event like the creation of a user or the modification of a resource. For details on which events
are emitted, please refer to the documentation of the emitting service.

## How to build

In order to build this module you'll need:

* Java SE Development Kit 8 or higher

After obtaining the sources change to the folder where the sources are located and call:

```
user@localhost:/home/user/generic-message-consumer$ ./gradlew install
BUILD SUCCESSFUL in 1s
3 actionable tasks: 3 executed
user@localhost:/home/user/generic-message-consumer$
```

The gradle wrapper will download and install gradle, if not already available. Afterwards, the module artifact
will be built and installed into the local maven repository, from where it can be used by other projects.

## Dependency from Maven Central Repository

Instead of using a local build you may also use the most recent version from the Central Maven Repository directly. 

### Maven

~~~~
<dependency>
    <groupId>edu.kit.datamanager</groupId>
    <artifactId>generic-message-consumer</artifactId>
    <version>0.2.1</version>
</dependency>
~~~~

### Gradle

~~~~
compile group: 'edu.kit.datamanager', name: 'generic-message-consumer', version: '0.2.1'
~~~~


## License

The KIT Data Manager is licensed under the Apache License, Version 2.0.
