# JUnit Lambda Prototype

The goal of the prototype phase is to come up with some working code that will entice people to give us feedback on the **programming model, APIs, and SPIs** as described in the sections below. At the current stage, we are **NOT COLLECTING FEEDBACK ABOUT THE IMPLEMENTATION**, simply because it's in large parts neither polished, nor thoroughly tested, nor stable.

We are also not accepting any pull requests at this time, for the following reasons:

- **Focus**: The goal of the prototype is to get feedback on the API and programming model. Focusing on code style, formatting, and other details will distract the community's (and our) attention. A lot of the code in the prototype will potentially be rewritten anyway.
- **Legal reasons**: Any contributor will have to sign a contributor's agreement as outlined in the [CONTRIBUTING] guidelines. The exact details have not been figured out yet, but we will contact you *before accepting your first pull request*.

If you want to provide input in the interim, please use the project's [issue tracker] or send us comments via [Twitter].

----

# Installation

Snapshot artifacts are deployed to Sonatype's [snapshots repository].

## Dependency Metadata

- **Group ID**: `org.junit.prototype`
- **Version**: `5.0.0-SNAPSHOT`
- **Artifact IDs**:
  - `junit-commons`
  - `junit-console`
  - `junit-engine-api`
  - `junit-launcher`
  - `junit4-engine`
  - `junit4-launcher-runner`
  - `junit5-api`
  - `junit5-engine`
  - `open-test-alliance`

See also: <https://oss.sonatype.org/content/repositories/snapshots/org/junit/prototype/>

## JUnit 5 Sample Projects

You can find a collection of sample projects based on the JUnit 5 prototype in the [junit5-samples] repository. You'll find the respective `build.gradle`
and `pom.xml` in the projects below:

- For Gradle, check out the [junit5-gradle-consumer] project.
- For Maven, check out the [junit5-maven-consumer] project.

----

# Running JUnit 5 Test

[How to run tests in JUnit5?](Prototype-Running-Tests)

----

# Writing JUnit 5 Test Cases

[How to write test cases in JUnit5?](Prototype-Writing-Test-Cases)

----

# Extending JUnit 5 with Test Decorators

[How to extend JUnit5?](Prototype-Test-Decorators)

----

# Integrating JUnit 4 Test Suites

[How to run - and migrate - your JUnit4 tests with JUnit5?](Prototype-JUnit4-Run-And-Migrate)

----

# Programmatically Discover and Launch JUnit 5 Tests

[The page on the JUnit5 Launcher API](Prototype-Launcher-API) is primarily targeted at IDE and build tool providers.

There is also a short paragraph on [how to plug in other test enginges into the launcher](Prototype-Launcher-API#plug-in-engine)

----

# The Open Test Alliance

We have the idea to build a [very minimal common foundation for testing libraries], so that third-party libraries (like Hamcrest and AssertJ) can be used with any of those libraries.

For the time being we suggest a small hierarchy of exceptions to be used. Check out the [open-test-alliance] project.


----

[CONTRIBUTING]: https://github.com/junit-team/junit-lambda/blob/master/CONTRIBUTING.md
[`CustomAnnotationParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/CustomAnnotationParameterResolver.java
[`CustomTypeParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/CustomTypeParameterResolver.java
[issue tracker]: https://github.com/junit-team/junit-lambda/issues
[junit5-gradle-consumer]: https://github.com/junit-team/junit5-samples/tree/master/junit5-gradle-consumer
[junit5-maven-consumer]: https://github.com/junit-team/junit5-samples/tree/master/junit5-maven-consumer
[junit5-samples]: https://github.com/junit-team/junit5-samples
[`MethodParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-api/src/main/java/org/junit/gen5/api/extension/MethodParameterResolver.java
[`MockitoDecorator`]: https://github.com/junit-team/junit-lambda/blob/master/sample-extension/src/main/java/com/example/mockito/MockitoDecorator.java
[`MockitoDecoratorInBaseClassTest`]: https://github.com/junit-team/junit-lambda/blob/master/sample-extension/src/test/java/com/example/mockito/MockitoDecoratorInBaseClassTest.java
[`org.junit.gen5.api`]: https://github.com/junit-team/junit-lambda/tree/master/junit5-api/src/main/java/org/junit/gen5/api
[`SampleTestCase`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/SampleTestCase.java
[snapshots repository]: https://oss.sonatype.org/content/repositories/snapshots/
[`TestNameParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-engine/src/main/java/org/junit/gen5/engine/junit5/extension/TestNameParameterResolver.java
[Twitter]: https://twitter.com/junitlambda
