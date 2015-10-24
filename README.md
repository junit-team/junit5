# JUnit Lambda

## Overview

This repository currently hosts the prototype for the next generation of JUnit, codenamed "JUnit Lambda".

## Continuous Integration Builds

[![Travis CI build status](https://travis-ci.org/junit-team/junit-lambda.svg)](https://travis-ci.org/junit-team/junit-lambda)

## Building from Source

All modules can be built with Gradle using the following command.

```
gradlew clean test assemble
```

## Installing in Local Maven Repository

All modules can be installed in a local Maven repository for consumption in other projects via the following command.

```
gradlew clean install
```

### Dependency Metadata

 - **Group ID**: `org.junit`
 - **Version**: `5.0.0.BUILD-SNAPSHOT`
 - **Artifact IDs**: `junit-core`, `junit-engine`, `junit-interceptor`, `junit-launch`, `junit4-bridge`
