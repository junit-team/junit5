# JUnit Lambda

## Overview

This repository currently hosts the prototype for the next generation of JUnit, codenamed "JUnit Lambda".

## Continuous Integration Builds

[![Travis CI build status](https://travis-ci.org/junit-team/junit-lambda.svg)](https://travis-ci.org/junit-team/junit-lambda)

## Building from Source

All modules can be built with Gradle using the following command.

```
gradlew clean assemble
```

All modules can be tested with Gradle using the following command.

```
gradlew clean test
```

## Installing in Local Maven Repository

All modules can be installed in a local Maven repository for consumption in other projects via the following command.

```
gradlew clean install
```

### Dependency Metadata

 - **Group ID**: `org.junit`
 - **Version**: `5.0.0.BUILD-SNAPSHOT`
 - **Artifact IDs**: `junit-core`, `junit-engine`, `junit-interceptor`, `junit-launcher`, `junit4-bridge`


## Contributing

For the time being we're not accepting any pull requests. This has two reasons:

- The goal of the prototype is to get feedback on the API. Focusing on code style, formatting and other details will distract the community's (and our) attention. Most (if not all) of the code will be rewritten anyway.
- Legal reasons. Any contributor will have to sign a contributor's agreement along the line of  [Contributing.md](https://github.com/junit-team/junit-lambda/blob/master/CONTRIBUTING.md). The exact details have not been figured out yet, but we will contact you *before accepting your first pull request*.

If you want to contribute by commenting and discussing the API, use [the project's issue tracker](https://github.com/junit-team/junit-lambda/issues), add something to [the feedback page](https://github.com/junit-team/junit-lambda/wiki/Prototype-Feedback) or comment on [Twitter](https://twitter.com/junitlambda).
