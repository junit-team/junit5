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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

public class TestWatcherTests extends AbstractJupiterTestEngineTests {

	@Test
	void testWatcherValidityIncludingNestedTest() {
		ExecutionEventRecorder recorder = executeTestsForClass(nestedTestWatcherTestCase.class);
		assertEquals(0, recorder.getContainerFailedCount());
	}

	@ExtendWith(TestResultAggregator.class)
	static class nestedTestWatcherTestCase {

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
		class secondLevelTestWatcherTestCase {
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

	public static class TestResultAggregator implements TestWatcher, AfterAllCallback {

		private Map<String, List<String>> results = new HashMap<>();

		@Override
		public void testSuccessful(String descriptor, ExtensionContext context) {
			storeResult("SUCCESSFUL", descriptor);
		}

		@Override
		public void testAborted(String descriptor, Throwable cause, ExtensionContext context) {
			storeResult("ABORTED", descriptor);
		}

		@Override
		public void testFailed(String descriptor, Throwable cause, ExtensionContext context) {
			storeResult("FAILED", descriptor);
		}

		@Override
		public void testSkipped(String descriptor, Optional<String> Reason, ExtensionContext context) {
			storeResult("SKIPPED", descriptor);
		}

		private void storeResult(String status, String method) {
			List<String> l = results.get(status);
			if (l == null) {
				l = new ArrayList<>();
			}
			l.add(method);
			results.put(status, l);
		}

		@Override
		public void afterAll(ExtensionContext context) throws Exception {
			this.results.entrySet().stream().forEach(resultType -> assertEquals(2, resultType.getValue().size()));
		}
	}

}
