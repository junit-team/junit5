# Contributing

## Project License:  Eclipse Public License v1.0

- You will only Submit Contributions where You have authored 100% of the content.
- You will only Submit Contributions to which You have the necessary rights. This means that if You are employed You have received the necessary permissions from Your employer to make the Contributions.
- Whatever content You Contribute will be provided under the Project License(s).


---

## Coding Conventions

### Formatting

Code formatting is enforced using the [Spotless](https://github.com/diffplug/spotless) Gradle plugin. You can use `gradle spotlessApply` to format new code and add missing license headers to source files. Formatter settings for Eclipse are available [in the repository](src/eclipse/junit-eclipse-formatter-settings.xml). For IntelliJ IDEA there's a [plugin](https://plugins.jetbrains.com/plugin/6546) you can use.

### Tests

#### Naming

- All test classes must end with a `Tests` suffix.
- Example test classes that should not be picked up by the build must end with a `TestCase` suffix.

#### Assertions

- Use `org.junit.gen5.api.Assertions` wherever possible.
- Use AssertJ when richer assertions are needed.
- Do not use `org.junit.Assert` or `junit.framework.Assert`.

#### Mocking

- Use either Mockito or hand-written test doubles.

### Logging

- Use sparingly
- Do not log in utility classes (junit-commons)
- Levels
  - `SEVERE` (Log4J: `ERROR`): extra information (in addition to an Exception) about errors that will halt execution
  - `WARNING` (Log4J: `WARN`): potential usage errors that should not halt execution
  - `INFO`: stuff the users might want to know but not by default (Example: `ServiceLoaderTestEngineRegistry` logs IDs of discovered engines)
  - `FINE` (Log4J: `DEBUG`)
  - `FINER` (Log4J: `TRACE`)
