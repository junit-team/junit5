# The JUnit5 Launcher API

One of the prominent goals of JUnit 5 is to make the interface between JUnit
and its programmatic clients - build tools and IDEs - more powerful and stable.
The purpose is to decouple the internals of discovering and executing tests
from all the filtering and configuration that's necessary from the outside.

For the prototype we came up with the concept of a launcher object that
can be used to discover, filter and execute JUnit tests. Moreover, we
added a mechanism to allow third party test libraries - like Spock, Cucumber
and Fitnesses - to plug into JUnit 5's launching infrastructure.

The launching API is in project [junit-launcher](https://github.com/junit-team/junit-lambda/tree/master/junit-launcher).

A sample user of the launching API is our [Console Runner](https://github.com/junit-team/junit-lambda/blob/master/junit-console/src/main/java/org/junit/gen5/console/ConsoleRunner.java)
in project [junit-console](https://github.com/junit-team/junit-lambda/tree/master/junit-console).

## Discovering Tests

Introducing test discovery as a dedicated feature of JUnit itself will (hopefully)
free IDEs and build tools from most of the difficulties they had to go through
to identify test classes and test methods.

Usage Example:

```
import static TestPlanSpecification.*;

TestPlanSpecification specification = build(
    forPackage("com.mycompany.mytests"),
    forClass(MyTestClass.class)
).filterWith(classNameMatches("*Test").or(classNameMatches("Test*")));

TestPlan plan = new Launcher().discover(specification);
```
There's currently the possibility to search for classes, methods,
all classes in a package or even all tests in the classpath. Discovery
takes place across all participating test engines.

The resulting test plan is basically a hierarchical (and read-only)
description of all engines, classes and test methods that fit
the `specification` object. The client can traverse the tree, retrieve
details about a node and get a link to the original source (like class,
method or file position). Every node in the test plan tree has a
unique ID that can be used to invoke a particular test or group of
tests.

## Running Tests

There's two ways of executing tests. Clients can either use the same
test specification object as in the discovery phase, or - to speed
things up a bit - hand in the ready test plan object from a previous
discover step. Test progress and result reporting is all done
through a [TestPlanExecutionListener](https://github.com/junit-team/junit-lambda/blob/master/junit-launcher/src/main/java/org/junit/gen5/launcher/TestPlanExecutionListener.java):

```
TestPlanSpecification specification = build(
    forPackage("com.mycompany.mytests"),
    forClass(MyTestClass.class)
).filterWith(classNameMatches("*Test").or(classNameMatches("Test*")));

Launcher launcher = new Launcher();
TestPlanExecutionListener listener = createListener();
launcher.registerTestPlanExecutionListener(listener);

launcher.execute(specification);
```

There's currently no result object, but you can easily use
a listener to aggregate the final results in an object of your own.
For an example see [TestSummaryReportingTestListener](https://github.com/junit-team/junit-lambda/blob/master/junit-console/src/main/java/org/junit/gen5/console/TestSummaryReportingTestListener.java).


## Plugging in Your Own Test Engine

Other test engines have to follow the interfaces in [junit-engine-api](https://github.com/junit-team/junit-lambda/tree/master/junit-engine-api).
Currently we have two engine implementations:

- [junit5-engine](https://github.com/junit-team/junit-lambda/tree/master/junit5-engine): The core of the current prototype.
- [junit4-engine](https://github.com/junit-team/junit-lambda/tree/master/junit4-engine): A thin layer on top of JUnit 4 to allow running "old" tests with the launcher infrastructure.
