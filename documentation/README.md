# JUnit 5 User Guide

This subproject contains the AsciiDoc sources for the future JUnit 5 User Guide.

## Structure

- `src/docs/asciidoc`: AsciiDoc files
- `src/docs/static`: Static files for the `gh-pages` branch
- `src/test/java`: Test source code that can be included in the AsciiDoc files
- `src/main/java`: Example source code that can be included in the AsciiDoc files

## Usage

### Generate AsciiDoc

```
gradle asciidoctor
```

This task generates HTML files into `build/asciidoc/html5`.

### Publish it to GitHub Pages

```
gradle publishGhPages
```

This task requires setting the Gradle property `githubToken`, e.g. in `~/.gradle/gradle.properties`, to a GitHub [personal access token](https://github.com/settings/tokens) that includes the `public_repo` scope.

When successful the uploaded files are available at:
<http://junit-team.github.io/junit-lambda/>
