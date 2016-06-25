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
import static org.junit.platform.engine.discovery.ClassSelector.selectClass;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link BeforeTestExecutionCallback},
 * {@link AfterTestExecutionCallback}, {@link BeforeEach}, and {@link AfterEach}
 * in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public class BeforeAndAfterTestExecutionCallbackTests extends AbstractJupiterTestEngineTests {

	private static List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	@Test
	public void beforeAndAfterTestExecutionCallbacks() {
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
			"beforeEachOuter",
				"fooBeforeCallback",
				"barBeforeCallback",
					"testOuter",
				"barAfterCallback",
				"fooAfterCallback",
			"afterEachOuter",

			// InnerTestCase
			"beforeEachOuter",
				"beforeEachInner",
					"fooBeforeCallback",
					"barBeforeCallback",
						"fizzBeforeCallback",
							"testInner",
						"fizzAfterCallback",
					"barAfterCallback",
					"fooAfterCallback",
				"afterEachInner",
			"afterEachOuter"

		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	public void beforeAndAfterTestExecutionCallbacksDeclaredOnSuperclassAndSubclass() {
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
	public void beforeAndAfterTestExecutionCallbacksDeclaredOnInterfaceAndClass() {
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
	public void beforeTestExecutionCallbackThrowsAnException() {
		TestDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInBeforeTestExecutionCallbackTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"beforeEachMethod",
				"fooBeforeCallback",
				"exceptionThrowingBeforeTestExecutionCallback", // throws an exception.
				// barBeforeCallback should not get invoked.
					// test() should not get invoked.
				"barAfterCallback",
				"fooAfterCallback",
			"afterEachMethod"
		), callSequence, "wrong call sequence");
		// @formatter:on
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
			"beforeEachMethod", // throws an exception.
				// fooBeforeCallback should not get invoked.
					// test should not get invoked.
				// fooAfterCallback should not get invoked.
			"afterEachMethod"
		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	// -------------------------------------------------------------------------

	@ExtendWith(FooTestExecutionCallbacks.class)
	private static class ParentTestCase {
	}

	@ExtendWith(BarTestExecutionCallbacks.class)
	private static class ChildTestCase extends ParentTestCase {

		@Test
		void test() {
			callSequence.add("testChild");
		}
	}

	@ExtendWith(FooTestExecutionCallbacks.class)
	private interface TestInterface {

		@Test
		default void defaultTest() {
			callSequence.add("defaultTestMethod");
		}
	}

	@ExtendWith(BarTestExecutionCallbacks.class)
	private static class TestInterfaceTestCase implements TestInterface {

		@Test
		void localTest() {
			callSequence.add("localTestMethod");
		}
	}

	@ExtendWith({ FooTestExecutionCallbacks.class, BarTestExecutionCallbacks.class })
	private static class OuterTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEachOuter");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterEachOuter");
		}

		@Nested
		@ExtendWith(FizzTestExecutionCallbacks.class)
		class InnerTestCase {

			@BeforeEach
			void beforeInnerMethod() {
				callSequence.add("beforeEachInner");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}

			@AfterEach
			void afterInnerMethod() {
				callSequence.add("afterEachInner");
			}
		}
	}

	@ExtendWith({ FooTestExecutionCallbacks.class, ExceptionThrowingBeforeTestExecutionCallback.class,
			BarTestExecutionCallbacks.class })
	private static class ExceptionInBeforeTestExecutionCallbackTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEachMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterEachMethod");
		}
	}

	@ExtendWith(FooTestExecutionCallbacks.class)
	private static class ExceptionInBeforeEachMethodTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEachMethod");
			throw new RuntimeException("@BeforeEach");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterEachMethod");
		}
	}

	// -------------------------------------------------------------------------

	private static class FooTestExecutionCallbacks implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("fooBeforeCallback");
		}

		@Override
		public void afterTestExecution(TestExtensionContext context) {
			callSequence.add("fooAfterCallback");
		}
	}

	private static class BarTestExecutionCallbacks implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("barBeforeCallback");
		}

		@Override
		public void afterTestExecution(TestExtensionContext context) {
			callSequence.add("barAfterCallback");
		}
	}

	private static class FizzTestExecutionCallbacks implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("fizzBeforeCallback");
		}

		@Override
		public void afterTestExecution(TestExtensionContext context) {
			callSequence.add("fizzAfterCallback");
		}
	}

	private static class ExceptionThrowingBeforeTestExecutionCallback implements BeforeTestExecutionCallback {

		@Override
		public void beforeTestExecution(TestExtensionContext context) {
			callSequence.add("exceptionThrowingBeforeTestExecutionCallback");
			throw new RuntimeException("BeforeTestExecutionCallback");
		}
	}

}
