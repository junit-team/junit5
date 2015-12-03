# Writing Test Extensions for JUnit 5

**Table of Contents**

- [Overview](#overview)
- [Registering Extensions](#registering-extensions)
- [Conditional Test Execution](#conditional-test-execution)
- [Test Instance Post-processing](#test-instance-post-processing)
- [Parameter Resolution](#parameter-resolution)
- [Test Lifecycle Callbacks](#test-lifecycle-callbacks)
- [Additional Planned Extension Points](#additional-planned-extension-points)

## Overview

In contrast to the competing `Runner`, `@Rule`, and `@ClassRule` extension points in JUnit 4, the JUnit 5 extension model consists of a single, coherent concept: the `TestExtension` API. Note, however, that `TestExtension` itself is just a marker interface.

## Registering Extensions

Developers can register one or more extensions by annotating a test class or test method with `@ExtendWith(...)`, supplying class references for the extensions to register. For example, to register a custom `MockitoExtension`, you would annotate your test class as follows.

```java
@ExtendWith(MockitoExtension.class)
class MockTests {
  // ...
}
```

Multiple extensions can be registered together like this:

```java
@ExtendWith({ FooExtension.class, BarExtension.class })
class MyTestsV1 {
  // ...
}
```

As an alternative, multiple extensions can be registered separately like this:

```java
@ExtendWith(FooExtension.class)
@ExtendWith(BarExtension.class)
class MyTestsV2 {
  // ...
}
```

The execution of tests in both `MyTestsV1` and `MyTestsV2` will be extended by the `FooExtension` and `BarExtension`, in exactly that order.

Registered extensions are inherited within test class hierarchies.

## Conditional Test Execution

[`Condition`] defines the `TestExtension` API for programmatic, _conditional test execution_.

A `Condition` is _evaluated_ to determine if a given test (e.g., class or method) should
be executed based on the supplied `TestExecutionContext`. When evaluated at the class
level, a `Condition` applies to all test methods within that class.

See the source code of [`DisabledCondition`] and [`@Disabled`] for a concrete example.

## Test Instance Post-processing

[`InstancePostProcessor`] defines the API for `TestExtensions` that
wish to _post process_ test instances.

Common use cases include injecting dependencies into the test instance,
invoking custom initialization methods on the test instance, etc.

For concrete examples, consult the source code for [`MockitoExtension`]
and [`SpringExtension`].

## Parameter Resolution

[`MethodParameterResolver`] is a `TestExtension` strategy for dynamically resolving
method parameters at runtime.

If a `@Test`, `@BeforeEach`, or `@AfterEach` method accepts a parameter, the parameter
must be _resolved_ at runtime by a `MethodParameterResolver`. A `MethodParameterResolver`
can either be built-in (see [`TestNameParameterResolver`]) or registered by the user via
`@ExtendWith`. Generally speaking, parameters may be resolved by *type* or by *annotation*.
For concrete examples, consult the source code for [`CustomTypeParameterResolver`] and 
[`CustomAnnotationParameterResolver`], respectively.

## Test Lifecycle Callbacks

The following interfaces define the APIs for extending tests at various points in the
test execution lifecycle. Consult the Javadoc for each of these in the
[`org.junit.gen5.api.extension`] package.

- `BeforeEachCallbacks`
- `AfterEachCallbacks`
- `BeforeAllCallbacks`
- `AfterAllCallbacks`

Note that extension developers may choose to implement any number of these
interfaces within a single extension. Consult the source code of the
[`SpringExtension`] for a concrete example.

## Additional Planned Extension Points

The JUnit Lambda team is planning several additional extension points, including but not limited to the following.

- Dynamic test registration -- for example, for computing parameterized tests at runtime


[`Condition`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/junit5-api/src/main/java/org/junit/gen5/api/extension/Condition.java
[`CustomAnnotationParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/sample-project/src/test/java/com/example/CustomAnnotationParameterResolver.java
[`CustomTypeParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/sample-project/src/test/java/com/example/CustomTypeParameterResolver.java
[`@Disabled`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/junit5-api/src/main/java/org/junit/gen5/api/Disabled.java
[`DisabledCondition`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/junit5-engine/src/main/java/org/junit/gen5/engine/junit5/extension/DisabledCondition.java
[`InstancePostProcessor`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/junit5-api/src/main/java/org/junit/gen5/api/extension/InstancePostProcessor.java
[issue tracker]: https://github.com/junit-team/junit-lambda/issues
[junit5-gradle-consumer]: https://github.com/junit-team/junit5-samples/tree/prototype-1/junit5-gradle-consumer
[junit5-maven-consumer]: https://github.com/junit-team/junit5-samples/tree/prototype-1/junit5-maven-consumer
[junit5-samples]: https://github.com/junit-team/junit5-samples
[`MethodParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/junit5-api/src/main/java/org/junit/gen5/api/extension/MethodParameterResolver.java
[`MockitoExtension`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/sample-extension/src/main/java/com/example/mockito/MockitoExtension.java
[`org.junit.gen5.api`]: https://github.com/junit-team/junit-lambda/tree/prototype-1/junit5-api/src/main/java/org/junit/gen5/api
[`org.junit.gen5.api.extension`]: https://github.com/junit-team/junit-lambda/tree/prototype-1/junit5-api/src/main/java/org/junit/gen5/api/extension
[`SampleTestCase`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/sample-project/src/test/java/com/example/SampleTestCase.java
[snapshots repository]: https://oss.sonatype.org/content/repositories/snapshots/
[`SpringExtension`]: https://github.com/sbrannen/spring-test-junit5/blob/prototype-1/src/main/java/org/springframework/test/context/junit5/SpringExtension.java
[`TestNameParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/prototype-1/junit5-engine/src/main/java/org/junit/gen5/engine/junit5/extension/TestNameParameterResolver.java
[Twitter]: https://twitter.com/junitlambda
