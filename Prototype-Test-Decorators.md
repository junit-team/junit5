# Writing Extensions for JUnit 5

## Test Decorators

_TODO: provide an introductory discussion of test descriptors._

## Parameter Resolution

If a `@Test`, `@Before`, or `@After` method accepts a parameter, the parameter must be _resolved_ at runtime by a [`MethodParameterResolver`]. A `MethodParameterResolver` can either be built-in or registered by the user. Generally speaking, parameters may be resolved by *type* or by *annotation*. For concrete examples, consult the source code for [`CustomTypeParameterResolver`] and [`CustomAnnotationParameterResolver`], respectively.

## Additional Planned Extension Points

_TODO: discuss additional planned extension points._

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
[`org.junit.gen5.Assertions`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-api/src/main/java/org/junit/gen5/api/Assertions.java
[`SampleTestCase`]: https://github.com/junit-team/junit-lambda/blob/master/sample-project/src/test/java/com/example/SampleTestCase.java
[snapshots repository]: https://oss.sonatype.org/content/repositories/snapshots/
[`TestNameParameterResolver`]: https://github.com/junit-team/junit-lambda/blob/master/junit5-engine/src/main/java/org/junit/gen5/engine/junit5/extension/TestNameParameterResolver.java
[Twitter]: https://twitter.com/junitlambda
