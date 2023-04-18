/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.displayName;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.skippedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.lang.reflect.Method;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.testkit.engine.Events;

/**
 * Integration tests for {@link RepeatedTest @RepeatedTest} and supporting
 * infrastructure.
 *
 * @since 5.0
 */
class RepeatedTestTests extends AbstractJupiterTestEngineTests {

	@RepeatedTest(1)
	@DisplayName("Repeat!")
	void customDisplayName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("repetition 1 of 1");
	}

	@RepeatedTest(1)
	@DisplayName("   \t ")
	void customDisplayNameWithBlankName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("repetition 1 of 1");
	}

	@RepeatedTest(value = 1, name = "{displayName}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingDisplayName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repeat!");
	}

	@RepeatedTest(value = 1, name = "#{currentRepetition}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingCurrentRepetition(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("#1");
	}

	@RepeatedTest(value = 1, name = "Repetition #{currentRepetition} for {displayName}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingDisplayNameAndCurrentRepetition(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repetition #1 for Repeat!");
	}

	@RepeatedTest(value = 1, name = RepeatedTest.LONG_DISPLAY_NAME)
	@DisplayName("Repeat!")
	void customDisplayNameWithPredefinedLongPattern(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repeat! :: repetition 1 of 1");
	}

	@RepeatedTest(value = 1, name = "{displayName} {currentRepetition}/{totalRepetitions}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingDisplayNameCurrentRepetitionAndTotalRepetitions(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repeat! 1/1");
	}

	@RepeatedTest(value = 1, name = "Repetition #{currentRepetition} for {displayName}")
	void defaultDisplayNameWithPatternIncludingDisplayNameAndCurrentRepetition(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo(
			"Repetition #1 for defaultDisplayNameWithPatternIncludingDisplayNameAndCurrentRepetition(TestInfo)");
	}

	@RepeatedTest(value = 5, name = "{displayName}")
	void injectRepetitionInfo(TestInfo testInfo, RepetitionInfo repetitionInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("injectRepetitionInfo(TestInfo, RepetitionInfo)");
		assertThat(repetitionInfo.getCurrentRepetition()).isBetween(1, 5);
		assertThat(repetitionInfo.getTotalRepetitions()).isEqualTo(5);
	}

	@Nested
	class LifecycleMethodTests {

		private static int fortyTwo = 0;

		@AfterAll
		static void afterAll() {
			assertEquals(42, fortyTwo);
		}

		@BeforeEach
		@AfterEach
		void beforeAndAfterEach(TestInfo testInfo, RepetitionInfo repetitionInfo) {
			switch (testInfo.getTestMethod().get().getName()) {
				case "repeatedOnce": {
					assertThat(repetitionInfo.getCurrentRepetition()).isEqualTo(1);
					assertThat(repetitionInfo.getTotalRepetitions()).isEqualTo(1);
					break;
				}
				case "repeatedFortyTwoTimes": {
					assertThat(repetitionInfo.getCurrentRepetition()).isBetween(1, 42);
					assertThat(repetitionInfo.getTotalRepetitions()).isEqualTo(42);
					break;
				}
			}
		}

		@RepeatedTest(1)
		void repeatedOnce(TestInfo testInfo) {
			assertThat(testInfo.getDisplayName()).isEqualTo("repetition 1 of 1");
		}

		@RepeatedTest(42)
		void repeatedFortyTwoTimes(TestInfo testInfo) {
			assertThat(testInfo.getDisplayName()).matches("repetition \\d+ of 42");
			fortyTwo++;
		}

	}

	@Nested
	class FailureTests {

		@Test
		void failsContainerForEmptyPattern() {
			executeTest("testWithEmptyPattern").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("testWithEmptyPattern()"), //
						finishedWithFailure(
							message(value -> value.contains("must be declared with a non-empty name")))));
		}

		@Test
		void failsContainerForBlankPattern() {
			executeTest("testWithBlankPattern").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("testWithBlankPattern()"), //
						finishedWithFailure(
							message(value -> value.contains("must be declared with a non-empty name")))));
		}

		@Test
		void failsContainerForNegativeRepeatCount() {
			executeTest("negativeRepeatCount").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("negativeRepeatCount()"), //
						finishedWithFailure(
							message(value -> value.contains("must be declared with a positive 'value'")))));
		}

		@Test
		void failsContainerForZeroRepeatCount() {
			executeTest("zeroRepeatCount").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("zeroRepeatCount()"), //
						finishedWithFailure(
							message(value -> value.contains("must be declared with a positive 'value'")))));
		}

		@Test
		void failsContainerForFailureThresholdSetToNegativeValue() {
			executeTest("failureThresholdSetToNegativeValue").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("failureThresholdSetToNegativeValue()"), //
						finishedWithFailure(
							message(value -> value.contains("must be declared with a positive 'failureThreshold'")))));
		}

		@Test
		void failsContainerForFailureThresholdSetToZero() {
			executeTest("failureThresholdSetToZero").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("failureThresholdSetToZero()"), //
						finishedWithFailure(
							message(value -> value.contains("must be declared with a positive 'failureThreshold'")))));
		}

		@Test
		void failsContainerForFailureThresholdGreaterThanRepetitionCount() {
			executeTest("failureThresholdGreaterThanRepetitionCount").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("failureThresholdGreaterThanRepetitionCount()"), //
						finishedWithFailure(message(value -> value.contains(
							"must be declared with a 'failureThreshold' less than the number of repetitions")))));
		}

		@Test
		void failsContainerForFailureThresholdEqualToRepetitionCount() {
			executeTest("failureThresholdEqualToRepetitionCount").assertThatEvents() //
					.haveExactly(1, event(container(), displayName("failureThresholdEqualToRepetitionCount()"), //
						finishedWithFailure(message(value -> value.contains(
							"must be declared with a 'failureThreshold' less than the number of repetitions")))));
		}

		@Test
		void failureThresholdEqualToRepetitionCountMinusOne() {
			String methodName = "failureThresholdEqualToRepetitionCountMinusOne";
			// @formatter:off
			executeTest(methodName).assertEventsMatchLooselyInOrder(
				event(container(methodName), started()),
				event(test("test-template-invocation:#1"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#2"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#3"), skippedWithReason("Failure threshold [2] exceeded")),
				event(container(methodName), finishedSuccessfully()));
			// @formatter:on
		}

		@Test
		void failureThreshold1() {
			String methodName = "failureThreshold1";
			// @formatter:off
			executeTest(methodName, RepetitionInfo.class).assertEventsMatchLooselyInOrder(
				event(container(methodName), started()),
				event(test("test-template-invocation:#1"), finishedSuccessfully()),
				event(test("test-template-invocation:#2"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#3"), skippedWithReason("Failure threshold [1] exceeded")),
				event(container(methodName), finishedSuccessfully()));
			// @formatter:on
		}

		@Test
		void failureThreshold2() {
			String methodName = "failureThreshold2";
			// @formatter:off
			executeTest(methodName, RepetitionInfo.class).assertEventsMatchLooselyInOrder(
				event(container(methodName), started()),
				event(test("test-template-invocation:#1"), finishedSuccessfully()),
				event(test("test-template-invocation:#2"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#3"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#4"), skippedWithReason("Failure threshold [2] exceeded")),
				event(container(methodName), finishedSuccessfully()));
			// @formatter:on
		}

		@Test
		void failureThreshold3() {
			String methodName = "failureThreshold3";
			// @formatter:off
			executeTest(methodName, RepetitionInfo.class).assertEventsMatchLooselyInOrder(
				event(container(methodName), started()),
				event(test("test-template-invocation:#1"), finishedSuccessfully()),
				event(test("test-template-invocation:#2"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#3"), finishedSuccessfully()),
				event(test("test-template-invocation:#4"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#5"), finishedSuccessfully()),
				event(test("test-template-invocation:#6"), finishedWithFailure(message("Boom!"))),
				event(test("test-template-invocation:#7"), skippedWithReason("Failure threshold [3] exceeded")),
				event(test("test-template-invocation:#8"), skippedWithReason("Failure threshold [3] exceeded")),
				event(container(methodName), finishedSuccessfully()));
			// @formatter:on
		}

		private Events executeTest(String methodName) {
			return executeTest(methodName, new Class<?>[0]);
		}

		private Events executeTest(String methodName, Class<?>... parameterTypes) {
			Class<TestCase> testClass = TestCase.class;
			Method method = ReflectionUtils.findMethod(testClass, methodName, parameterTypes).get();
			return executeTests(selectMethod(testClass, method)).allEvents();
		}

	}

	static class TestCase {

		@RepeatedTest(value = 1, name = "")
		void testWithEmptyPattern() {
		}

		@RepeatedTest(value = 1, name = " \t  ")
		void testWithBlankPattern() {
		}

		@RepeatedTest(-99)
		void negativeRepeatCount() {
		}

		@RepeatedTest(0)
		void zeroRepeatCount() {
		}

		@RepeatedTest(value = 10, failureThreshold = -1)
		void failureThresholdSetToNegativeValue() {
			fail("Boom!");
		}

		@RepeatedTest(value = 10, failureThreshold = 0)
		void failureThresholdSetToZero() {
			fail("Boom!");
		}

		@RepeatedTest(value = 10, failureThreshold = 11)
		void failureThresholdGreaterThanRepetitionCount() {
			fail("Boom!");
		}

		@RepeatedTest(value = 10, failureThreshold = 10)
		void failureThresholdEqualToRepetitionCount() {
			fail("Boom!");
		}

		@RepeatedTest(value = 3, failureThreshold = 2)
		void failureThresholdEqualToRepetitionCountMinusOne() {
			fail("Boom!");
		}

		@RepeatedTest(value = 3, failureThreshold = 1)
		void failureThreshold1(RepetitionInfo info) {
			if (info.getCurrentRepetition() > 1) {
				fail("Boom!");
			}
		}

		@RepeatedTest(value = 4, failureThreshold = 2)
		void failureThreshold2(RepetitionInfo info) {
			if (info.getCurrentRepetition() > 1) {
				fail("Boom!");
			}
		}

		@RepeatedTest(value = 8, failureThreshold = 3)
		void failureThreshold3(RepetitionInfo info) {
			int count = info.getCurrentRepetition();
			if ((count > 1) && (count % 2 == 0)) {
				fail("Boom!");
			}
		}

	}

}
