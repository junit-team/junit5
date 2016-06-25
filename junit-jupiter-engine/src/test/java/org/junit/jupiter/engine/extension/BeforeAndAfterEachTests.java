/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.ClassSelector.selectClass;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link BeforeEach}, {@link AfterEach},
 * {@link BeforeEachCallback}, and {@link AfterEachCallback} in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 * @see BeforeAndAfterTestExecutionCallbackTests
 */
public class BeforeAndAfterEachTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	private static Optional<Throwable> actualExceptionInAfterEachCallback;

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
		actualExceptionInAfterEachCallback = null;
	}

	@Test
	public void beforeEachAndAfterEachCallbacks() {
		TestDiscoveryRequest request = request().selectors(selectClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(

			// OuterTestCase
			"fooBeforeCallback",
			"barBeforeCallback",
				"beforeMethod",
					"testOuter",
				"afterMethod",
			"barAfterCallback",
			"fooAfterCallback",

			// InnerTestCase
			"fooBeforeCallback",
			"barBeforeCallback",
			"fizzBeforeCallback",
				"beforeMethod",
					"beforeInnerMethod",
						"testInner",
					"afterInnerMethod",
				"afterMethod",
			"fizzAfterCallback",
			"barAfterCallback",
			"fooAfterCallback"

			), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	public void beforeEachAndAfterEachCallbacksDeclaredOnSuperclassAndSubclass() {
		TestDiscoveryRequest request = request().selectors(selectClass(ChildTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeCallback",
			"barBeforeCallback",
				"testChild",
			"barAfterCallback",
			"fooAfterCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	public void beforeEachAndAfterEachCallbacksDeclaredOnInterfaceAndClass() {
		TestDiscoveryRequest request = request().selectors(selectClass(TestInterfaceTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(

			// Test Interface
			"fooBeforeCallback",
			"barBeforeCallback",
				"defaultTestMethod",
			"barAfterCallback",
			"fooAfterCallback",

			// Test Class
			"fooBeforeCallback",
			"barBeforeCallback",
				"localTestMethod",
			"barAfterCallback",
			"fooAfterCallback"

		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	public void beforeEachCallbackThrowsAnException() {
		TestDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInBeforeEachCallbackTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeCallback",
			"exceptionThrowingBeforeEachCallback", // throws an exception.
			// barBeforeCallback should not get invoked.
				// beforeMethod should not get invoked.
					// test should not get invoked.
				// afterMethod should not get invoked.
			"barAfterCallback",
			"fooAfterCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertTrue(actualExceptionInAfterEachCallback.isPresent(), "test exception should be present");
		assertEquals(EnigmaException.class, actualExceptionInAfterEachCallback.get().getClass());
	}

	@Test
	public void afterEachCallbackThrowsAnException() {
		TestDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInAfterEachCallbackTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeCallback",
			"barBeforeCallback",
				"beforeMethod",
					"test",
				"afterMethod",
			"barAfterCallback",
			"exceptionThrowingAfterEachCallback", // throws an exception.
			"fooAfterCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertTrue(actualExceptionInAfterEachCallback.isPresent(), "test exception should be present");
		assertEquals(EnigmaException.class, actualExceptionInAfterEachCallback.get().getClass());
	}

	@Test
	public void beforeEachMethodThrowsAnException() {
		TestDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInBeforeEachMethodTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeCallback",
				"beforeMethod", // throws an exception.
					// test should not get invoked.
				"afterMethod",
			"fooAfterCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertTrue(actualExceptionInAfterEachCallback.isPresent(), "test exception should be present");
		assertEquals(EnigmaException.class, actualExceptionInAfterEachCallback.get().getClass());
	}

	@Test
	public void afterEachMethodThrowsAnException() {
		TestDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInAfterEachMethodTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeCallback",
				"beforeMethod",
					"test",
				"afterMethod", // throws an exception.
			"fooAfterCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertTrue(actualExceptionInAfterEachCallback.isPresent(), "test exception should be present");
		assertEquals(EnigmaException.class, actualExceptionInAfterEachCallback.get().getClass());
	}

	@Test
	public void testMethodThrowsAnException() {
		TestDiscoveryRequest request = request().selectors(selectClass(ExceptionInTestMethodTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeCallback",
				"beforeMethod",
					"test", // throws an exception.
				"afterMethod",
			"fooAfterCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertTrue(actualExceptionInAfterEachCallback.isPresent(), "test exception should be present");
		assertEquals(EnigmaException.class, actualExceptionInAfterEachCallback.get().getClass());
	}

	// -------------------------------------------------------------------------

	@ExtendWith(FooMethodLevelCallbacks.class)
	private static class ParentTestCase {
	}

	@ExtendWith(BarMethodLevelCallbacks.class)
	private static class ChildTestCase extends ParentTestCase {

		@Test
		void test() {
			callSequence.add("testChild");
		}
	}

	@ExtendWith(FooMethodLevelCallbacks.class)
	private interface TestInterface {

		@Test
		default void defaultTest() {
			callSequence.add("defaultTestMethod");
		}
	}

	@ExtendWith(BarMethodLevelCallbacks.class)
	private static class TestInterfaceTestCase implements TestInterface {

		@Test
		void localTest() {
			callSequence.add("localTestMethod");
		}
	}

	@ExtendWith({ FooMethodLevelCallbacks.class, BarMethodLevelCallbacks.class })
	private static class OuterTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
		}

		@Nested
		@ExtendWith(FizzMethodLevelCallbacks.class)
		class InnerTestCase {

			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeInnerMethod");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}

			@AfterEach
			void afterInnerMethod() {
				callSequence.add("afterInnerMethod");
			}
		}
	}

	@ExtendWith({ FooMethodLevelCallbacks.class, ExceptionThrowingBeforeEachCallback.class,
			BarMethodLevelCallbacks.class })
	private static class ExceptionInBeforeEachCallbackTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
		}
	}

	@ExtendWith({ FooMethodLevelCallbacks.class, ExceptionThrowingAfterEachCallback.class,
			BarMethodLevelCallbacks.class })
	private static class ExceptionInAfterEachCallbackTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
		}
	}

	@ExtendWith(FooMethodLevelCallbacks.class)
	private static class ExceptionInBeforeEachMethodTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
			throw new EnigmaException("@BeforeEach");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
		}
	}

	@ExtendWith(FooMethodLevelCallbacks.class)
	private static class ExceptionInAfterEachMethodTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
			throw new EnigmaException("@AfterEach");
		}
	}

	@ExtendWith(FooMethodLevelCallbacks.class)
	private static class ExceptionInTestMethodTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
			throw new EnigmaException("@Test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterMethod");
		}
	}

	// -------------------------------------------------------------------------

	private static class FooMethodLevelCallbacks implements BeforeEachCallback, AfterEachCallback {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("fooBeforeCallback");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("fooAfterCallback");
			actualExceptionInAfterEachCallback = context.getTestException();
		}
	}

	private static class BarMethodLevelCallbacks implements BeforeEachCallback, AfterEachCallback {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("barBeforeCallback");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("barAfterCallback");
		}
	}

	private static class FizzMethodLevelCallbacks implements BeforeEachCallback, AfterEachCallback {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("fizzBeforeCallback");
		}

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("fizzAfterCallback");
		}
	}

	private static class ExceptionThrowingBeforeEachCallback implements BeforeEachCallback {

		@Override
		public void beforeEach(TestExtensionContext context) {
			callSequence.add("exceptionThrowingBeforeEachCallback");
			throw new EnigmaException("BeforeEachCallback");
		}
	}

	private static class ExceptionThrowingAfterEachCallback implements AfterEachCallback {

		@Override
		public void afterEach(TestExtensionContext context) {
			callSequence.add("exceptionThrowingAfterEachCallback");
			throw new EnigmaException("AfterEachCallback");
		}
	}

	@SuppressWarnings("serial")
	private static class EnigmaException extends RuntimeException {

		EnigmaException(String message) {
			super(message);
		}
	}

}
