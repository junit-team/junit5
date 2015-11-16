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

# Running JUnit 5 Test Cases

----

# Writing JUnit 5 Test Cases

## Annotations

JUnit 5 supports the following annotations for configuring tests and extending the framework.

All core annotations are located in the [`org.junit.gen5.api`] package in the `junit5-api` module.

| Annotation | Description |
|------------|-------------|
| **`@Test`** | Denotes that a method is a test method. Unlike JUnit 4's `@Test` annotation, this annotation does not declare any attributes, since test decorators in JUnit 5 operate based on their own dedicated annotations. |
| **`@Name`** | Declares a custom display name for the test class or test method |
| **`@TestName`** | Allows the display name of the current test to be supplied as a method parameter to `@Test`, `@Before`, and `@After` methods; analogous to the JUnit 4's `TestName` rule |
| **`@Before`** | Denotes that the annotated method should be executed _before_ **each** `@Test` method in the current class or class hierarchy |
| **`@After`** | Denotes that the annotated method should be executed _after_ **each** `@Test` method in the current class or class hierarchy |
| **`@BeforeAll`** | Denotes that the annotated method should be executed _before_ **all** `@Test` methods in the current class or class hierarchy; analogous to JUnit 4's `@BeforeClass` |
| **`@AfterAll`** | Denotes that the annotated method should be executed _after_ **all** `@Test` methods in the current class or class hierarchy; analogous to JUnit 4's `@AfterClass` |
| **`@Context`** | Denotes that the annotated class is an inner test class |
| **`@Tag`** and **`@Tags`** | Used to declare _tags_ for filtering tests, either at the class or method level; analogous to test groups in TestNG or Categories in JUnit 4 |
| **`@Conditional`** | Used to declare _conditions_ that will be evaluated to determine if a test is enabled. `@Disabled` is a built-in implementation of conditional test execution. |
| **`@Disabled`** | Used to _disable_ a test class or test method; analogous to JUnit 4's `@Ignore` |
| **`@TestDecorators`** | Used to register custom extensions and decorators for tests such as `MethodParameterResolver` |

## Examples

## Method Parameters

In all prior JUnit versions, `@Test`, `@Before`, and `@After` methods were not allowed to have parameters (at least not with the standard `Runner` implementations). As one of the major changes in JUnit 5, methods are now permitted to have parameters allowing for greater flexibility. If there is a method parameter, it needs to be _resolved_ at runtime by a [`MethodParameterResolver`]. A `MethodParameterResolver` can either be built-in or registered by the user (see the extension model for further details). Generally speaking, parameters may be resolved by *type* or by *annotation*. For concrete examples, consult the source code for [`CustomTypeParameterResolver`] and [`CustomAnnotationParameterResolver`], respectively.

For a very simple yet useful example, consider the support for `@TestName`. If a method parameter is of type `String` and annotated with `@TestName`, the [`TestNameParameterResolver`] will supply the _display name_ of the current test at runtime (either its canonical name or its user-provided `@Name`). This acts as a drop-in replacement for the `TestName` rule from JUnit 4.

Check out the `methodInjectionTest(...)` test method in [`SampleTestCase`] for an example that uses the built-in `TestNameParameterResolver` as well as the aforementioned custom resolvers, `CustomTypeParameterResolver` and `CustomAnnotationParameterResolver`.

The [`MockitoDecorator`] is another example of a `MethodParameterResolver`. 
While not intended to be production-ready, it demonstrates the simplicity and expressiveness of both the extension model and the parameter resolution process. Check out the source code for [`MockitoDecoratorInBaseClassTest`] for an example of injecting Mockito mocks into `@Before` and `@Test` methods.

----

# Extending JUnit 5's Built-in Behavior

----

# Launching JUnit Lambda

This section is primarily intended for IDE and build tool providers.

----

# Integrating JUnit 4 Test Suites

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
[`MockitoDecoratorInBaseClassTest`]: https://github.com/junit-team/junit-lambda/blob/master/sample-extension/src/test/java/com/example/mockito/MockitoDecoratorInBaseClassTest.java
[`org.junit.gen5.api`]: https://github.com/junit-team/junit-lambda/tree/master/junit5-api/src/main/java/org/junit/gen5/api
[`SampleTestCase`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/SampleTestCase.java
[snapshots repository]: https://oss.sonatype.org/content/repositories/snapshots/
[`TestNameParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-engine/src/main/java/org/junit/gen5/engine/junit5/extension/TestNameParameterResolver.java
[Twitter]: https://twitter.com/junitlambda
