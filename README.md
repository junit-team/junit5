# junit-lambda

##Stuff to review and consider

- [LambdaSpec](https://github.com/ktoso/lambda-spec): RSpec like testing with Java lambdas.
  - [Article on the topic](http://www.blog.project13.pl/index.php/coding/1830/proof-of-concept-lambdaspec-how-testing-will-look-with-java8/)
- [Jumi](http://jumi.fi/): Parallel test execution by default. Backwards compatible to JUnit.
- [Hierarchical Context Runner](https://github.com/bechte/junit-hierarchicalcontextrunner): Runner for JUnit by Stefan Bechtold.
  - [Hierarchical testing with @Context](https://github.com/bechte/JUT/blob/master/src/test/java/de/bechte/jut/core/TestResultTest.java): Also from Stefan.
- [Exception testing with Lambdas - 1](http://www.codeaffine.com/2014/07/28/clean-junit-throwable-tests-with-java-8-lambdas/)
- [Exception testing with Lambdas - 2](http://blog.jooq.org/2014/05/23/java-8-friday-better-exceptions/)
- [Combining matchers, streams and lambdas](http://blog.jooq.org/2014/05/30/java-8-friday-most-internal-dsls-are-outdated/)
- [JUnit Java8 Runner](https://github.com/motlin/JUnit-Java-8-Runner): Allowing test methods in interfaces.
- [JUnit Quickcheck](https://github.com/pholser/junit-quickcheck): A quickcheck reimplementation in Java building on JUnit theories
- [Service Provider Interface](http://en.wikipedia.org/wiki/Service_provider_interface)

##Our Proposals

###Proposal 1 : Using initialization blocks and lambdas to define tests

- [Examples](https://github.com/junit-team/junit-lambda/blob/master/src/test/java/org/junit/lambda/proposal01)

