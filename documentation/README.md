# JUnit 5 User Guide

This subproject contains the AsciiDoc sources for the JUnit 5 User Guide.

## Structure

- `src/docs/asciidoc`: AsciiDoc files
- `src/test/java`: Test source code that can be included in the AsciiDoc files
- `src/main/java`: Example source code that can be included in the AsciiDoc files

## Usage

### Generate AsciiDoc

On linux, `graphviz` package providing `/usr/bin/dot` needs to be installed.

```
gradle asciidoctor
```

This task generates HTML files into `build/asciidoc`.
