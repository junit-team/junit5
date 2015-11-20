# Writing Test Extensions for JUnit 5

In contrast to the competing `Runner`, `@Rule`, and `@ClassRule` extension points in JUnit 4, the JUnit 5 extension model consists of a single, coherent concept: the `TestExtension` API. Note, however, that `TestExtension` itself is just a marker interface.

Developers can register one or more extensions by annotating a test class or test method with `@ExtendWith(...)`, supplying class references for the extensions to register. For example, to register a custom `MockitoExtension`, you would annotate your test class as follows.

```java
import com.example.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MockTests {
  // ...
}
```

Registered extensions are inherited within test class hierarchies.

## Test Instance Post-processing

[`InstancePostProcessor`] defines the API for `TestExtensions` that
wish to _post process_ test instances.

Common use cases include injecting dependencies into the test instance,
invoking custom initialization methods on the test instance, etc.

For a concrete example, consult the source code for the [`MockitoExtension`].


## Parameter Resolution

`MethodParameterResolver` is a `TestExtension` strategy for dynamically resolving method parameters at runtime.

If a `@Test`, `@BeforeEach`, or `@AfterEach` method accepts a parameter, the parameter must be _resolved_ at runtime by a [`MethodParameterResolver`]. A `MethodParameterResolver` can either be built-in or registered by the user via `@ExtendWith`. Generally speaking, parameters may be resolved by *type* or by *annotation*. For concrete examples, consult the source code for [`CustomTypeParameterResolver`] and [`CustomAnnotationParameterResolver`], respectively.

## Additional Planned Extension Points

As of the time of this writing, `InstancePostProcessor` and `MethodParameterResolver` are the only supported extension points; however, the JUnit Lambda team is planning several additional extension points, including but not limited to the following.

1. BeforeAll / AfterAll callbacks
1. BeforeEach / AfterEach callbacks
1. Dynamic test registration -- for example, for computing parameterized tests at runtime

[CONTRIBUTING]: https://github.com/junit-team/junit-lambda/blob/master/CONTRIBUTING.md
[`CustomAnnotationParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/CustomAnnotationParameterResolver.java
[`CustomTypeParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/CustomTypeParameterResolver.java
[`InstancePostProcessor`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-api/src/main/java/org/junit/gen5/api/extension/InstancePostProcessor.java
[issue tracker]: https://github.com/junit-team/junit-lambda/issues
[junit5-gradle-consumer]: https://github.com/junit-team/junit5-samples/tree/master/junit5-gradle-consumer
[junit5-maven-consumer]: https://github.com/junit-team/junit5-samples/tree/master/junit5-maven-consumer
[junit5-samples]: https://github.com/junit-team/junit5-samples
[`MethodParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-api/src/main/java/org/junit/gen5/api/extension/MethodParameterResolver.java
[`MockitoDecorator`]: https://github.com/junit-team/junit-lambda/blob/master/sample-extension/src/main/java/com/example/mockito/MockitoDecorator.java
[`MockitoDecoratorInBaseClassTest`]: https://github.com/junit-team/junit-lambda/blob/master/sample-extension/src/test/java/com/example/mockito/MockitoDecoratorInBaseClassTest.java
[`MockitoExtension`]: https://github.com/junit-team/junit-lambda/blob/master/sample-extension/src/main/java/com/example/mockito/MockitoExtension.java
[`org.junit.gen5.api`]: https://github.com/junit-team/junit-lambda/tree/master/junit5-api/src/main/java/org/junit/gen5/api
[`org.junit.gen5.Assertions`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-api/src/main/java/org/junit/gen5/api/Assertions.java
[`SampleTestCase`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/SampleTestCase.java
[snapshots repository]: https://oss.sonatype.org/content/repositories/snapshots/
[`TestNameParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-engine/src/main/java/org/junit/gen5/engine/junit5/extension/TestNameParameterResolver.java
[Twitter]: https://twitter.com/junitlambda
