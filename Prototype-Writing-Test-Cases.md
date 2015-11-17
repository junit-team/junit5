# Writing JUnit 5 Test Cases

**Table of Contents**

- [Annotations](#annotations)
  - [Meta-Annotations and Composed Annotations](meta-annotations-and-composed-annotations)
- [Standard Test Class](#standard-test-class)
- [Custom Names](#custom-names)
- [Assertions](#assertions)
- [Disabling Tests](#disabling-tests)
- [Tagging and Filtering](#tagging-and-filtering)
- [Test Contexts](#test-contexts)
- [Method Parameters and Dependency Injection](#method-parameters-and-dependency-injection)

----

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
| **`@TestDecorators`** | Used to register custom extensions and decorators for tests such as `MethodParameterResolver`. See [the page on test decorators](Prototype-Test-Decorators) |

### Meta-Annotations and Composed Annotations

JUnit 5 annotations can be used as _meta-annotations_. That means that you can define your own
_composed annotation_ that will automatically _inherit_ the semantics of its meta-annotations.

For example, instead of copying and pasting `@Tag("fast")` throughout your code base (see [Tagging and Filtering](#tagging-and-filtering)), you can create a custom _composed annotation_ named `@Fast` as
follows. `@Fast` can then be used as a drop-in replacement for `@Tag("fast")`.

```java
import org.junit.gen5.api.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Tag("fast")
public @interface Fast {
}
```

----

## Standard Test Class

```java
import org.junit.gen5.api.*;

class MyTest {

  @BeforeAll
  void initAll() {}

  @Before
  void init() {}

  @Test
  void succeedingTest() {}

  @After
  void tearDown() {}

  @AfterAll
  void tearDownAll() {}

}
```

Notice that neither the test class nor the test method need to be `public`.
Also, `@BeforeAll` and `@AfterAll` can be used on `static` or non-static methods.

----

## Custom Names

Test classes and test methods can declare a custom name -- with spaces, special characters,
and even emojis -- that will be displayed by test runners and test reporting:

```java
import org.junit.gen5.api.*;

@Name("A special test case")
class CanHaveAnyNameTest {

  @Test
  @Name("A nice name, isn't it?")
  void testWithANiceName() {}

}
```

----

## Assertions

JUnit 5 comes with many of the assertion methods that JUnit 4 has and adds a few
that lend themselves well to being used with Java 8 lambdas. All JUnit 5 assertions
are static methods in the [`org.junit.gen5.Assertions`] class.

```java
import static org.junit.gen5.api.Assertions.*;

import org.junit.gen5.api.*;

class MyTest {

  @Test
  void standardAssertions() {
    assertEquals(2, 2);
    assertEquals(4, 4, "The optional assertion message is now the last parameter.");
    assertTrue(2 == 2, () -> "Assertion messages can be lazily evaluated -- " +
                             "to avoid constructing complex messages unnecessarily.");
  }

  @Test
  void groupedAssertions() {
    // In a grouped assertion all assertions are executed, and any
    // failures will be reported together.
    assertAll("address",
      () -> assertEquals("Johannes", address.getFirstName()),
      () -> assertEquals("Link", address.getLastName())
    );
  }

  @Test
  void exceptionTesting() {
    Throwable exception = expectThrows(IllegalArgumentException.class,
      () -> throw new IllegalArgumentException("a message")
    );
    assertEquals("a message", exception.getMessage());
  }

}
```

----

## Disabling Tests

Here's a disabled test case:

```java
import org.junit.gen5.api.*;

@Disabled
class MyTest {

  @Test
  void testWillBeSkipped() {}

}
```

And here's a test case with a disabled test method:

```java
import org.junit.gen5.api.*;

class MyTest {

  @Disabled
  @Test
  void testWillBeSkipped() {}

  @Test
  void testWillBeExecuted() {}

}
```

----

## Tagging and Filtering

Test classes and methods can be tagged. Those tags can later be used to filter
[test discovery and execution](Prototype-Running-Tests):

```java
import org.junit.gen5.api.*;

@Tag("fast")
@Tag("model")
class FastModelTests {

  @Test
  @Tag("taxes")
  void testingTaxCalculation() {}

}
```

----

## Test Contexts

Test contexts give the test writer more capabilities to express the relationship
among several group of tests. Here's an example:

```java
import org.junit.gen5.api.*;

class MyObjectTest {

  MyObject myObject;

  @Before
  void init() {
    myObject = new MyObject();
  }

  @Test
  void testEmptyObject() {}

  @Context
  class WithChildren() {

    @Before
    void initWithChildren() {
      myObject.addChild(new MyObject());
      myObject.addChild(new MyObject());
    }

    @Test
    void testObjectWithChildren() {}

  }

}
```

Notice that _only non-static inner classes_ can serve as contexts.
Contexts can be arbitrarily nested and can be considered to be full members of
the test class family.

----

## Method Parameters and Dependency Injection

In all prior JUnit versions, `@Test`, `@Before`, and `@After` methods were not allowed to have parameters (at least not with the standard `Runner` implementations). As one of the major changes in JUnit 5, methods are now permitted to have parameters allowing for greater flexibility and enabling method-level _Dependency Injection_.

There are a few built-in resolvers in the prototype that need not be explicitly enabled:

- `@TestName`: If a method parameter is of type `String` and annotated with `@TestName`, the [`TestNameParameterResolver`] will supply the _display name_ of the current test at runtime (either its canonical name or its user-provided `@Name`). This acts as a drop-in replacement for the `TestName` rule from JUnit 4:

  ```java
  import org.junit.gen5.api.*;

  class MyTest {

    @Before
    void init(@TestName name) {
      assertTrue(name.equals("TEST 1") || name.equals("test2"));
    }

    @Test
    @Name("TEST 1")
    void test1(@TestName name) {
      assertEquals("TEST 1", name);
    }

    @Test
    void test2() {}

  }
  ```

All other parameter resolvers must be explicitly enabled by registering a [test decorator](Prototype-Test-Decorators) via `@TestDecorators`.

-  Check out the `methodInjectionTest(...)` test method in [`SampleTestCase`] for an example that uses the built-in `TestNameParameterResolver` as well as the aforementioned custom resolvers, `CustomTypeParameterResolver` and `CustomAnnotationParameterResolver`.

-  The [`MockitoDecorator`] is another example of a `MethodParameterResolver`. While not intended to be production-ready, it demonstrates the simplicity and expressiveness of both the extension model and the parameter resolution process. Check out the source code for [`MockitoDecoratorInBaseClassTest`] for an example of injecting Mockito mocks into `@Before` and `@Test` methods:

  ```java
  import org.junit.gen5.api.*;

  import static org.mockito.Mockito.when;
  import com.example.mockito.MockitoDecorator;
  
  @TestDecorators(MockitoDecorator.class)
  class MyMockitoTest {

    @Before
    void init(@InjectMock MyType myType) {
      when(myType.getName()).thenReturn("hello");
    }

    @Test
    void simpleTestWithInjectedMock(@InjectMock MyType myType) {
      assertEquals("hello", myType.getName());
    }

  }
  ```

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
