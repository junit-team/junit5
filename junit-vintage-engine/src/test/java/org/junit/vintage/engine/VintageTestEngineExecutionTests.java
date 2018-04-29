/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.abortedWithReason;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.dynamicTestRegistered;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.skippedWithReason;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.uniqueIdSubstring;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;

import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.AssumptionViolatedException;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.vintage.engine.samples.junit3.PlainJUnit3TestCaseWithSingleTestWhichFails;
import org.junit.vintage.engine.samples.junit4.EmptyIgnoredTestCase;
import org.junit.vintage.engine.samples.junit4.EnclosedJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.IgnoredJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteOfSuiteWithIgnoredJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteOfSuiteWithJUnit4TestCaseWithAssumptionFailureInBeforeClass;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteOfSuiteWithJUnit4TestCaseWithErrorInBeforeClass;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithExceptionThrowingRunner;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithIgnoredJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithJUnit3SuiteWithSingleTestCase;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithJUnit4TestCaseWithAssumptionFailureInBeforeClass;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithJUnit4TestCaseWithErrorInBeforeClass;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithAssumptionFailureInBeforeClass;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithErrorCollectorStoringMultipleFailures;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithErrorInAfterClass;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithErrorInBeforeClass;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithExceptionThrowingRunner;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithOverloadedMethod;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithRunnerWithCustomUniqueIds;
import org.junit.vintage.engine.samples.junit4.MalformedJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.ParameterizedTestCase;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithLifecycleMethods;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichFails;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichIsIgnored;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods;
import org.opentest4j.MultipleFailuresError;

/**
 * @since 4.12
 */
class VintageTestEngineExecutionTests {

	@Test
	void executesPlainJUnit4TestCaseWithSingleTestWhichFails() {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("failingTest"), started()), //
			event(test("failingTest"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("this test should fail")))), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesPlainJUnit4TestCaseWithTwoTests() {
		Class<?> testClass = PlainJUnit4TestCaseWithTwoTestMethods.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("failingTest"), started()), //
			event(test("failingTest"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("this test should fail")))), //
			event(test("successfulTest"), started()), //
			event(test("successfulTest"), finishedSuccessfully()), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesPlainJUnit4TestCaseWithFiveTests() {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("abortedTest"), started()), //
			event(test("abortedTest"),
				abortedWithReason(
					allOf(isA(AssumptionViolatedException.class), message("this test should be aborted")))), //
			event(test("failingTest"), started()), //
			event(test("failingTest"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("this test should fail")))), //
			event(test("ignoredTest1_withoutReason"), skippedWithReason("")), //
			event(test("ignoredTest2_withReason"), skippedWithReason("a custom reason")), //
			event(test("successfulTest"), started()), //
			event(test("successfulTest"), finishedSuccessfully()), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesEnclosedJUnit4TestCase() {
		Class<?> testClass = EnclosedJUnit4TestCase.class;
		Class<?> nestedClass = EnclosedJUnit4TestCase.NestedClass.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(nestedClass), started()), //
			event(test("successfulTest"), started()), //
			event(test("successfulTest"), finishedSuccessfully()), //
			event(test("failingTest"), started()), //
			event(test("failingTest"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("this test should fail")))), //
			event(container(nestedClass), finishedSuccessfully()), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4SuiteWithJUnit3SuiteWithSingleTestCase() {
		Class<?> junit4SuiteClass = JUnit4SuiteWithJUnit3SuiteWithSingleTestCase.class;
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;

		List<ExecutionEvent> executionEvents = execute(junit4SuiteClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(junit4SuiteClass), started()), //
			event(container("TestSuite with 1 tests"), started()), //
			event(container(testClass), started()), //
			event(test("test"), started()), //
			event(test("test"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("this test should fail")))), //
			event(container(testClass), finishedSuccessfully()), //
			event(container("TestSuite with 1 tests"), finishedSuccessfully()), //
			event(container(junit4SuiteClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesMalformedJUnit4TestCase() {
		Class<?> testClass = MalformedJUnit4TestCase.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("initializationError"), started()), //
			event(test("initializationError"), finishedWithFailure(message("Method nonPublicTest() should be public"))), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4TestCaseWithErrorInBeforeClass() {
		Class<?> testClass = JUnit4TestCaseWithErrorInBeforeClass.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(testClass),
				finishedWithFailure(allOf(isA(AssertionError.class), message("something went wrong")))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4SuiteWithJUnit4TestCaseWithErrorInBeforeClass() {
		Class<?> suiteClass = JUnit4SuiteWithJUnit4TestCaseWithErrorInBeforeClass.class;
		Class<?> testClass = JUnit4TestCaseWithErrorInBeforeClass.class;

		List<ExecutionEvent> executionEvents = execute(suiteClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(suiteClass), started()), //
			event(container(testClass), started()), //
			event(container(testClass),
				finishedWithFailure(allOf(isA(AssertionError.class), message("something went wrong")))), //
			event(container(suiteClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4SuiteOfSuiteWithJUnit4TestCaseWithErrorInBeforeClass() {
		Class<?> suiteOfSuiteClass = JUnit4SuiteOfSuiteWithJUnit4TestCaseWithErrorInBeforeClass.class;
		Class<?> suiteClass = JUnit4SuiteWithJUnit4TestCaseWithErrorInBeforeClass.class;
		Class<?> testClass = JUnit4TestCaseWithErrorInBeforeClass.class;

		List<ExecutionEvent> executionEvents = execute(suiteOfSuiteClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(suiteOfSuiteClass), started()), //
			event(container(suiteClass), started()), //
			event(container(testClass), started()), //
			event(container(testClass),
				finishedWithFailure(allOf(isA(AssertionError.class), message("something went wrong")))), //
			event(container(suiteClass), finishedSuccessfully()), //
			event(container(suiteOfSuiteClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4SuiteOfSuiteWithJUnit4TestCaseWithAssumptionFailureInBeforeClass() {
		Class<?> suiteOfSuiteClass = JUnit4SuiteOfSuiteWithJUnit4TestCaseWithAssumptionFailureInBeforeClass.class;
		Class<?> suiteClass = JUnit4SuiteWithJUnit4TestCaseWithAssumptionFailureInBeforeClass.class;
		Class<?> testClass = JUnit4TestCaseWithAssumptionFailureInBeforeClass.class;

		List<ExecutionEvent> executionEvents = execute(suiteOfSuiteClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(suiteOfSuiteClass), started()), //
			event(container(suiteClass), started()), //
			event(container(testClass), started()), //
			event(container(testClass),
				abortedWithReason(allOf(isA(AssumptionViolatedException.class), message("assumption violated")))), //
			event(container(suiteClass), finishedSuccessfully()), //
			event(container(suiteOfSuiteClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4TestCaseWithErrorInAfterClass() {
		Class<?> testClass = JUnit4TestCaseWithErrorInAfterClass.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("failingTest"), started()), //
			event(test("failingTest"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("expected to fail")))), //
			event(test("succeedingTest"), started()), //
			event(test("succeedingTest"), finishedSuccessfully()), //
			event(container(testClass),
				finishedWithFailure(allOf(isA(AssertionError.class), message("error in @AfterClass")))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4TestCaseWithOverloadedMethod() {
		Class<?> testClass = JUnit4TestCaseWithOverloadedMethod.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("theory(" + JUnit4TestCaseWithOverloadedMethod.class.getName() + ")[0]"), started()), //
			event(test("theory(" + JUnit4TestCaseWithOverloadedMethod.class.getName() + ")[0]"), finishedWithFailure()), //
			event(test("theory(" + JUnit4TestCaseWithOverloadedMethod.class.getName() + ")[1]"), started()), //
			event(test("theory(" + JUnit4TestCaseWithOverloadedMethod.class.getName() + ")[1]"), finishedWithFailure()), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesIgnoredJUnit4TestCase() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), skippedWithReason("complete class is ignored")), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesEmptyIgnoredTestClass() {
		Class<?> testClass = EmptyIgnoredTestCase.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(test(testClass.getName()), skippedWithReason("empty")), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void reportsExecutionEventsAroundLifecycleMethods() {
		Class<?> testClass = PlainJUnit4TestCaseWithLifecycleMethods.class;
		PlainJUnit4TestCaseWithLifecycleMethods.EVENTS.clear();
		EngineExecutionListener listener = new EngineExecutionListener() {
			@Override
			public void executionStarted(TestDescriptor testDescriptor) {
				PlainJUnit4TestCaseWithLifecycleMethods.EVENTS.add(
					"executionStarted:" + testDescriptor.getDisplayName());
			}

			@Override
			public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
				PlainJUnit4TestCaseWithLifecycleMethods.EVENTS.add(
					"executionFinished:" + testDescriptor.getDisplayName());
			}

			@Override
			public void executionSkipped(TestDescriptor testDescriptor, String reason) {
				PlainJUnit4TestCaseWithLifecycleMethods.EVENTS.add(
					"executionSkipped:" + testDescriptor.getDisplayName());
			}

			@Override
			public void dynamicTestRegistered(TestDescriptor testDescriptor) {
			}

			@Override
			public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
			}
		};

		execute(testClass, listener);

		// @formatter:off
		assertThat(PlainJUnit4TestCaseWithLifecycleMethods.EVENTS).containsExactly(
			"executionStarted:JUnit Vintage",
				"executionStarted:" + testClass.getName(),
					"beforeClass",
						"executionStarted:failingTest",
							"before",
								"failingTest",
							"after",
						"executionFinished:failingTest",
						"executionSkipped:skippedTest",
						"executionStarted:succeedingTest",
							"before",
								"succeedingTest",
							"after",
						"executionFinished:succeedingTest",
					"afterClass",
				"executionFinished:" + testClass.getName(),
			"executionFinished:JUnit Vintage"
		);
		// @formatter:on
	}

	@Test
	void executesJUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored() {
		Class<?> suiteClass = JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;

		List<ExecutionEvent> executionEvents = execute(suiteClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(suiteClass), started()), //
			event(container(testClass), started()), //
			event(test("ignoredTest"), skippedWithReason("ignored test")), //
			event(container(testClass), finishedSuccessfully()), //
			event(container(suiteClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4SuiteOfSuiteWithIgnoredJUnit4TestCase() {
		Class<?> suiteOfSuiteClass = JUnit4SuiteOfSuiteWithIgnoredJUnit4TestCase.class;
		Class<?> suiteClass = JUnit4SuiteWithIgnoredJUnit4TestCase.class;
		Class<?> testClass = IgnoredJUnit4TestCase.class;

		List<ExecutionEvent> executionEvents = execute(suiteOfSuiteClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(suiteOfSuiteClass), started()), //
			event(container(suiteClass), started()), //
			event(container(testClass), skippedWithReason("complete class is ignored")), //
			event(container(suiteClass), finishedSuccessfully()), //
			event(container(suiteOfSuiteClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesParameterizedTestCase() {
		Class<?> testClass = ParameterizedTestCase.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container("[foo]"), started()), //
			event(test("test[foo]"), started()), //
			event(test("test[foo]"), finishedSuccessfully()), //
			event(container("[foo]"), finishedSuccessfully()), //
			event(container("[bar]"), started()), //
			event(test("test[bar]"), started()), //
			event(test("test[bar]"),
				finishedWithFailure(allOf(isA(AssertionError.class), message("expected:<[foo]> but was:<[bar]>")))), //
			event(container("[bar]"), finishedSuccessfully()), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4TestCaseWithExceptionThrowingRunner() {
		Class<?> testClass = JUnit4TestCaseWithExceptionThrowingRunner.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(test(testClass.getName()), started()), //
			event(test(testClass.getName()), finishedWithFailure()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4SuiteWithExceptionThrowingRunner() {
		Class<?> testClass = JUnit4SuiteWithExceptionThrowingRunner.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(testClass), finishedWithFailure()), //
			event(engine(), finishedSuccessfully()));
	}

	public static class DynamicSuiteRunner extends Runner {

		private final Class<?> testClass;

		public DynamicSuiteRunner(Class<?> testClass) {
			this.testClass = testClass;
		}

		@Override
		public Description getDescription() {
			return createSuiteDescription(testClass);
		}

		@Override
		public void run(RunNotifier notifier) {
			Description dynamicDescription = createTestDescription(testClass, "dynamicTest");
			notifier.fireTestStarted(dynamicDescription);
			notifier.fireTestFinished(dynamicDescription);
		}

	}

	@RunWith(DynamicSuiteRunner.class)
	public static class DynamicTestClass {

	}

	@Test
	void reportsDynamicTestsForUnknownDescriptions() {
		Class<?> testClass = DynamicTestClass.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(test(testClass.getName()), started()), //
			event(dynamicTestRegistered("dynamicTest")), //
			event(test("dynamicTest"), started()), //
			event(test("dynamicTest"), finishedSuccessfully()), //
			event(test(testClass.getName()), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	public static class MisbehavingChildlessRunner extends Runner {

		private final Class<?> testClass;

		public MisbehavingChildlessRunner(Class<?> testClass) {
			this.testClass = testClass;
		}

		@Override
		public Description getDescription() {
			return createSuiteDescription(testClass);
		}

		@Override
		public void run(RunNotifier notifier) {
			notifier.fireTestStarted(createTestDescription(testClass, "doesNotExist"));
		}

	}

	@RunWith(MisbehavingChildlessRunner.class)
	public static class MisbehavingChildTestClass {

	}

	@Test
	void ignoreEventsForUnknownDescriptionsByMisbehavingChildlessRunner() {
		Class<?> testClass = MisbehavingChildTestClass.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(test(testClass.getName()), started()), //
			event(dynamicTestRegistered("doesNotExist")), //
			event(test("doesNotExist"), started()), //
			event(test(testClass.getName()), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void executesJUnit4TestCaseWithRunnerWithCustomUniqueIds() {
		Class<?> testClass = JUnit4TestCaseWithRunnerWithCustomUniqueIds.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(uniqueIdSubstring(testClass.getName()), started()), //
			event(uniqueIdSubstring(testClass.getName()), finishedWithFailure()), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	private static List<ExecutionEvent> execute(Class<?> testClass) {
		return ExecutionEventRecorder.execute(new VintageTestEngine(), request(testClass));
	}

	private static void execute(Class<?> testClass, EngineExecutionListener listener) {
		ExecutionEventRecorder.execute(new VintageTestEngine(), request(testClass), listener);
	}

	private static LauncherDiscoveryRequest request(Class<?> testClass) {
		return LauncherDiscoveryRequestBuilder.request().selectors(selectClass(testClass)).build();
	}

	@Test
	void executesJUnit4TestCaseWithErrorCollectorStoringMultipleFailures() {
		Class<?> testClass = JUnit4TestCaseWithErrorCollectorStoringMultipleFailures.class;

		List<ExecutionEvent> executionEvents = execute(testClass);

		assertRecordedExecutionEventsContainsExactly(executionEvents, //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("example"), started()), //
			event(test("example"), //
				finishedWithFailure(allOf(isA(MultipleFailuresError.class), //
					new Condition<>(throwable -> ((MultipleFailuresError) throwable).getFailures().size() == 3,
						"Must contain multiple errors (3)")))), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}
}
