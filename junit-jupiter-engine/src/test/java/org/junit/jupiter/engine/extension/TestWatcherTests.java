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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

class TestWatcherTests extends AbstractJupiterTestEngineTests {

	@Test
	void testWatcherValidityIncludingNestedTest() {
		ExecutionEventRecorder recorder = executeTestsForClass(TestWatcherValidityTestCase.class);
		assertEquals(0, recorder.getContainerFailedCount());
	}

	@Test
	void testWatcherExceptionsAreLoggedAndSwallowedTest() {

		List<String> testWatcherMethodNames = getTestWatcherMethodNames();

		LogRecordListener listener = new LogRecordListener();
		LoggerFactory.addListener(listener);
		ExecutionEventRecorder recorder = executeTestsForClass(TestWatcherSimpleExceptionHandlingTestCase.class);
		LoggerFactory.removeListener(listener);

		assertAll(
			() -> assertEquals(8,
				listener.stream().filter(l -> l.getSourceClassName().equals(TestMethodTestDescriptor.class.getName())
						&& l.getSourceMethodName().contains("invokeTestWatchers")
						&& l.getThrown() instanceof JUnitException
						&& testWatcherMethodNames.contains(l.getThrown().getStackTrace()[0].getMethodName())).count(),
				"Thrown exceptions were not logged properly."),
			() -> assertEquals(2, recorder.getTestFailedCount(), "Thrown exceptions were not successfully caught."));
	}

	static List<String> getTestWatcherMethodNames() {
		Method[] methods = TestWatcher.class.getDeclaredMethods();
		return Arrays.stream(methods).map(Method::getName).collect(Collectors.toList());
	}

	static class BaseTestWatcherNestedTestCase {

		@Test
		public void successfulTest() {
			//no-op
		}

		@Test
		public void failedTest() {
			fail("Must fail");
		}

		@Test
		public void abortedTest() {
			Assumptions.assumeTrue(false);
		}

		@Test
		@Disabled
		public void skippedTest() {
			//no-op
		}

		@Nested
		class SecondLevelTestWatcherTestCase {
			@Test
			public void successfulTest() {
				//no-op
			}

			@Test
			public void failedTest() {
				fail("Must fail");
			}

			@Test
			public void abortedTest() {
				Assumptions.assumeTrue(false);
			}

			@Test
			@Disabled
			public void skippedTest() {
				//no-op
			}
		}
	}

	@ExtendWith(TestWatcherValidityCheckingWatcher.class)
	static class TestWatcherValidityTestCase extends BaseTestWatcherNestedTestCase {
	}

	@ExtendWith(ExceptionThrowingTestWatcher.class)
	static class TestWatcherSimpleExceptionHandlingTestCase extends BaseTestWatcherNestedTestCase {
	}

	static class TestResultAggregator implements TestWatcher {

		protected Map<String, List<String>> results = new HashMap<>();

		@Override
		public void testSuccessful(ExtensionContext context) {
			storeResult("SUCCESSFUL", context.getUniqueId());
		}

		@Override
		public void testAborted(ExtensionContext context, Throwable cause) {
			storeResult("ABORTED", context.getUniqueId());
		}

		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			storeResult("FAILED", context.getUniqueId());
		}

		@Override
		public void testDisabled(ExtensionContext context, Optional<String> reason) {
			storeResult("SKIPPED", context.getUniqueId());
		}

		protected void storeResult(String status, String method) {
			List<String> l = results.computeIfAbsent(status, k -> new ArrayList<>());
			l.add(method);
			results.put(status, l);
		}
	}

	static class TestWatcherValidityCheckingWatcher extends TestResultAggregator implements AfterAllCallback {
		@Override
		public void afterAll(ExtensionContext context) {
			this.results.values().forEach(idList -> assertEquals(2, idList.size()));
		}
	}

	static class ExceptionThrowingTestWatcher implements TestWatcher {
		@Override
		public void testSuccessful(ExtensionContext context) {
			throw new JUnitException("Exception in testSuccessful ");
		}

		@Override
		public void testDisabled(ExtensionContext context, Optional<String> reason) {
			throw new JUnitException("Exception in testDisabled");
		}

		@Override
		public void testAborted(ExtensionContext context, Throwable cause) {
			throw new JUnitException("Exception in testAborted");
		}

		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			throw new JUnitException("Exception in testFailed");
		}
	}

}
