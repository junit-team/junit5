# JUnit 5

This repository is the home of the next generation of JUnit, _JUnit 5_.

The project is currently in _Phase 5_, working toward additional [_milestone_](https://github.com/junit-team/junit5/milestones/5.0%20M2) releases.

[JUnit 5.0.0-M1](https://github.com/junit-team/junit5/releases/tag/r5.0.0-M1)
was released on July 7th, 2016.

## Roadmap

Consult the wiki for details on the current [JUnit 5 roadmap](https://github.com/junit-team/junit5/wiki#roadmap).

## Documentation

### User Guide

The [JUnit 5 User Guide] is available online.

### API

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

| CI Server | OS      | Status | Description |
| --------- | ------- | ------ | ----------- |
| Jenkins   | Linux   | [![Build Status](https://junit.ci.cloudbees.com/job/JUnit5/badge/icon)](https://junit.ci.cloudbees.com/job/JUnit5) | Official CI build server for JUnit 5 |
| Travis CI | Linux   | [![Travis CI build status](https://travis-ci.org/junit-team/junit5.svg?branch=master)](https://travis-ci.org/junit-team/junit5) | Used to perform quick checks on submitted pull requests and for build matrices including JDK 8 and JDK 9 early access builds |
| AppVeyor  | Windows | [![Build status](https://ci.appveyor.com/api/projects/status/xv8wc8w9sr44ghc4/branch/master?svg=true)](https://ci.appveyor.com/project/marcphilipp/junit5/branch/master) | Used to ensure that JUnit 5 can be built on Windows |

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

The following sections list the dependency metadata for the JUnit Platform, JUnit
Jupiter, and JUnit Vintage.

See also <http://repo1.maven.org/maven2/org/junit/> for releases and <https://oss.sonatype.org/content/repositories/snapshots/org/junit/> for snapshots.

### JUnit Platform

- **Group ID**: `org.junit.platform`
- **Version**: `1.0.0-M1` or `1.0.0-SNAPSHOT`
- **Artifact IDs**:
  - `junit-platform-commons`
  - `junit-platform-console`
  - `junit-platform-engine`
  - `junit-platform-gradle-plugin`
  - `junit-platform-launcher`
  - `junit-platform-runner`
  - `junit-platform-surefire-provider`

### JUnit Jupiter

- **Group ID**: `org.junit.jupiter`
- **Version**: `5.0.0-M1` or `5.0.0-SNAPSHOT`
- **Artifact IDs**:
  - `junit-jupiter-api`
  - `junit-jupiter-engine`

### JUnit Vintage

- **Group ID**: `org.junit.vintage`
- **Version**: `4.12.0-M1` or `4.12.0-SNAPSHOT`
- **Artifact ID**: `junit-vintage-engine`


[Atlassian]: https://www.atlassian.com/
[Clover]: https://www.atlassian.com/software/clover/
[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/master/CONTRIBUTING.md
[Jenkins CI server]: https://junit.ci.cloudbees.com/job/JUnit5/lastSuccessfulBuild/clover-report/
[JUnit 5 Javadoc]: http://junit.org/junit5/docs/current/api/
[JUnit 5 User Guide]: http://junit.org/junit5/docs/current/user-guide/
[Prototype]: https://github.com/junit-team/junit5/wiki/Prototype
[Twitter]: https://twitter.com/junitlambda
