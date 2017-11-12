# JUnit 5

This repository is the home of the next generation of JUnit, _JUnit 5_.

[JUnit 5.0.0](https://github.com/junit-team/junit5/releases/tag/r5.0.0)
was released on September 10, 2017.

## Roadmap

Consult the wiki for details on the current [JUnit 5 roadmap](https://github.com/junit-team/junit5/wiki/Roadmap).

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

## Getting Help
Ask JUnit 5 related questions on [StackOverflow] or chat with us on [Gitter].

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

## Gradle Build Scans

JUnit 5 utilizes [Gradle's](https://gradle.com/) support for _Build Scans_. An example
build scan for JUnit 5 can be viewed [here](https://scans.gradle.com/s/pgjgssca2kkli).
Note, however, that the number of listed tests only reflects the Spock tests within the
JUnit 5 test suite. To see a full representation of the number of tests executed per
project, click on "See console output" on the build scan page.

## Building from Source

All modules can be built with Gradle using the following command.

```
gradlew clean assemble
```

All modules can be tested with Gradle using the following command.

```
gradlew clean test
```

Since Gradle has excellent incremental build support, you can usually omit executing the `clean` task.

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
- **Version**: `1.0.2` or `1.0.3-SNAPSHOT`
- **Artifact IDs** and **Automatic-Module-Name**:
  - `junit-platform-commons` (`org.junit.platform.commons`)
  - `junit-platform-console` (`org.junit.platform.console`)
  - `junit-platform-console-standalone` (*N/A*)
  - `junit-platform-engine` (`org.junit.platform.engine`)
  - `junit-platform-gradle-plugin` (`org.junit.platform.gradle.plugin`)
  - `junit-platform-launcher` (`org.junit.platform.launcher`)
  - `junit-platform-runner` (`org.junit.platform.runner`)
  - `junit-platform-suite-api` (`org.junit.platform.suite.api`)
  - `junit-platform-surefire-provider` (`org.junit.platform.surefire.provider`)

### JUnit Jupiter

- **Group ID**: `org.junit.jupiter`
- **Version**: `5.0.2` or `5.0.3-SNAPSHOT`
- **Artifact IDs** and **Automatic-Module-Name**:
  - `junit-jupiter-api` (`org.junit.jupiter.api`)
  - `junit-jupiter-engine` (`org.junit.jupiter.engine`)
  - `junit-jupiter-migrationsupport` (`org.junit.jupiter.migrationsupport`)
  - `junit-jupiter-params` (`org.junit.jupiter.params`)

### JUnit Vintage

- **Group ID**: `org.junit.vintage`
- **Version**: `4.12.2` or `4.12.3-SNAPSHOT`
- **Artifact ID** and **Automatic-Module-Name**:
  - `junit-vintage-engine` (`org.junit.vintage.engine`)

## Java 9 Module Names

All published JAR artifacts contain an [Automatic-Module-Name] manifest attribute
whose value is used as the name of the automatic module defined by that JAR file
when it is placed on the **Java 9** module path. The names are listed above in the
Dependency Metadata section.

This allows test module authors to require well-known JUnit module names as
can be seen in the following example:

```
open module foo.bar {
  requires org.junit.jupiter.api;
  requires org.junit.platform.commons;
  requires org.opentest4j;
}
```

The `junit-platform-console-standalone` JAR does not provide an automatic module name
as it is not intended to be used as a module.


[Atlassian]: https://www.atlassian.com/
[Automatic-Module-Name]: http://mail.openjdk.java.net/pipermail/jpms-spec-experts/2017-April/000667.html
[Clover]: https://www.atlassian.com/software/clover/
[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/master/CONTRIBUTING.md
[Gitter]: https://gitter.im/junit-team/junit5
[Jenkins CI server]: https://junit.ci.cloudbees.com/job/JUnit5/lastSuccessfulBuild/clover-report/
[JUnit 5 Javadoc]: http://junit.org/junit5/docs/current/api/
[JUnit 5 User Guide]: http://junit.org/junit5/docs/current/user-guide/
[Prototype]: https://github.com/junit-team/junit5/wiki/Prototype
[StackOverflow]: https://stackoverflow.com/questions/tagged/junit5
[Twitter]: https://twitter.com/junitlambda
