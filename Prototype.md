# JUnit Lambda Prototype

The goal of the prototype phase is to come up with some working code that will entice people to give us feedback on the **programming model, APIs, and SPIs** as described in the sections below. At the current stage, we are **NOT COLLECTING FEEDBACK ABOUT THE IMPLEMENTATION**, simply because it's in large parts neither polished, nor thoroughly tested, nor stable.

We are also not accepting any pull requests at this time, for the following reasons:

- **Focus**: The goal of the prototype is to get feedback on the API and programming model. Focusing on code style, formatting, and other details will distract the community's (and our) attention. A lot of the code in the prototype will potentially be rewritten anyway.
- **Legal reasons**: Any contributor will have to sign a contributor's agreement along the lines of [Contributing.md](https://github.com/junit-team/junit-lambda/blob/master/CONTRIBUTING.md). The exact details have not been figured out yet, but we will contact you *before accepting your first pull request*.

If you want to provide input in the interim, please use [the project's issue tracker](https://github.com/junit-team/junit-lambda/issues) or send us comments via [Twitter](https://twitter.com/junitlambda).

## Installation

Snapshot artifacts are deployed to [Sonatype's snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/).

## JUnit 5 Sample Projects

You can find a collection of sample projects based on the JUnit 5 prototype in the [junit5-samples](https://github.com/junit-team/junit5-samples) repository.

For Gradle, check out the [junit5-gradle-consumer](https://github.com/junit-team/junit5-samples/tree/master/junit5-gradle-consumer) project.

For Maven, check out the [junit5-maven-consumer](https://github.com/junit-team/junit5-samples/tree/master/junit5-maven-consumer) project.

## Running JUnit 5 test cases

## Writing JUnit 5 test cases

JUnit 5 supports the familiar @Test, @Before, and @After annotations.

TODO: 
* expand on differences, explain why @Test takes no arguments 
* explain new annotations


In all prior JUnit versions test methods were required not to have parameters.
As one of the major changes in JUnit 5 methods are now allowed to have parameters allowing for greater flexibility. 
If there is a method parameter it needs to be _resolved_ at runtime. This is achieved by `MethodParameterResolver` instances. Such `MethodParameterResolver`s can either be built-in or added by the user (see extension model).
Generally, parameters may be resolved by type or by annotations. 

For a very simple example see the `@TestName` annotation. It must be declared on a parameter of type String and will hold the name of the test at runtime (either its canonical name or its user-provided `@Name`). 
This acts as a simple replacement of the old `TestName` rule.

The MockitoDecorator is another example of a `MethodParameterResolver`. 
While not intended to be production-ready it demonstrates the simplicity and expressives of both
the extension model and the parameter resolution process.






## Extending JUnit 5's built-in behavior

## Launching JUnit Lambda (for IDE and build tool providers)

## Integrating JUnit 4 test suites

## The Open Test Alliance