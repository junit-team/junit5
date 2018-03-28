/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.container;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.engine;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.suppressed;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

/**
 * Integration tests that verify correct exception handling in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class ExceptionHandlingTests extends AbstractJupiterTestEngineTests {

	@Test
	void failureInTestMethodIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("failingTest");

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(FailureTestCase.class, method));

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(), //
			event(test("failingTest"),
				finishedWithFailure(allOf(isA(AssertionFailedError.class), message("always fails")))));
	}

	@Test
	void uncheckedExceptionInTestMethodIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("testWithUncheckedException");

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(FailureTestCase.class, method));

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(), //
			event(test("testWithUncheckedException"),
				finishedWithFailure(allOf(isA(RuntimeException.class), message("unchecked")))));
	}

	@Test
	void checkedExceptionInTestMethodIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("testWithCheckedException");

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(FailureTestCase.class, method));

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(), //
			event(test("testWithCheckedException"),
				finishedWithFailure(allOf(isA(IOException.class), message("checked")))));
	}

	@Test
	void checkedExceptionInBeforeEachIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");

		FailureTestCase.exceptionToThrowInBeforeEach = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(FailureTestCase.class, method));

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(),
			event(test("succeedingTest"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))));
	}

	@Test
	void checkedExceptionInAfterEachIsRegistered() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");

		FailureTestCase.exceptionToThrowInAfterEach = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(FailureTestCase.class, method));

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(),
			event(test("succeedingTest"), finishedWithFailure(allOf(isA(IOException.class), message("checked")))));
	}

	@Test
	void checkedExceptionInAfterEachIsSuppressedByExceptionInTest() throws NoSuchMethodException {
		Class<?> testClass = FailureTestCase.class;
		Method method = testClass.getDeclaredMethod("testWithUncheckedException");

		FailureTestCase.exceptionToThrowInAfterEach = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(testClass, method));

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("testWithUncheckedException"), started()), //
			event(test("testWithUncheckedException"), //
				finishedWithFailure(allOf( //
					isA(RuntimeException.class), //
					message("unchecked"), //
					suppressed(0, allOf(isA(IOException.class), message("checked")))))), //
			event(container(testClass), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionInAfterEachTakesPrecedenceOverFailedAssumptionInTest() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("abortedTest");
		LauncherDiscoveryRequest request = request().selectors(selectMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInAfterEach = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(FailureTestCase.class), started()), //
			event(test("abortedTest"), started()), //
			event(test("abortedTest"), //
				finishedWithFailure(allOf( //
					isA(IOException.class), //
					message("checked"), //
					suppressed(0, allOf(isA(TestAbortedException.class)))))), //
			event(container(FailureTestCase.class), finishedSuccessfully()), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void checkedExceptionInBeforeAllIsRegistered() throws NoSuchMethodException {
		Class<?> testClass = FailureTestCase.class;
		Method method = testClass.getDeclaredMethod("succeedingTest");

		FailureTestCase.exceptionToThrowInBeforeAll = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(testClass, method));

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(testClass), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void checkedExceptionInAfterAllIsRegistered() throws NoSuchMethodException {
		Class<?> testClass = FailureTestCase.class;
		Method method = testClass.getDeclaredMethod("succeedingTest");

		FailureTestCase.exceptionToThrowInAfterAll = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(selectMethod(testClass, method));

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("succeedingTest"), started()), //
			event(test("succeedingTest"), finishedSuccessfully()), //
			event(container(testClass), finishedWithFailure(allOf(isA(IOException.class), message("checked")))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionInAfterAllCallbackDoesNotHideExceptionInBeforeAllCallback() {
		Class<?> testClass = TestCaseWithThrowingBeforeAllAndAfterAllCallbacks.class;

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(testClass), finishedWithFailure(allOf( //
				message("beforeAll callback"), //
				suppressed(0, message("afterAll callback"))))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionsInConstructorAndAfterAllCallbackAreReportedWhenTestInstancePerMethodIsUsed() {
		Class<?> testClass = TestCaseWithInvalidConstructorAndThrowingAfterAllCallbackAndPerMethodLifecycle.class;

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(test("test"), started()), //
			event(test("test"), finishedWithFailure(message("constructor"))), //
			event(container(testClass), finishedWithFailure(message("afterAll callback"))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void exceptionInConstructorPreventsExecutionOfAfterAllCallbacksWhenTestInstancePerClassIsUsed() {
		Class<?> testClass = TestCaseWithInvalidConstructorAndThrowingAfterAllCallbackAndPerClassLifecycle.class;

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(testClass), started()), //
			event(container(testClass), finishedWithFailure(message("constructor"))), //
			event(engine(), finishedSuccessfully()));
	}

	@Test
	void failureInAfterAllTakesPrecedenceOverTestAbortedExceptionInBeforeAll() throws NoSuchMethodException {
		Method method = FailureTestCase.class.getDeclaredMethod("succeedingTest");
		LauncherDiscoveryRequest request = request().selectors(selectMethod(FailureTestCase.class, method)).build();

		FailureTestCase.exceptionToThrowInBeforeAll = Optional.of(new TestAbortedException("aborted"));
		FailureTestCase.exceptionToThrowInAfterAll = Optional.of(new IOException("checked"));

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getExecutionEvents(), //
			event(engine(), started()), //
			event(container(FailureTestCase.class), started()), //
			event(container(FailureTestCase.class),
				finishedWithFailure(allOf(isA(IOException.class), message("checked"),
					suppressed(0, allOf(isA(TestAbortedException.class), message("aborted")))))), //
			event(engine(), finishedSuccessfully()));
	}

	@AfterEach
	void cleanUpExceptions() {
		FailureTestCase.exceptionToThrowInBeforeAll = Optional.empty();
		FailureTestCase.exceptionToThrowInAfterAll = Optional.empty();
		FailureTestCase.exceptionToThrowInBeforeEach = Optional.empty();
		FailureTestCase.exceptionToThrowInAfterEach = Optional.empty();
	}

	// -------------------------------------------------------------------------

	static class FailureTestCase {

		static Optional<Throwable> exceptionToThrowInBeforeAll = Optional.empty();
		static Optional<Throwable> exceptionToThrowInAfterAll = Optional.empty();
		static Optional<Throwable> exceptionToThrowInBeforeEach = Optional.empty();
		static Optional<Throwable> exceptionToThrowInAfterEach = Optional.empty();

		@BeforeAll
		static void beforeAll() throws Throwable {
			if (exceptionToThrowInBeforeAll.isPresent())
				throw exceptionToThrowInBeforeAll.get();
		}

		@AfterAll
		static void afterAll() throws Throwable {
			if (exceptionToThrowInAfterAll.isPresent())
				throw exceptionToThrowInAfterAll.get();
		}

		@BeforeEach
		void beforeEach() throws Throwable {
			if (exceptionToThrowInBeforeEach.isPresent())
				throw exceptionToThrowInBeforeEach.get();
		}

		@AfterEach
		void afterEach() throws Throwable {
			if (exceptionToThrowInAfterEach.isPresent())
				throw exceptionToThrowInAfterEach.get();
		}

		@Test
		void succeedingTest() {
		}

		@Test
		void failingTest() {
			Assertions.fail("always fails");
		}

		@Test
		void testWithUncheckedException() {
			throw new RuntimeException("unchecked");
		}

		@Test
		void testWithCheckedException() throws IOException {
			throw new IOException("checked");
		}

		@Test
		void abortedTest() {
			assumeFalse(true, "abortedTest");
		}

	}

	@TestInstance(PER_METHOD)
	@ExtendWith(ThrowingAfterAllCallback.class)
	static class TestCaseWithInvalidConstructorAndThrowingAfterAllCallbackAndPerMethodLifecycle {
		TestCaseWithInvalidConstructorAndThrowingAfterAllCallbackAndPerMethodLifecycle() {
			throw new IllegalStateException("constructor");
		}

		@Test
		void test() {
		}
	}

	@TestInstance(PER_CLASS)
	@ExtendWith(ThrowingAfterAllCallback.class)
	static class TestCaseWithInvalidConstructorAndThrowingAfterAllCallbackAndPerClassLifecycle {
		TestCaseWithInvalidConstructorAndThrowingAfterAllCallbackAndPerClassLifecycle() {
			throw new IllegalStateException("constructor");
		}

		@Test
		void test() {
		}
	}

	@ExtendWith(ThrowingBeforeAllCallback.class)
	@ExtendWith(ThrowingAfterAllCallback.class)
	static class TestCaseWithThrowingBeforeAllAndAfterAllCallbacks {
		@Test
		void test() {
		}
	}

	static class ThrowingBeforeAllCallback implements BeforeAllCallback {
		@Override
		public void beforeAll(ExtensionContext context) {
			throw new IllegalStateException("beforeAll callback");
		}
	}

	static class ThrowingAfterAllCallback implements AfterAllCallback {
		@Override
		public void afterAll(ExtensionContext context) {
			throw new IllegalStateException("afterAll callback");
		}
	}

}
