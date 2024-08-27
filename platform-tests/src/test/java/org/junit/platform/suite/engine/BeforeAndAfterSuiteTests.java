/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.suite.engine.SuiteEngineDescriptor.ENGINE_ID;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.FailingAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.FailingBeforeAndAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.FailingBeforeSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.NonStaticAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.NonStaticBeforeSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.NonVoidAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.NonVoidBeforeSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.ParameterAcceptingAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.ParameterAcceptingBeforeSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.PrivateAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.PrivateBeforeSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.SeveralFailingBeforeAndAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.SubclassWithBeforeAndAfterSuite;
import static org.junit.platform.suite.engine.testsuites.LifecycleMethodsSuites.SuccessfulBeforeAndAfterSuite;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.suppressed;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.engine.testcases.StatefulTestCase;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Integration tests that verify support for {@link BeforeSuite} and {@link AfterSuite},
 * in the {@link SuiteTestEngine}.
 *
 * @since 1.11
 */
public class BeforeAndAfterSuiteTests {

	@BeforeEach
	void setUp() {
		StatefulTestCase.callSequence = new ArrayList<>();
	}

	@Test
	void successfulBeforeAndAfterSuite() {
		// @formatter:off
		executeSuite(SuccessfulBeforeAndAfterSuite.class)
				.allEvents()
				.assertStatistics(stats -> stats.started(7).finished(7).succeeded(6).failed(1))
				.assertThatEvents()
				.haveExactly(1, event(test(StatefulTestCase.Test1.class.getName()), finishedSuccessfully()))
				.haveExactly(1, event(test(StatefulTestCase.Test2.class.getName()), finishedWithFailure()));

		assertThat(StatefulTestCase.callSequence).containsExactly(
				"beforeSuiteMethod",
				"test1",
				"test2",
				"afterSuiteMethod"
		);
		// @formatter:on
	}

	@Test
	void beforeAndAfterSuiteInheritance() {
		// @formatter:off
		executeSuite(SubclassWithBeforeAndAfterSuite.class)
				.allEvents()
				.assertStatistics(stats -> stats.started(7).finished(7).succeeded(6).failed(1));

		assertThat(StatefulTestCase.callSequence).containsExactly(
				"superclassBeforeSuiteMethod",
				"subclassBeforeSuiteMethod",
				"test1",
				"test2",
				"subclassAfterSuiteMethod",
				"superclassAfterSuiteMethod"
		);
		// @formatter:on
	}

	@Test
	void failingBeforeSuite() {
		// @formatter:off
		executeSuite(FailingBeforeSuite.class)
				.allEvents()
				.assertStatistics(stats -> stats.started(2).finished(2).succeeded(1).failed(1))
				.assertThatEvents()
				.haveExactly(1, event(
						container(FailingBeforeSuite.class),
						finishedWithFailure(instanceOf(RuntimeException.class),
								message("Exception thrown by @BeforeSuite method"))));

		assertThat(StatefulTestCase.callSequence).containsExactly(
				"beforeSuiteMethod",
				"afterSuiteMethod"
		);
		// @formatter:on
	}

	@Test
	void failingAfterSuite() {
		// @formatter:off
		executeSuite(FailingAfterSuite.class)
				.allEvents()
				.assertStatistics(stats -> stats.started(7).finished(7).succeeded(5).failed(2))
				.assertThatEvents()
				.haveExactly(1, event(
						container(FailingAfterSuite.class),
						finishedWithFailure(instanceOf(RuntimeException.class),
								message("Exception thrown by @AfterSuite method"))));

		assertThat(StatefulTestCase.callSequence).containsExactly(
				"beforeSuiteMethod",
				"test1",
				"test2",
				"afterSuiteMethod"
		);
		// @formatter:on
	}

	@Test
	void failingBeforeAndAfterSuite() {
		// @formatter:off
		executeSuite(FailingBeforeAndAfterSuite.class)
				.allEvents()
				.assertStatistics(stats -> stats.started(2).finished(2).succeeded(1).failed(1))
				.assertThatEvents()
				.haveExactly(1, event(
						container(FailingBeforeAndAfterSuite.class),
						finishedWithFailure(instanceOf(RuntimeException.class),
								message("Exception thrown by @BeforeSuite method"),
								suppressed(0, instanceOf(RuntimeException.class),
										message("Exception thrown by @AfterSuite method")))));

		assertThat(StatefulTestCase.callSequence).containsExactly(
				"beforeSuiteMethod",
				"afterSuiteMethod"
		);
		// @formatter:on
	}

	@Test
	void severalFailingBeforeAndAfterSuite() {
		// @formatter:off
		executeSuite(SeveralFailingBeforeAndAfterSuite.class)
				.allEvents()
				.assertStatistics(stats -> stats.started(2).finished(2).succeeded(1).failed(1))
				.assertThatEvents()
				.haveExactly(1, event(
						container(SeveralFailingBeforeAndAfterSuite.class),
						finishedWithFailure(instanceOf(RuntimeException.class),
								message("Exception thrown by @BeforeSuite method"),
								suppressed(0, instanceOf(RuntimeException.class),
										message("Exception thrown by @AfterSuite method")),
								suppressed(1, instanceOf(RuntimeException.class),
										message("Exception thrown by @AfterSuite method")))));

		assertThat(StatefulTestCase.callSequence).containsExactly(
				"beforeSuiteMethod",
				"afterSuiteMethod",
				"afterSuiteMethod"
		);
		// @formatter:on
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource
	void invalidBeforeOrAfterSuiteMethod(Class<?> testSuiteClass, Predicate<String> failureMessagePredicate) {
		// @formatter:off
		executeSuite(testSuiteClass)
				.allEvents()
				.assertThatEvents()
				.haveExactly(1, event(
						container(testSuiteClass),
						finishedWithFailure(instanceOf(JUnitException.class), message(failureMessagePredicate))));
		// @formatter:on
	}

	private static Stream<Arguments> invalidBeforeOrAfterSuiteMethod() {
		return Stream.of(
			invalidBeforeOrAfterSuiteCase(NonVoidBeforeSuite.class, "@BeforeSuite method", "must not return a value."),
			invalidBeforeOrAfterSuiteCase(ParameterAcceptingBeforeSuite.class, "@BeforeSuite method",
				"must not accept parameters."),
			invalidBeforeOrAfterSuiteCase(NonStaticBeforeSuite.class, "@BeforeSuite method", "must be static."),
			invalidBeforeOrAfterSuiteCase(PrivateBeforeSuite.class, "@BeforeSuite method", "must not be private."),
			invalidBeforeOrAfterSuiteCase(NonVoidAfterSuite.class, "@AfterSuite method", "must not return a value."),
			invalidBeforeOrAfterSuiteCase(ParameterAcceptingAfterSuite.class, "@AfterSuite method",
				"must not accept parameters."),
			invalidBeforeOrAfterSuiteCase(NonStaticAfterSuite.class, "@AfterSuite method", "must be static."),
			invalidBeforeOrAfterSuiteCase(PrivateAfterSuite.class, "@AfterSuite method", "must not be private."));
	}

	private static Arguments invalidBeforeOrAfterSuiteCase(Class<?> suiteClass, String failureMessageStart,
			String failureMessageEnd) {
		return arguments(named(suiteClass.getSimpleName(), suiteClass),
			(Predicate<String>) s -> s.startsWith(failureMessageStart) && s.endsWith(failureMessageEnd));
	}

	private static EngineExecutionResults executeSuite(Class<?> suiteClass) {
		return EngineTestKit.engine(ENGINE_ID).selectors(selectClass(suiteClass)).execute();
	}

}
