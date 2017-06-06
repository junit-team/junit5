# JUnit 5 User Guide

This subproject contains the AsciiDoc sources for the JUnit 5 User Guide.

## Structure

- `src/docs/asciidoc`: AsciiDoc files
- `src/test/java`: Test source code that can be included in the AsciiDoc files
- `src/main/java`: Example source code that can be included in the AsciiDoc files

## Usage

### Generate AsciiDoc

```
gradle asciidoctor
```

This task generates HTML files into `build/asciidoc/html5`.

### Running example tests and demos

The example tests included with the documentation can be run via the console,
Gradle or Maven as described below.  Note that the instructions below assume
that you're in the documentation directory.

To run the example tests from a terminal on Linux or Mac OSX, issue the following
command at the prompt:

```
./run-example-tests-via-console.sh
```

To run the example tests from a console in Microsoft Windows, issue the following
command at the prompt:

```
TODO
```

To run the example tests using the Gradle build system, issue the following
command at the prompt:

```
../gradlew junitPlatformTest
```

To run the example tests using the Maven build system, issue the following
command at the prompt:

```
mvn test
```
