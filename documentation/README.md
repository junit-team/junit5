# JUnit 5 User Guide

This subproject contains the AsciiDoc sources for the JUnit 5 User Guide.

## Structure

- `src/docs/asciidoc`: AsciiDoc files
- `src/test/java`: Java test source code that can be included in the AsciiDoc files
- `src/test/kotlin`: Kotlin test source code that can be included in the AsciiDoc files
- `src/test/resources`: Classpath resources that can be included in the AsciiDoc files or
  used in tests

## Usage

### Generate AsciiDoc

This following Gradle command generates the HTML version of the User Guide as
`build/asciidoc/index.html`.

```
gradlew asciidoctor
```

On Linux operating systems, the `graphviz` package providing `/usr/bin/dot` must be
installed in order to generate the User Guide.
