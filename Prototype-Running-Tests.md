# Running JUnit 5 Tests

At this stage there is no direct support to run JUnit 5 tests in build tools and IDEs. We provide two intermediate solutions so you can go ahead and try out the prototype. You can use the `ConsoleRunner` or execute JUnit 5 tests with a JUnit 4 style runner.


## Console Runner

The `ConsoleRunner` is a command-line Java application that lets you run JUnit 4/5 tests and prints out test executions and results to the console.

### Gradle/Maven usage

We have prepared two small sample projects that use it through Gradle and Maven. Please see [junit5-gradle-consumer](https://github.com/junit-team/junit5-samples/tree/master/junit5-gradle-consumer) and [junit5-maven-consumer](https://github.com/junit-team/junit5-samples/tree/master/junit5-maven-consumer), respectively.

### Options

*Caution:* These options are very likely to change as we continue to work on the prototype.

	NAME
	        ConsoleRunner - console test runner
	
	SYNOPSIS
	        ConsoleRunner [(-C | --disable-ansi-colors)] [(-h | --help)]
	                [(-m <argumentMode> | --argument-mode <argumentMode>)]
	                [(-x | --enable-exit-code)] [--] [<arguments>...]
	
	OPTIONS
	        -C, --disable-ansi-colors
	            Disable colored output (not supported by all terminals)
	
	        -h, --help
	            Display help information
	
	        -m <argumentMode>, --argument-mode <argumentMode>
	            How to treat arguments. Possible values: classes, packages
	
	        -x, --enable-exit-code
	            Exit process with number of failing tests as exit code
	
	        --
	            This option can be used to separate command-line options from the
	            list of argument, (useful when arguments might be mistaken for
	            command-line options
	
	        <arguments>
	            Test classes or packages to execute (depending on
	            --argument-mode/-m)

## Using JUnit4 to Run JUnit5 Tests

The `JUnit5` runner lets you run JUnit 5 tests with JUnit 4. This way you can run JUnit 5 tests in IDEs and build tools that only know about JUnit 4. As soon as we add reporting-related features to JUnit 5 that JUnit 4 does not have, the runner will only be able to support a subset of the functionality. But for the time being it's an easy way to get started.
