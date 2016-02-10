# JUnit 5

This repository is the home of the next generation of JUnit, _JUnit 5_.

The project is currently in _Phase 3_, building the _Alpha 1 milestone_ which is based on feedback received for the [Prototype].

## Documentation

### User Guide

The [JUnit 5 User Guide] is available online.

### Javadoc

The [JUnit 5 Javadoc] is available online.

## Continuous Integration Builds

[![Travis CI build status](https://travis-ci.org/junit-team/junit5.svg?branch=master)](https://travis-ci.org/junit-team/junit5)

## Code Coverage

Code coverage using [Clover](https://www.atlassian.com/software/clover/) for the latest build is available on the [Jenkins CI server](https://junit.ci.cloudbees.com/job/JUnit5/clover/).

A code coverage report can also be generated locally by executing `gradlew -PenableClover clean cloverHtmlReport`. The results will be available in
`junit-tests/build/reports/clover/html/index.html`.


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

- **Group ID**: `org.junit`
- **Version**: `5.0.0-ALPHA` OR `5.0.0-SNAPSHOT`
- **Artifact IDs**:
	- `junit-commons`
	- `junit-console`
	- `junit-engine-api`
	- `junit-gradle`
	- `junit-launcher`
	- `junit4-engine`
	- `junit4-runner`
	- `junit5-api`
	- `junit5-engine`
	- `surefire-junit5`

See also: <https://oss.sonatype.org/content/repositories/snapshots/org/junit/>

[JUnit 5 Javadoc]: https://junit.ci.cloudbees.com/job/JUnit5/javadoc/
[JUnit 5 User Guide]: http://junit-team.github.io/junit5/
[Prototype]: https://github.com/junit-team/junit5/wiki/Prototype
[Twitter]: https://twitter.com/junitlambda
