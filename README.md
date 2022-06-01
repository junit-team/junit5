# <img src="https://junit.org/junit5/assets/img/junit5-logo.png" align="center" width="100"> JUnit 5

JUnit 5 is the next generation of JUnit. The goal is to create an up-to-date foundation for developer-side testing on the JVM. This includes focusing on Java 8 and above, as well as enabling many different styles of testing.

JUnit 5 is the result of [JUnit Lambda] and its [crowdfunding campaign on Indiegogo.]

[![Support JUnit](https://img.shields.io/badge/%F0%9F%92%9A-Support%20JUnit-brightgreen.svg)](https://junit.org/sponsoring)

## Latest Releases
[![Jupiter](https://img.shields.io/maven-metadata/v.svg?color=0057b7&label=Jupiter&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fjunit%2Fjupiter%2Fjunit-jupiter%2Fmaven-metadata.xml&versionPrefix=5.8)](https://search.maven.org/search?q=g:org.junit.jupiter%20AND%20v:5.8.2) [![Vintage](https://img.shields.io/maven-metadata/v.svg?colorB=0057b7&label=Vintage&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fjunit%2Fvintage%2Fjunit-vintage-engine%2Fmaven-metadata.xml&versionPrefix=5.8)](https://search.maven.org/search?q=g:org.junit.vintage%20AND%20v:5.8.2) [![Platform](https://img.shields.io/maven-metadata/v.svg?colorB=0057b7&label=Platform&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fjunit%2Fplatform%2Fjunit-platform-commons%2Fmaven-metadata.xml&versionPrefix=1.8)](https://search.maven.org/search?q=g:org.junit.platform%20AND%20v:1.8.2)
- General Availability (GA): [JUnit 5.8.2] (November 28, 2021)
- Preview (Milestone/Release Candidate): n/a
- [Release Notes]

## Continuous Integration Builds

[![CI Status](https://github.com/junit-team/junit5/workflows/CI/badge.svg)](https://github.com/junit-team/junit5/actions) [![Cross-Version Status](https://github.com/junit-team/junit5/workflows/Cross-Version/badge.svg)](https://github.com/junit-team/junit5/actions)

Official CI build server for JUnit 5. Used to perform quick checks on submitted pull
requests and for build matrices including the latest released OpenJDK and early access
builds of the next OpenJDK.

## Code Coverage

Code coverage using [JaCoCo] for the latest build is available on [Codecov].

A code coverage report can also be generated locally via the [Gradle Wrapper] by
executing `gradlew -PenableJaCoCo clean jacocoRootReport`. The results will be available
in `build/reports/jacoco/jacocoRootReport/html/index.html`.

## Code Style

For code styling it is recommended to run [EclipseCodeFormatter] and/or reference [Google Java Style Guide] and the [CONTRIBUTING.md].

## Features and Documentation

- [Website]
- [User Guide]
- [Javadoc]
- [Full suite of test features]
- [Backwards compatibility with JUnit4]
- [Compatibility with major IDEs]
- [Extensions]

## Examples

We provide example artifacts of JUnit5 configurations within the following repo. Please refer to the README.md within each directory for instructions.
- [Samples]

## Building from Source

You need [JDK 17] to build JUnit 5. [Gradle toolchains] are used to detect and
potentially download additional JDKs for compilation and test execution.

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

## Contributing

Contributions to JUnit 5 are both welcomed and appreciated. For specific guidelines
regarding contributions, please see [CONTRIBUTING.md]. Those willing to use milestone or SNAPSHOT releases are encouraged
to file feature requests and bug reports using the project's
[issue tracker]. Issues marked with an
<a href="https://github.com/junit-team/junit5/issues?q=is%3Aissue+is%3Aopen+label%3Aup-for-grabs">`up-for-grabs`</a>
label are specifically targeted for community contributions.

## Getting Help

Ask JUnit 5 related questions on [StackOverflow] or chat with the community on [Gitter].

## License

[Eclipse Public License v2.0]

## Gradle Enterprise

[![Revved up by Gradle Enterprise](https://img.shields.io/badge/Revved%20up%20by-Gradle%20Enterprise-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.junit.org/scans)

JUnit 5 utilizes [Gradle Enterprise] for _Build Scans_, _Build Cache_, and _Test Distribution_.

The latest Build Scans are available on [ge.junit.org]. Currently,
only core team members can publish Build Scans and use Test Distribution on that server.
You can, however, publish a Build Scan to [scans.gradle.com] by
using the `--scan` parameter explicitly.

The remote Build Cache is enabled by default for everyone so that local builds can reuse
task outputs from previous CI builds.

## Dependency Metadata

Consult the [Dependency Metadata] section of the [User Guide] for a list of all artifacts
of the JUnit Platform, JUnit Jupiter, and JUnit Vintage.

See also <https://repo1.maven.org/maven2/org/junit/> for releases and
<https://oss.sonatype.org/content/repositories/snapshots/org/junit/> for snapshots.

[Website]: https://junit.org/junit5/
[Codecov]: https://codecov.io/gh/junit-team/junit5
[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/HEAD/CONTRIBUTING.md
[Dependency Metadata]: https://junit.org/junit5/docs/current/user-guide/#dependency-metadata
[Gitter]: https://gitter.im/junit-team/junit5
[Gradle toolchains]: https://docs.gradle.org/current/userguide/toolchains.html
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper
[JaCoCo]: https://www.eclemma.org/jacoco/
[Javadoc]: https://junit.org/junit5/docs/current/api/
[JDK 17]: https://adoptium.net/archive.html?variant=openjdk17&jvmVariant=hotspot
[Release Notes]: https://junit.org/junit5/docs/current/release-notes/
[Samples]: https://github.com/junit-team/junit5-samples
[StackOverflow]: https://stackoverflow.com/questions/tagged/junit5
[User Guide]: https://junit.org/junit5/docs/current/user-guide/
[JUnit Lambda]: https://junit.org/junit4/junit-lambda.html
[crowdfunding campaign on Indiegogo.]: https://junit.org/junit4/junit-lambda-campaign.html
[JUnit 5.8.2]: https://github.com/junit-team/junit5/releases/tag/r5.8.2
[EclipseCodeFormatter]: https://github.com/krasa/EclipseCodeFormatter
[Google Java Style Guide]: https://www.practicesofmastery.com/post/eclipse-google-java-style-guide/
[Full suite of test features]: https://junit.org/junit5/docs/current/user-guide/#writing-tests
[Backwards compatibility with JUnit4]: https://junit.org/junit5/docs/current/user-guide/#migrating-from-junit4
[Compatibility with major IDEs]: https://junit.org/junit5/docs/current/user-guide/#running-tests
[issue tracker]: https://github.com/junit-team/junit5/issues
[Extensions]: https://junit.org/junit5/docs/current/user-guide/#extensions
[Eclipse Public License v2.0]: https://github.com/junit-team/junit5/blob/main/LICENSE.md
[Gradle Enterprise]: https://gradle.com/
[ge.junit.org]: https://ge.junit.org/
[scans.gradle.com]: https://scans.gradle.com/