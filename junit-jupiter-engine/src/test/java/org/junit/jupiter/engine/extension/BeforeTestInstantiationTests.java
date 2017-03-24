/*
 * Copyright 2015-2017 the original author or authors.
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
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestInstantiationCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify support for {@link BeforeTestInstantiationCallback} in the
 * {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
public class BeforeTestInstantiationTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@Test
	void beforeTestInstantiationCallback() {
		// @formatter:off
		assertBeforeTestInstantiationCallback(TopLevelTestCase.class,
				"fooBeforeAllCallback",
				"barBeforeAllCallback",
					"beforeAllMethod-1",
						"fooBeforeTestInstantiationCallback",
						"barBeforeTestInstantiationCallback",
							"constructor-1"
		);
		// @formatter:on
	}

	@Test
	void beforeTestInstantiationCallbackInSubclass() {
		// @formatter:off
		assertBeforeTestInstantiationCallback(SecondLevelTestCase.class,
				"fooBeforeAllCallback",
				"barBeforeAllCallback",
				"bazBeforeAllCallback",
					"beforeAllMethod-1",
					"beforeAllMethod-2",
						"fooBeforeTestInstantiationCallback",
						"barBeforeTestInstantiationCallback",
						"bazBeforeTestInstantiationCallback",
							"constructor-1",
							"constructor-2",
					"afterAllMethod-2"
		);
		// @formatter:on
	}

	@Test
	void beforeAllMethodThrowsAnException() {
		// @formatter:off
		assertBeforeTestInstantiationCallback(ExceptionInBeforeTestInstantiationTestCase.class, 0,
				"fooBeforeAllCallback",
					"fooBeforeTestInstantiationCallback",
						"exceptionThrowingBeforeTestInstantiationCallback",  // throws an exception.
							// test should not get invoked.
				"afterAllMethod"
		);
		// @formatter:on
	}

	private void assertBeforeTestInstantiationCallback(Class<?> testClass, String... expectedCalls) {
		assertBeforeTestInstantiationCallback(testClass, 1, expectedCalls);
	}

	private void assertBeforeTestInstantiationCallback(Class<?> testClass, int testsSuccessful,
			String... expectedCalls) {
		callSequence.clear();
		LauncherDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(testsSuccessful, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");

		assertEquals(asList(expectedCalls), callSequence, () -> "wrong call sequence for " + testClass.getName());
	}

	// -------------------------------------------------------------------------

	// Must NOT be private; otherwise, the @Test method gets discovered but never executed.
	@ExtendWith({ FooClassLevelCallbacks.class, BarClassLevelCallbacks.class })
	static class TopLevelTestCase {

		public TopLevelTestCase() {
			callSequence.add("constructor-1");
		}

		@BeforeAll
		static void beforeAll1() {
			callSequence.add("beforeAllMethod-1");
		}

		@Test
		void test() {
		}
	}

	// Must NOT be private; otherwise, the @Test method gets discovered but never executed.
	@ExtendWith(BazClassLevelCallbacks.class)
	static class SecondLevelTestCase extends TopLevelTestCase {

		public SecondLevelTestCase() {
			callSequence.add("constructor-2");
		}

		@BeforeAll
		static void beforeAll2() {
			callSequence.add("beforeAllMethod-2");
		}

		@AfterAll
		static void afterAll2() {
			callSequence.add("afterAllMethod-2");
		}
	}

	@ExtendWith({ FooClassLevelCallbacks.class, ExceptionThrowingBeforeTestInstantiationCallback.class })
	private static class ExceptionInBeforeTestInstantiationTestCase {

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

	private static class FooClassLevelCallbacks implements BeforeAllCallback, BeforeTestInstantiationCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("fooBeforeAllCallback");
		}

		@Override
		public void beforeTestInstantiation(ContainerExtensionContext context) throws Exception {
			callSequence.add("fooBeforeTestInstantiationCallback");
		}
	}

	private static class BarClassLevelCallbacks implements BeforeAllCallback, BeforeTestInstantiationCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("barBeforeAllCallback");
		}

		@Override
		public void beforeTestInstantiation(ContainerExtensionContext context) throws Exception {
			callSequence.add("barBeforeTestInstantiationCallback");
		}
	}

	private static class BazClassLevelCallbacks implements BeforeAllCallback, BeforeTestInstantiationCallback {

		@Override
		public void beforeAll(ContainerExtensionContext testExecutionContext) {
			callSequence.add("bazBeforeAllCallback");
		}

		@Override
		public void beforeTestInstantiation(ContainerExtensionContext context) throws Exception {
			callSequence.add("bazBeforeTestInstantiationCallback");
		}
	}

	private static class ExceptionThrowingBeforeTestInstantiationCallback implements BeforeTestInstantiationCallback {

		@Override
		public void beforeTestInstantiation(ContainerExtensionContext context) throws Exception {
			callSequence.add("exceptionThrowingBeforeTestInstantiationCallback");
			throw new RuntimeException("BeforeTestInstantiationCallback");
		}
	}

}
