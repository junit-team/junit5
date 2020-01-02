/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.engine.Constants.DEFAULT_AFTER_ALL_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_AFTER_EACH_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_BEFORE_ALL_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_BEFORE_EACH_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.TIMEOUT_MODE_PROPERTY_NAME;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.RuntimeUtils;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Events;
import org.junit.platform.testkit.engine.Execution;

/**
 * @since 5.5
 */
@DisplayName("@Timeout")
class TimeoutExtensionTests extends AbstractJupiterTestEngineTests {

	@Test
	@DisplayName("is applied on annotated @Test methods")
	void appliesTimeoutOnAnnotatedTestMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectMethod(TimeoutAnnotatedTestMethodTestCase.class, "testMethod")) //
				.configurationParameter(DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Execution execution = findExecution(results.testEvents(), "testMethod()");
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("testMethod() timed out after 10 milliseconds");
	}

	@Test
	@DisplayName("is not applied on annotated @Test methods using timeout mode: disabled")
	void doesNotApplyTimeoutOnAnnotatedTestMethodsUsingDisabledTimeoutMode() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectMethod(TimeoutAnnotatedTestMethodTestCase.class, "testMethod")) //
				.configurationParameter(DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.configurationParameter(TIMEOUT_MODE_PROPERTY_NAME, "disabled").build());

		Execution execution = findExecution(results.testEvents(), "testMethod()");
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable()) //
				.isEmpty();
	}

	@Test
	@DisplayName("is not applied on annotated @Test methods using timeout mode: disabled")
	void applyTimeoutOnAnnotatedTestMethodsUsingDisabledOnDebugTimeoutMode() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectMethod(TimeoutAnnotatedTestMethodTestCase.class, "testMethod")) //
				.configurationParameter(DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.configurationParameter(TIMEOUT_MODE_PROPERTY_NAME, "disabled_on_debug").build());

		Execution execution = findExecution(results.testEvents(), "testMethod()");

		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				// The check to see if debugging is pushing the timer just above 1 second
				.isLessThan(Duration.ofSeconds(2));

		// Should we test if we're debugging? This test will fail if we are debugging.
		if (RuntimeUtils.isDebugMode()) {
			assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable()) //
					.isEmpty();
		}
		else {
			assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
					.isInstanceOf(TimeoutException.class) //
					.hasMessage("testMethod() timed out after 10 milliseconds");
		}
	}

	@Test
	@DisplayName("is applied on annotated @TestTemplate methods")
	void appliesTimeoutOnAnnotatedTestTemplateMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectMethod(TimeoutAnnotatedTestMethodTestCase.class, "testTemplateMethod")) //
				.configurationParameter(DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Stream.of("repetition 1", "repetition 2").forEach(displayName -> {
			Execution execution = findExecution(results.testEvents(), displayName);
			assertThat(execution.getDuration()) //
					.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
					.isLessThan(Duration.ofSeconds(1));
			assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
					.isInstanceOf(TimeoutException.class) //
					.hasMessage("testTemplateMethod() timed out after 10 milliseconds");
		});
	}

	@Test
	@DisplayName("is applied on annotated @TestFactory methods")
	void appliesTimeoutOnAnnotatedTestFactoryMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectMethod(TimeoutAnnotatedTestMethodTestCase.class, "testFactoryMethod")) //
				.configurationParameter(DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Execution execution = findExecution(results.containerEvents(), "testFactoryMethod()");
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("testFactoryMethod() timed out after 10 milliseconds");
	}

	@TestFactory
	@DisplayName("is applied on testable methods in annotated classes")
	Stream<DynamicTest> appliesTimeoutOnTestableMethodsInAnnotatedClasses() {
		return Stream.of(TimeoutAnnotatedClassTestCase.class, InheritedTimeoutAnnotatedClassTestCase.class).map(
			testClass -> dynamicTest(testClass.getSimpleName(), () -> {
				EngineExecutionResults results = executeTests(request() //
						.selectors(selectClass(testClass)) //
						.configurationParameter(DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
						.configurationParameter(DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
						.configurationParameter(DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
						.build());

				Stream.of("testMethod()", "repetition 1", "repetition 2", "testFactoryMethod()").forEach(
					displayName -> {
						Execution execution = findExecution(results.allEvents(), displayName);
						assertThat(execution.getDuration()) //
								.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
								.isLessThan(Duration.ofSeconds(1));
						assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
								.isInstanceOf(TimeoutException.class) //
								.hasMessageEndingWith("timed out after 10000000 nanoseconds");
					});
			}));
	}

	@Test
	@DisplayName("fails methods that do not throw InterruptedException")
	void failsMethodsWithoutInterruptedException() {
		EngineExecutionResults results = executeTestsForClass(MethodWithoutInterruptedExceptionTestCase.class);

		Execution execution = findExecution(results.testEvents(), "methodThatDoesNotThrowInterruptedException()");
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(1)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getStatus()).isEqualTo(FAILED);
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("methodThatDoesNotThrowInterruptedException() timed out after 1 millisecond");
	}

	@Test
	@DisplayName("is applied on annotated @BeforeAll methods")
	void appliesTimeoutOnAnnotatedBeforeAllMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectClass(TimeoutAnnotatedBeforeAllMethodTestCase.class)) //
				.configurationParameter(DEFAULT_BEFORE_ALL_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Execution execution = findExecution(results.containerEvents(),
			TimeoutAnnotatedBeforeAllMethodTestCase.class.getSimpleName());
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("setUp() timed out after 10 milliseconds");
	}

	@Test
	@DisplayName("is applied on annotated @BeforeEach methods")
	void appliesTimeoutOnAnnotatedBeforeEachMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectClass(TimeoutAnnotatedBeforeEachMethodTestCase.class)) //
				.configurationParameter(DEFAULT_BEFORE_EACH_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Execution execution = findExecution(results.testEvents(), "testMethod()");
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("setUp() timed out after 10 milliseconds");
	}

	@Test
	@DisplayName("is applied on annotated @AfterEach methods")
	void appliesTimeoutOnAnnotatedAfterEachMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectClass(TimeoutAnnotatedAfterEachMethodTestCase.class)) //
				.configurationParameter(DEFAULT_AFTER_EACH_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Execution execution = findExecution(results.testEvents(), "testMethod()");
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("tearDown() timed out after 10 milliseconds");
	}

	@Test
	@DisplayName("is applied on annotated @AfterAll methods")
	void appliesTimeoutOnAnnotatedAfterAllMethods() {
		EngineExecutionResults results = executeTests(request() //
				.selectors(selectClass(TimeoutAnnotatedAfterAllMethodTestCase.class)) //
				.configurationParameter(DEFAULT_AFTER_ALL_METHOD_TIMEOUT_PROPERTY_NAME, "42ns") //
				.build());

		Execution execution = findExecution(results.containerEvents(),
			TimeoutAnnotatedAfterAllMethodTestCase.class.getSimpleName());
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessage("tearDown() timed out after 10 milliseconds");
	}

	@TestFactory
	@DisplayName("is applied from configuration parameters by default")
	Stream<DynamicTest> appliesDefaultTimeoutsFromConfigurationParameters() {
		return Map.of(DEFAULT_BEFORE_ALL_METHOD_TIMEOUT_PROPERTY_NAME, "beforeAll()", //
			DEFAULT_BEFORE_EACH_METHOD_TIMEOUT_PROPERTY_NAME, "beforeEach()", //
			DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME, "test()", //
			DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME, "testTemplate()", //
			DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME, "testFactory()", //
			DEFAULT_AFTER_EACH_METHOD_TIMEOUT_PROPERTY_NAME, "afterEach()", //
			DEFAULT_AFTER_ALL_METHOD_TIMEOUT_PROPERTY_NAME, "afterAll()" //
		).entrySet().stream().map(entry -> dynamicTest("uses " + entry.getKey() + " config param", () -> {
			EngineExecutionResults results = executeTests(request() //
					.selectors(selectClass(PlainTestCase.class)) //
					.configurationParameter(entry.getKey(), "1ns") //
					.build());
			var failure = results.allEvents().executions().failed() //
					.map(execution -> execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
					.findFirst();
			assertThat(failure).containsInstanceOf(TimeoutException.class);
			assertThat(failure.get()).hasMessage(entry.getValue() + " timed out after 1 nanosecond");
		}));
	}

	@Test
	@DisplayName("does not swallow blacklisted exceptions")
	void doesNotSwallowBlacklistedExceptions() {
		assertThrows(OutOfMemoryError.class, () -> executeTestsForClass(BlacklistedExceptionTestCase.class));
	}

	@Test
	@DisplayName("does not affect tests that don't exceed the timeout")
	void doesNotAffectTestsThatDoNotExceedTimeoutDuration() {
		executeTestsForClass(NonTimeoutExceedingTestCase.class).allEvents().assertStatistics(stats -> stats.failed(0));
	}

	@Test
	@DisplayName("includes fully qualified class name if method is not in the test class")
	void includesClassNameIfMethodIsNotInTestClass() {
		EngineExecutionResults results = executeTestsForClass(NestedClassWithOuterSetupMethodTestCase.class);

		Execution execution = findExecution(results.testEvents(), "testMethod()");
		assertThat(execution.getDuration()) //
				.isGreaterThanOrEqualTo(Duration.ofMillis(10)) //
				.isLessThan(Duration.ofSeconds(1));
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(TimeoutException.class) //
				.hasMessageEndingWith(
					"$NestedClassWithOuterSetupMethodTestCase#setUp() timed out after 10 milliseconds");
	}

	@Test
	@DisplayName("reports illegal timeout durations")
	void reportsIllegalTimeoutDurations() {
		EngineExecutionResults results = executeTestsForClass(IllegalTimeoutDurationTestCase.class);

		Execution execution = findExecution(results.testEvents(), "testMethod()");
		assertThat(execution.getTerminationInfo().getExecutionResult().getThrowable().orElseThrow()) //
				.isInstanceOf(PreconditionViolationException.class) //
				.hasMessage("timeout duration must be a positive number: 0");
	}

	private Execution findExecution(Events events, String displayName) {
		return getOnlyElement(events //
				.executions() //
				.filter(execution -> execution.getTestDescriptor().getDisplayName().contains(displayName)) //
				.collect(toList()));
	}

	static class TimeoutAnnotatedTestMethodTestCase {
		@Test
		@Timeout(value = 10, unit = MILLISECONDS)
		void testMethod() throws Exception {
			Thread.sleep(1000);
		}

		@RepeatedTest(2)
		@Timeout(value = 10, unit = MILLISECONDS)
		void testTemplateMethod() throws Exception {
			Thread.sleep(1000);
		}

		@TestFactory
		@Timeout(value = 10, unit = MILLISECONDS)
		Stream<DynamicTest> testFactoryMethod() throws Exception {
			Thread.sleep(1000);
			return Stream.empty();
		}
	}

	static class TimeoutAnnotatedBeforeAllMethodTestCase {
		@BeforeAll
		@Timeout(value = 10, unit = MILLISECONDS)
		static void setUp() throws Exception {
			Thread.sleep(1000);
		}

		@Test
		void testMethod() {
			// never called
		}
	}

	static class TimeoutAnnotatedBeforeEachMethodTestCase {
		@BeforeEach
		@Timeout(value = 10, unit = MILLISECONDS)
		void setUp() throws Exception {
			Thread.sleep(1000);
		}

		@Test
		void testMethod() {
			// never called
		}
	}

	static class TimeoutAnnotatedAfterEachMethodTestCase {
		@Test
		void testMethod() {
			// do nothing
		}

		@AfterEach
		@Timeout(value = 10, unit = MILLISECONDS)
		void tearDown() throws Exception {
			Thread.sleep(1000);
		}
	}

	static class TimeoutAnnotatedAfterAllMethodTestCase {
		@Test
		void testMethod() {
			// do nothing
		}

		@AfterAll
		@Timeout(value = 10, unit = MILLISECONDS)
		static void tearDown() throws Exception {
			Thread.sleep(1000);
		}
	}

	@Timeout(value = 10_000_000, unit = NANOSECONDS)
	static class TimeoutAnnotatedClassTestCase {
		@Nested
		class NestedClass {
			@Test
			void testMethod() throws Exception {
				Thread.sleep(1000);
			}

			@RepeatedTest(2)
			void testTemplateMethod() throws Exception {
				Thread.sleep(1000);
			}

			@TestFactory
			Stream<DynamicTest> testFactoryMethod() throws Exception {
				Thread.sleep(1000);
				return Stream.empty();
			}
		}
	}

	static class InheritedTimeoutAnnotatedClassTestCase extends TimeoutAnnotatedClassTestCase {
	}

	static class MethodWithoutInterruptedExceptionTestCase {
		@Test
		@Timeout(value = 1, unit = MILLISECONDS)
		void methodThatDoesNotThrowInterruptedException() {
			new EventuallyInterruptibleInvocation().proceed();
		}
	}

	static class PlainTestCase {
		@BeforeAll
		static void beforeAll() throws Exception {
			Thread.sleep(10);
		}

		@BeforeEach
		void beforeEach() throws Exception {
			Thread.sleep(10);
		}

		@Test
		void test() throws Exception {
			Thread.sleep(10);
		}

		@RepeatedTest(2)
		void testTemplate() throws Exception {
			Thread.sleep(10);
		}

		@TestFactory
		Stream<DynamicTest> testFactory() throws Exception {
			Thread.sleep(10);
			return Stream.empty();
		}

		@AfterEach
		void afterEach() throws Exception {
			Thread.sleep(10);
		}

		@AfterAll
		static void afterAll() throws Exception {
			Thread.sleep(10);
		}
	}

	static class BlacklistedExceptionTestCase {
		@Test
		@Timeout(value = 1, unit = NANOSECONDS)
		void test() {
			new EventuallyInterruptibleInvocation().proceed();
			throw new OutOfMemoryError();
		}
	}

	@Timeout(10)
	static class NonTimeoutExceedingTestCase {
		@Test
		void testMethod() {
		}

		@RepeatedTest(1)
		void testTemplateMethod() {
		}

		@TestFactory
		Stream<DynamicTest> testFactoryMethod() {
			return Stream.of(dynamicTest("dynamicTest", () -> {
			}));
		}
	}

	static class NestedClassWithOuterSetupMethodTestCase {

		@Timeout(value = 10, unit = MILLISECONDS)
		@BeforeEach
		void setUp() throws Exception {
			Thread.sleep(1000);
		}

		@Nested
		class NestedClass {

			@BeforeEach
			void setUp() {
			}

			@Test
			void testMethod() {
			}

		}

	}

	static class IllegalTimeoutDurationTestCase {

		@Test
		@Timeout(0)
		void testMethod() {
		}

	}

}
