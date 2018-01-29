/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify support for {@link BeforeEach}, {@link AfterEach},
 * {@link BeforeEachCallback}, and {@link AfterEachCallback} in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 * @see BeforeAndAfterTestExecutionCallbackTests
 */
class BeforeAndAfterEachTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();
	private static final List<String> beforeEachMethodCallSequence = new ArrayList<>();

	private static Optional<Throwable> actualExceptionInAfterEachCallback;

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
		beforeEachMethodCallSequence.clear();
		actualExceptionInAfterEachCallback = null;
	}

	@Test
	void beforeEachAndAfterEachCallbacks() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(OuterTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(

			// OuterTestCase
			"fooBeforeEachCallback",
			"barBeforeEachCallback",
				"beforeEachMethod",
					"testOuter",
				"afterEachMethod",
			"barAfterEachCallback",
			"fooAfterEachCallback",

			// InnerTestCase
			"fooBeforeEachCallback",
			"barBeforeEachCallback",
			"fizzBeforeEachCallback",
				"beforeEachMethod",
					"beforeEachInnerMethod",
						"testInner",
					"afterEachInnerMethod",
				"afterEachMethod",
			"fizzAfterEachCallback",
			"barAfterEachCallback",
			"fooAfterEachCallback"

			), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	void beforeEachAndAfterEachCallbacksDeclaredOnSuperclassAndSubclass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(ChildTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeEachCallback",
			"barBeforeEachCallback",
				"testChild",
			"barAfterEachCallback",
			"fooAfterEachCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	void beforeEachAndAfterEachCallbacksDeclaredOnInterfaceAndClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(TestInterfaceTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(

			// Test Interface
			"fooBeforeEachCallback",
			"barBeforeEachCallback",
				"defaultTestMethod",
			"barAfterEachCallback",
			"fooAfterEachCallback",

			// Test Class
			"fooBeforeEachCallback",
			"barBeforeEachCallback",
				"localTestMethod",
			"barAfterEachCallback",
			"fooAfterEachCallback"

		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	@Test
	void beforeEachCallbackThrowsAnException() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInBeforeEachCallbackTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeEachCallback",
			"exceptionThrowingBeforeEachCallback", // throws an exception.
			// barBeforeEachCallback should not get invoked.
				// beforeEachMethod should not get invoked.
					// test should not get invoked.
				// afterEachMethod should not get invoked.
			"barAfterEachCallback",
			"fooAfterEachCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertThat(actualExceptionInAfterEachCallback).containsInstanceOf(EnigmaException.class);
	}

	@Test
	void afterEachCallbackThrowsAnException() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInAfterEachCallbackTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeEachCallback",
			"barBeforeEachCallback",
				"beforeEachMethod",
					"test",
				"afterEachMethod",
			"barAfterEachCallback",
			"exceptionThrowingAfterEachCallback", // throws an exception.
			"fooAfterEachCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertThat(actualExceptionInAfterEachCallback).containsInstanceOf(EnigmaException.class);
	}

	@Test
	void beforeEachMethodThrowsAnException() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInBeforeEachMethodTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// Since the JVM does not guarantee the order in which methods are
		// returned via reflection (and since JUnit Jupiter does not yet
		// support ordering of @BeforeEach methods), we have to figure out
		// which @BeforeEach method got executed first in order to determine
		// the expected call sequence.

		// @formatter:off
		List<String> list1 = asList(
			"fooBeforeEachCallback",
				"beforeEachMethod1", // throws an exception.
				// "beforeEachMethod2" should not get invoked
					// test should not get invoked.
				"afterEachMethod",
			"fooAfterEachCallback"
		);
		List<String> list2 = asList(
			"fooBeforeEachCallback",
				"beforeEachMethod2",
				"beforeEachMethod1", // throws an exception.
					// test should not get invoked.
				"afterEachMethod",
			"fooAfterEachCallback"
		);
		// @formatter:on

		List<String> expected = beforeEachMethodCallSequence.get(0).equals("beforeEachMethod1") ? list1 : list2;

		assertEquals(expected, callSequence, "wrong call sequence");

		assertThat(actualExceptionInAfterEachCallback).containsInstanceOf(EnigmaException.class);
	}

	@Test
	void afterEachMethodThrowsAnException() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInAfterEachMethodTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeEachCallback",
				"beforeEachMethod",
					"test",
				"afterEachMethod", // throws an exception.
			"fooAfterEachCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertThat(actualExceptionInAfterEachCallback).containsInstanceOf(EnigmaException.class);
	}

	@Test
	void testMethodThrowsAnException() {
		LauncherDiscoveryRequest request = request().selectors(
			selectClass(ExceptionInTestMethodTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		// @formatter:off
		assertEquals(asList(
			"fooBeforeEachCallback",
				"beforeEachMethod",
					"test", // throws an exception.
				"afterEachMethod",
			"fooAfterEachCallback"
		), callSequence, "wrong call sequence");
		// @formatter:on

		assertThat(actualExceptionInAfterEachCallback).containsInstanceOf(EnigmaException.class);
	}

	// -------------------------------------------------------------------------

	@ExtendWith(FooMethodLevelCallbacks.class)
	static class ParentTestCase {
	}

	@ExtendWith(BarMethodLevelCallbacks.class)
	static class ChildTestCase extends ParentTestCase {

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
	static class TestInterfaceTestCase implements TestInterface {

		@Test
		void localTest() {
			callSequence.add("localTestMethod");
		}
	}

	@ExtendWith({ FooMethodLevelCallbacks.class, BarMethodLevelCallbacks.class })
	static class OuterTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEachMethod");
		}

		@Test
		void testOuter() {
			callSequence.add("testOuter");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterEachMethod");
		}

		@Nested
		@ExtendWith(FizzMethodLevelCallbacks.class)
		class InnerTestCase {

			@BeforeEach
			void beforeEachInnerMethod() {
				callSequence.add("beforeEachInnerMethod");
			}

			@Test
			void testInner() {
				callSequence.add("testInner");
			}

			@AfterEach
			void afterEachInnerMethod() {
				callSequence.add("afterEachInnerMethod");
			}
		}
	}

	@ExtendWith({ FooMethodLevelCallbacks.class, ExceptionThrowingBeforeEachCallback.class,
			BarMethodLevelCallbacks.class })
	static class ExceptionInBeforeEachCallbackTestCase {

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

	@ExtendWith({ FooMethodLevelCallbacks.class, ExceptionThrowingAfterEachCallback.class,
			BarMethodLevelCallbacks.class })
	static class ExceptionInAfterEachCallbackTestCase {

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

	@ExtendWith(FooMethodLevelCallbacks.class)
	static class ExceptionInBeforeEachMethodTestCase {

		@BeforeEach
		void beforeEach1() {
			beforeEachMethodCallSequence.add("beforeEachMethod1");
			callSequence.add("beforeEachMethod1");
			throw new EnigmaException("@BeforeEach");
		}

		@BeforeEach
		void beforeEach2() {
			beforeEachMethodCallSequence.add("beforeEachMethod2");
			callSequence.add("beforeEachMethod2");
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

	@ExtendWith(FooMethodLevelCallbacks.class)
	static class ExceptionInAfterEachMethodTestCase {

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
			throw new EnigmaException("@AfterEach");
		}
	}

	@ExtendWith(FooMethodLevelCallbacks.class)
	static class ExceptionInTestMethodTestCase {

		@BeforeEach
		void beforeEach() {
			callSequence.add("beforeEachMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
			throw new EnigmaException("@Test");
		}

		@AfterEach
		void afterEach() {
			callSequence.add("afterEachMethod");
		}
	}

	// -------------------------------------------------------------------------

	static class FooMethodLevelCallbacks implements BeforeEachCallback, AfterEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			callSequence.add("fooBeforeEachCallback");
		}

		@Override
		public void afterEach(ExtensionContext context) {
			callSequence.add("fooAfterEachCallback");
			actualExceptionInAfterEachCallback = context.getExecutionException();
		}
	}

	static class BarMethodLevelCallbacks implements BeforeEachCallback, AfterEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			callSequence.add("barBeforeEachCallback");
		}

		@Override
		public void afterEach(ExtensionContext context) {
			callSequence.add("barAfterEachCallback");
		}
	}

	static class FizzMethodLevelCallbacks implements BeforeEachCallback, AfterEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			callSequence.add("fizzBeforeEachCallback");
		}

		@Override
		public void afterEach(ExtensionContext context) {
			callSequence.add("fizzAfterEachCallback");
		}
	}

	static class ExceptionThrowingBeforeEachCallback implements BeforeEachCallback {

		@Override
		public void beforeEach(ExtensionContext context) {
			callSequence.add("exceptionThrowingBeforeEachCallback");
			throw new EnigmaException("BeforeEachCallback");
		}
	}

	static class ExceptionThrowingAfterEachCallback implements AfterEachCallback {

		@Override
		public void afterEach(ExtensionContext context) {
			callSequence.add("exceptionThrowingAfterEachCallback");
			throw new EnigmaException("AfterEachCallback");
		}
	}

}
