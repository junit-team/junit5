= JUnit Lambda Prototype

The goal of the prototype phase is to come up with some working code that will entice people to give us feedback on the *programming model, APIs, and SPIs* as described in the sections below. At the current stage, we are *NOT COLLECTING FEEDBACK ABOUT THE IMPLEMENTATION*, simply because it's in large parts neither polished, nor thoroughly tested, nor stable.

We are also not accepting any pull requests at this time, for the following reasons:

* *Focus*: The goal of the prototype is to get feedback on the API and programming model. Focusing on code style, formatting, and other details will distract the community's (and our) attention. A lot of the code in the prototype will potentially be rewritten anyway.
* *Legal reasons*: Any contributor will have to sign a contributor's agreement as outlined in the https://github.com/junit-team/junit-lambda/blob/prototype-1/CONTRIBUTING.md[CONTRIBUTING] guidelines. The exact details have not been figured out yet, but we will contact you _before accepting your first pull request_.

If you want to provide input in the interim, please use the project's https://github.com/junit-team/junit-lambda/issues[issue tracker] or send us comments via https://twitter.com/junitlambda[Twitter].

—-

= Supported Java Versions

JUnit 5 only supports Java 8 and above. However, you can still test classes compiled with lower versions.

= Installation

Snapshot artifacts are deployed to Sonatype's https://oss.sonatype.org/content/repositories/snapshots/[snapshots repository].

== Dependency Metadata

* *Group ID*: `org.junit.prototype`
* *Version*: `5.0.0-SNAPSHOT`
* *Artifact IDs*:
** `junit-commons`
** `junit-console`
** `junit-engine-api`
** `junit-gradle`
** `junit-launcher`
** `junit4-engine`
** `junit4-launcher-runner`
** `junit5-api`
** `junit5-engine`
** `open-test-alliance` (Version `1.0.0-SNAPSHOT`)
** `surefire-junit5`

See also: https://oss.sonatype.org/content/repositories/snapshots/org/junit/prototype/[https://oss.sonatype.org/content/repositories/snapshots/org/junit/prototype/]

== JUnit 5 Sample Projects

You can find a collection of sample projects based on the JUnit 5 prototype in the https://github.com/junit-team/junit5-samples[junit5-samples] repository. You'll find the respective `build.gradle`
and `pom.xml` in the projects below:

* For Gradle, check out the https://github.com/junit-team/junit5-samples/tree/prototype-1/junit5-gradle-consumer[junit5-gradle-consumer] project.
* For Maven, check out the https://github.com/junit-team/junit5-samples/tree/prototype-1/junit5-maven-consumer[junit5-maven-consumer] project.
'''

= Writing JUnit 5 Test Cases

link:Prototype-Writing-Test-Cases[How to write test cases in JUnit 5?]

'''

= Running JUnit 5 Tests

link:Prototype-Running-Tests[How to run tests in JUnit 5?]

'''

= Extending JUnit 5

link:Prototype-Test-Extensions[How to extend JUnit 5?]

'''

= From JUnit 4 to 5: Integration and Migration

link:Prototype-JUnit4-Run-And-Migrate[How to run existing JUnit 4 tests with JUnit5 and migrate from JUnit 4 to JUnit 5?]

'''

= Programmatically Discover and Launch JUnit 5 Tests

The link:Prototype-Launcher-API[JUnit 5 Launcher API] page is primarily targeted at IDE and build tool providers.

There is also a short paragraph on link:Prototype-Launcher-API#plugging-in-your-own-test-engine[how to plug other test engines into the launcher].

'''

= The Open Test Alliance

Based on discussions with IDE and build tool developers from Eclipse, Gradle, and IntelliJ, the JUnit Lambda team has launched an initiative called the https://github.com/ota4j-team/opentest4j[Open Test Alliance for the JVM], an open source project to provide a minimal common foundation for testing libraries on the JVM. The primary goal of the project is to enable testing frameworks like JUnit, TestNG, Spock, etc. and third-party assertion libraries like Hamcrest, AssertJ, etc. to use a common set of exceptions that IDEs and build tools can support in a consistent manner across all testing scenarios – for example, for consistent reporting and test execution visualization.

'''