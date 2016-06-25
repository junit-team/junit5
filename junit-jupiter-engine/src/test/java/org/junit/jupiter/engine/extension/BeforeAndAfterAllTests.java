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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.TestDiscoveryRequest;

/**
 * Integration tests that verify support for {@link BeforeAll}, {@link AfterAll},
 * {@link BeforeAllCallback}, and {@link AfterAllCallback} in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public class BeforeAndAfterAllTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@Test
	void beforeAllAndAfterAllCallbacks() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(TopLevelTestCase.class,
			"fooBeforeAllCallback",
			"barBeforeAllCallback",
				"beforeAllMethod-1",
					"test-1",
				"afterAllMethod-1",
			"barAfterAllCallback",
			"fooAfterAllCallback"
		);
		// @formatter:on
	}

	@Test
	void beforeAllAndAfterAllCallbacksInSubclass() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(SecondLevelTestCase.class,
			"fooBeforeAllCallback",
			"barBeforeAllCallback",
				"bazBeforeAllCallback",
					"beforeAllMethod-1",
						"beforeAllMethod-2",
							"test-2",
						"afterAllMethod-2",
					"afterAllMethod-1",
				"bazAfterAllCallback",
			"barAfterAllCallback",
			"fooAfterAllCallback"
		);
		// @formatter:on
	}

	@Test
	void beforeAllAndAfterAllCallbacksInSubSubclass() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(ThirdLevelTestCase.class,
			"fooBeforeAllCallback",
			"barBeforeAllCallback",
				"bazBeforeAllCallback",
					"quuxBeforeAllCallback",
						"beforeAllMethod-1",
							"beforeAllMethod-2",
								"beforeAllMethod-3",
									"test-3",
								"afterAllMethod-3",
							"afterAllMethod-2",
						"afterAllMethod-1",
					"quuxAfterAllCallback",
				"bazAfterAllCallback",
			"barAfterAllCallback",
			"fooAfterAllCallback"
		);
		// @formatter:on
	}

	@Test
	void beforeAllMethodThrowsAnException() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(ExceptionInBeforeAllMethodTestCase.class, 0, 0,
			"fooBeforeAllCallback",
				"beforeAllMethod", // throws an exception.
					// test should not get invoked.
				"afterAllMethod",
			"fooAfterAllCallback"
		);
		// @formatter:on
	}

	@Test
	void beforeAllCallbackThrowsAnException() {
		// @formatter:off
		assertBeforeAllAndAfterAllCallbacks(ExceptionInBeforeAllCallbackTestCase.class, 0, 0,
			"fooBeforeAllCallback",
			"exceptionThrowingBeforeAllCallback", // throws an exception.
				// beforeAllMethod should not get invoked.
					// test should not get invoked.
				// afterAllMethod should not get invoked.
			"fooAfterAllCallback"
		);
		// @formatter:on
	}

	private void assertBeforeAllAndAfterAllCallbacks(Class<?> testClass, String... expectedCalls) {
		assertBeforeAllAndAfterAllCallbacks(testClass, 1, 1, expectedCalls);
	}

	private void assertBeforeAllAndAfterAllCallbacks(Class<?> testClass, int testsStarted, int testsSuccessful,
			String... expectedCalls) {

		callSequence.clear();
		TestDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(testsStarted, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(testsSuccessful, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		assertEquals(asList(expectedCalls), callSequence, () -> "wrong call sequence for " + testClass.getName());
	}

	// -------------------------------------------------------------------------

	// Must NOT be private; otherwise, the @Test method gets discovered but never executed.
	@ExtendWith({ FooClassLevelCallbacks.class, BarClassLevelCallbacks.class })
	static class TopLevelTestCase {

		@BeforeAll
		static void beforeAll1() {
			callSequence.add("beforeAllMethod-1");
		}

		@AfterAll
		static void afterAll1() {
			callSequence.add("afterAllMethod-1");
		}

		@Test
		void test() {
			callSequence.add("test-1");
		}
	}

	// Must NOT be private; otherwise, the @Test method gets discovered but never executed.
	@ExtendWith(BazClassLevelCallbacks.class)
	static class SecondLevelTestCase extends TopLevelTestCase {

		@BeforeAll
		static void beforeAll2() {
			callSequence.add("beforeAllMethod-2");
		}

		@AfterAll
		static void afterAll2() {
			callSequence.add("afterAllMethod-2");
		}

		@Test
		@Override
		void test() {
			callSequence.add("test-2");
		}
	}

	@ExtendWith(QuuxClassLevelCallbacks.class)
	private static class ThirdLevelTestCase extends SecondLevelTestCase {

		@BeforeAll
		static void beforeAll3() {
			callSequence.add("beforeAllMethod-3");
		}

		@AfterAll
		static void afterAll3() {
			callSequence.add("afterAllMethod-3");
		}

		@Test
		@Override
		void test() {
			callSequence.add("test-3");
		}
	}

	@ExtendWith(FooClassLevelCallbacks.class)
	private static class ExceptionInBeforeAllMethodTestCase {

		@BeforeAll
		static void beforeAll() {
			callSequence.add("beforeAllMethod");
			throw new RuntimeException("@BeforeAll");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterAll
		static void afterAll() {
			callSequence.add("afterAllMethod");
		}
	}

	@ExtendWith({ FooClassLevelCallbacks.class, ExceptionThrowingBeforeAllCallback.class })
	private static class ExceptionInBeforeAllCallbackTestCase {

		@BeforeAll
		static void beforeAll() {
			callSequence.add("beforeAllMethod");
		}

		@Test
		void test() {
			callSequence.add("test");
		}

		@AfterAll
		static void afterAll() {
			callSequence.add("afterAllMethod");
		}
	}

	// -------------------------------------------------------------------------

	private static class FooClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooBeforeAllCallback");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooAfterAllCallback");
		}
	}

	private static class BarClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barBeforeAllCallback");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barAfterAllCallback");
		}
	}

	private static class BazClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("bazBeforeAllCallback");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("bazAfterAllCallback");
		}
	}

	private static class QuuxClassLevelCallbacks implements BeforeAllCallback, AfterAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("quuxBeforeAllCallback");
		}

		@Override
		public void afterAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("quuxAfterAllCallback");
		}
	}

	private static class ExceptionThrowingBeforeAllCallback implements BeforeAllCallback {

		@Override
		public void beforeAll(ContainerExtensionContext context) {
			callSequence.add("exceptionThrowingBeforeAllCallback");
			throw new RuntimeException("BeforeAllCallback");
		}
	}

}
