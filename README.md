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
- **Version**: `5.0.0-SNAPSHOT`
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

## Contributing

At the current stage, we are not accepting pull requests for the following reasons.

- **Legal reasons**: Any contributor will have to sign a contributor's agreement along the lines of [CONTRIBUTING.md]. The exact details have not been figured out yet, but we will contact you *before accepting your first pull request*.

If you want to provide input in the interim, please use [the project's issue tracker](https://github.com/junit-team/junit5/issues) or send us comments via [Twitter].


[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/master/CONTRIBUTING.md
[JUnit 5 Javadoc]: https://junit.ci.cloudbees.com/job/JUnit5/javadoc/
[JUnit 5 User Guide]: http://junit-team.github.io/junit5/
[Prototype]: https://github.com/junit-team/junit5/wiki/Prototype
[Twitter]: https://twitter.com/junitlambda
