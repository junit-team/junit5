# JUnit 5

This repository is the home of the next generation of JUnit, _JUnit 5_.

The project is currently in _Phase 4_, working toward the first official
[_milestone_](https://github.com/junit-team/junit5/milestones/5.0%20M1) release.

[JUnit 5.0.0-ALPHA](https://github.com/junit-team/junit5/releases/tag/r5.0.0-ALPHA)
was released on February 1st, 2016.

## Roadmap

Consult the wiki for details on the current [JUnit 5 roadmap](https://github.com/junit-team/junit5/wiki#roadmap).

## Documentation

### User Guide

The [JUnit 5 User Guide] is available online.

### Javadoc

The [JUnit 5 Javadoc] is available online.

## Contributing

Contributions to JUnit 5 are both welcomed and appreciated. For specific guidelines
regarding contributions, please see [CONTRIBUTING.md] in the root directory of the
project. Those willing to use the ALPHA, milestone, or SNAPSHOT releases are encouraged
to file feature requests and bug reports using the project's
[issue tracker](https://github.com/junit-team/junit5/issues). Issues marked with an
<a href="https://github.com/junit-team/junit5/issues?q=is%3Aissue+is%3Aopen+label%3Aup-for-grabs">`up-for-grabs`</a>
label are specifically targeted for community contributions.

## Continuous Integration Builds

[![Travis CI build status](https://travis-ci.org/junit-team/junit5.svg?branch=master)](https://travis-ci.org/junit-team/junit5)

## Code Coverage

Code coverage using [Clover] for the latest build is available on the [Jenkins CI server].
We are thankful to [Atlassian] for providing the Clover license free of charge.

A code coverage report can also be generated locally by executing
`gradlew -PenableClover clean cloverHtmlReport` if you have a local Clover license file
on your computer. The results will be available in
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


[Atlassian]: https://www.atlassian.com/
[Clover]: https://www.atlassian.com/software/clover/
[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/master/CONTRIBUTING.md
[Jenkins CI server]: https://junit.ci.cloudbees.com/job/JUnit5/lastSuccessfulBuild/clover-report/
[JUnit 5 Javadoc]: https://junit.ci.cloudbees.com/job/JUnit5/javadoc/
[JUnit 5 User Guide]: http://junit-team.github.io/junit5/
[Prototype]: https://github.com/junit-team/junit5/wiki/Prototype
[Twitter]: https://twitter.com/junitlambda
