# JUnit Lambda

We are currently in _Phase 2_, building the prototype. Jump straight to the [Prototype](https://github.com/junit-team/junit-lambda/wiki/Prototype) page for full details.

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

## Dependency Metadata

- **Group ID**: `org.junit.prototype`
- **Version**: `5.0.0-SNAPSHOT`
- **Artifact IDs**:
	- `junit-commons`
	- `junit-console`
	- `junit-engine-api`
	- `junit-launcher`
	- `junit4-engine`
	- `junit4-launcher-runner`
	- `junit5-api`
	- `junit5-engine`
	- `open-test-alliance` (Version `1.0.0-SNAPSHOT`)

See also: <https://oss.sonatype.org/content/repositories/snapshots/org/junit/prototype/>

## Contributing

# JUnit Lambda Prototype

The JUnit Lambda team is currently collecting feedback about the prototype which [is described in the Wiki](https://github.com/junit-team/junit-lambda/wiki/Prototype).

The goal of the prototype phase is to come up with some working code that will entice people to give us feedback on the **programming model, APIs, and SPIs** as described in the sections below. At the current stage, we are **NOT COLLECTING FEEDBACK ABOUT THE IMPLEMENTATION**, simply because it's in large parts neither polished, nor thoroughly tested, nor stable.

We are also not accepting any pull requests at this time, for the following reasons:

- **Focus**: The goal of the prototype is to get feedback on the API and programming model. Focusing on code style, formatting, and other details will distract the community's (and our) attention. A lot of the code in the prototype will potentially be rewritten anyway.
- **Legal reasons**: Any contributor will have to sign a contributor's agreement along the lines of [Contributing.md](https://github.com/junit-team/junit-lambda/blob/master/CONTRIBUTING.md). The exact details have not been figured out yet, but we will contact you *before accepting your first pull request*.

If you want to provide input in the interim, please use [the project's issue tracker](https://github.com/junit-team/junit-lambda/issues) or send us comments via [Twitter](https://twitter.com/junitlambda).
