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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Integration tests for the {@link TestWatcher} extension API.
 *
 * @since 5.4
 */
class TestWatcherTests extends AbstractJupiterTestEngineTests {

	private static final List<String> testWatcherMethodNames = Arrays.stream(TestWatcher.class.getDeclaredMethods())//
			.map(Method::getName).collect(toList());

	@BeforeEach
	void clearResults() {
		TrackingTestWatcher.results.clear();
	}

	@Test
	void testWatcherInvokedForTestMethodsInTopLevelAndNestedTestClasses() {
		assertCommonStatistics(executeTestsForClass(TrackingTestWatcherTestCase.class));
		assertThat(TrackingTestWatcher.results.keySet()).containsAll(testWatcherMethodNames);
		TrackingTestWatcher.results.values().forEach(uidList -> assertEquals(2, uidList.size()));
	}

	@Test
	@TrackLogRecords
	void testWatcherExceptionsAreLoggedAndSwallowed(LogRecordListener logRecordListener) {
		assertCommonStatistics(executeTestsForClass(ExceptionThrowingTestWatcherTestCase.class));

		// @formatter:off
		long exceptionCount = logRecordListener.stream(TestMethodTestDescriptor.class, Level.WARNING)
				.map(LogRecord::getThrown)
				.filter(throwable -> throwable instanceof JUnitException)
				.filter(throwable -> testWatcherMethodNames.contains(throwable.getStackTrace()[0].getMethodName()))
				.count();
		// @formatter:on

		assertEquals(8, exceptionCount, "Thrown exceptions were not logged properly.");
	}

	private void assertCommonStatistics(EngineExecutionResults results) {
		results.containers().assertStatistics(stats -> stats.started(3).succeeded(3).failed(0));
		results.tests().assertStatistics(stats -> stats.skipped(2).started(6).succeeded(2).aborted(2).failed(2));
	}

	// -------------------------------------------------------------------------

	private static abstract class AbstractTestCase {

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
		class SecondLevel {

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

	@ExtendWith(TrackingTestWatcher.class)
	static class TrackingTestWatcherTestCase extends AbstractTestCase {
	}

	@ExtendWith(ExceptionThrowingTestWatcher.class)
	static class ExceptionThrowingTestWatcherTestCase extends AbstractTestCase {
	}

	private static class TrackingTestWatcher implements TestWatcher {

		private static final Map<String, List<String>> results = new HashMap<>();

		@Override
		public void testSuccessful(ExtensionContext context) {
			trackResult("testSuccessful", context.getUniqueId());
		}

		@Override
		public void testAborted(ExtensionContext context, Throwable cause) {
			trackResult("testAborted", context.getUniqueId());
		}

		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			trackResult("testFailed", context.getUniqueId());
		}

		@Override
		public void testDisabled(ExtensionContext context, Optional<String> reason) {
			trackResult("testDisabled", context.getUniqueId());
		}

		protected void trackResult(String status, String uid) {
			results.computeIfAbsent(status, k -> new ArrayList<>()).add(uid);
		}

	}

	private static class ExceptionThrowingTestWatcher implements TestWatcher {

		@Override
		public void testSuccessful(ExtensionContext context) {
			throw new JUnitException("Exception in testSuccessful()");
		}

		@Override
		public void testDisabled(ExtensionContext context, Optional<String> reason) {
			throw new JUnitException("Exception in testDisabled()");
		}

		@Override
		public void testAborted(ExtensionContext context, Throwable cause) {
			throw new JUnitException("Exception in testAborted()");
		}

		@Override
		public void testFailed(ExtensionContext context, Throwable cause) {
			throw new JUnitException("Exception in testFailed()");
		}

	}

}
