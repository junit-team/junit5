# JUnit 5

This repository is the home of the next generation of JUnit, _JUnit 5_.

[![Support us on Steady](https://img.shields.io/badge/support_us-on_Steady-ff7264.svg)](https://steadyhq.com/en/junit)

## Latest Releases

- General Availability (GA): [JUnit 5.4.2](https://github.com/junit-team/junit5/releases/tag/r5.4.2)
(April 7, 2019).
- Preview (Milestone/Release Candidate): [JUnit 5.5.0-M1](https://github.com/junit-team/junit5/releases/tag/r5.5.0-M1) (March 19, 2019).

## Documentation

- [User Guide]
- [Javadoc]
- [Release Notes]
- [Samples]

## Contributing

Contributions to JUnit 5 are both welcomed and appreciated. For specific guidelines
regarding contributions, please see [CONTRIBUTING.md] in the root directory of the
project. Those willing to use milestone or SNAPSHOT releases are encouraged
to file feature requests and bug reports using the project's
[issue tracker](https://github.com/junit-team/junit5/issues). Issues marked with an
<a href="https://github.com/junit-team/junit5/issues?q=is%3Aissue+is%3Aopen+label%3Aup-for-grabs">`up-for-grabs`</a>
label are specifically targeted for community contributions.

## Getting Help

Ask JUnit 5 related questions on [StackOverflow] or chat with the team and the community on [Gitter].

## Continuous Integration Builds

[![Build Status](https://dev.azure.com/junit-team/junit5/_apis/build/status/junit-team.junit5?branchName=master)](https://dev.azure.com/junit-team/junit5/_build/latest?definitionId=1&branchName=master)

Official CI build server for JUnit 5. Used to perform quick checks on submitted pull
requests and for build matrices including the latest released OpenJDK and early access
builds of the next OpenJDK.

## Code Coverage

Code coverage using [JaCoCo] for the latest build is available on [Codecov].

A code coverage report can also be generated locally via the [Gradle Wrapper] by
executing `gradlew -PenableJaCoCo clean jacocoRootReport`. The results will be available
in `build/reports/jacoco/jacocoRootReport/html/index.html`.

## Gradle Build Scans

JUnit 5 utilizes [Gradle's](https://gradle.org/) support for _Build Scans_. An example
build scan for JUnit 5 can be viewed [here](https://scans.gradle.com/s/bl3pw4mrbgsao).

## Building from Source

You need [JDK 11] to build JUnit 5.

All modules can be _built_ with the [Gradle Wrapper] using the following command.

`gradlew clean assemble`

All modules can be _tested_ with the [Gradle Wrapper] using the following command.

`gradlew clean test`

Since Gradle has excellent incremental build support, you can usually omit executing the
`clean` task.

## Installing in Local Maven Repository

All modules can be _installed_ with the [Gradle Wrapper] in a local Maven repository for
consumption in other projects via the following command.

`gradlew clean publishToMavenLocal`

## Dependency Metadata

The following sections list the dependency metadata for the JUnit Platform, JUnit
Jupiter, and JUnit Vintage.

See also <https://repo1.maven.org/maven2/org/junit/> for releases and <https://oss.sonatype.org/content/repositories/snapshots/org/junit/> for snapshots.

### JUnit Platform

- **Group ID**: `org.junit.platform`
- **Version**: `1.4.2`, `5.5.0-M1`, or `1.5.0-SNAPSHOT`
- **Artifact IDs** and Java **module** name:
  - `junit-platform-commons` (`org.junit.platform.commons`)
  - `junit-platform-console` (`org.junit.platform.console`)
  - `junit-platform-console-standalone` (*N/A*)
  - `junit-platform-engine` (`org.junit.platform.engine`)
  - `junit-platform-launcher` (`org.junit.platform.launcher`)
  - `junit-platform-reporting` (`org.junit.platform.reporting`)
  - `junit-platform-runner` (`org.junit.platform.runner`)
  - `junit-platform-suite-api` (`org.junit.platform.suite.api`)
  - `junit-platform-testkit` (`org.junit.platform.testkit`)

### JUnit Jupiter

- **Group ID**: `org.junit.jupiter`
- **Version**: `5.4.2`, `5.5.0-M1`, or `5.5.0-SNAPSHOT`
- **Artifact IDs** and Java **module** name:
  - `junit-jupiter` (`org.junit.jupiter`)
  - `junit-jupiter-api` (`org.junit.jupiter.api`)
  - `junit-jupiter-engine` (`org.junit.jupiter.engine`)
  - `junit-jupiter-migrationsupport` (`org.junit.jupiter.migrationsupport`)
  - `junit-jupiter-params` (`org.junit.jupiter.params`)

### JUnit Vintage

- **Group ID**: `org.junit.vintage`
- **Version**: `5.4.2`, `5.5.0-M1`, or `5.5.0-SNAPSHOT`
- **Artifact ID** and Java **module** name:
  - `junit-vintage-engine` (`org.junit.vintage.engine`)

### Bill of Materials (BOM)

- **Group ID**: `org.junit`
- **Artifact ID** `junit-bom`
- **Version**: `5.4.2`, `5.5.0-M1`, or `5.5.0-SNAPSHOT`


[Codecov]: https://codecov.io/gh/junit-team/junit5
[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/master/CONTRIBUTING.md
[Gitter]: https://gitter.im/junit-team/junit5
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper
[JaCoCo]: http://www.eclemma.org/jacoco/
[Javadoc]: https://junit.org/junit5/docs/current/api/
[JDK 11]: https://jdk.java.net/11/
[Release Notes]: https://junit.org/junit5/docs/current/release-notes/
[StackOverflow]: https://stackoverflow.com/questions/tagged/junit5
[User Guide]: https://junit.org/junit5/docs/current/user-guide/
[Samples]: https://github.com/junit-team/junit5-samples
