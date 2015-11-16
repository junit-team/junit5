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

----

# JUnit 5 Sample Projects

You can find a collection of sample projects based on the JUnit 5 prototype in the [junit5-samples] repository.

For Gradle, check out the [junit5-gradle-consumer] project.

For Maven, check out the [junit5-maven-consumer] project.

----

# Running JUnit 5 test cases

----

# Writing JUnit 5 test cases

JUnit 5 supports the familiar `@Test`, `@Before`, and `@After` annotations.

TODO:

- expand on differences
  - explain why @Test takes no arguments
- explain new annotations

## Method Parameters

In all prior JUnit versions, test methods were not allowed to have parameters (at least with the standard `Runner` implementations). As one of the major changes in JUnit 5, methods are now permitted to have parameters allowing for greater flexibility. If there is a method parameter, it needs to be _resolved_ at runtime by a [`MethodParameterResolver`]. A `MethodParameterResolver` can either be built-in or registered by the user (see the extension model for further details). Generally speaking, parameters may be resolved by *type* or by *annotation*. For concrete examples, consult the source code for [`CustomTypeParameterResolver`] and [`CustomAnnotationParameterResolver`], respectively.

For a very simple yet useful example, see the `@TestName` annotation. It must be declared on a method parameter of type `String` and will hold the name of the test at runtime (either its canonical name or its user-provided `@Name`). This acts as a drop-in replacement for the `TestName` rule from JUnit 4.

The [`MockitoDecorator`] is another example of a `MethodParameterResolver`. 
While not intended to be production-ready, it demonstrates the simplicity and expressiveness of both the extension model and the parameter resolution process.

Note that the method parameter resolution process in JUnit 5 is similar to the one used in Spring MVC controller methods.

----

# Extending JUnit 5's built-in behavior

----

# Launching JUnit Lambda (for IDE and build tool providers)

----

# Integrating JUnit 4 test suites

----

# The Open Test Alliance

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
[snapshots repository]: https://oss.sonatype.org/content/repositories/snapshots/
[Twitter]: https://twitter.com/junitlambda
