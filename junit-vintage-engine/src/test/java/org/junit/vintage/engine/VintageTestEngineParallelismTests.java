/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.IntSummaryStatistics;
import java.util.concurrent.atomic.LongAdder;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.vintage.engine.samples.junit4.ConcurrencyTests.A;
import org.junit.vintage.engine.samples.junit4.ConcurrencyTests.B;

/**
 * Tests to ensure that vintage JUnit tests can run in parallel.
 * At the moment, only concurrency of top-level containers is supported.
 *
 * @since 5.8
 */
class VintageTestEngineParallelismTests {

	private static final String PARALLELISM = "junit.jupiter.execution.parallel.config.fixed.parallelism";
	private static final String PARALLEL_STRATEGY = "junit.jupiter.execution.parallel.config.strategy";
	private static final String DEFAULT_PARALLEL_MODE = "junit.jupiter.execution.parallel.mode.default";
	private static final String PARALLEL_EXECUTION_ENABLED = "junit.jupiter.execution.parallel.enabled";

	/**
	 * Verify that if parallel execution is enabled, tests from separate classes will run concurrently.
	 */
	@Test
	void verifyTestsRunConcurrently() {
		// given
		final LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request().selectors(
			selectClass(A.class), selectClass(B.class)).configurationParameter(PARALLEL_EXECUTION_ENABLED,
				"true").configurationParameter(DEFAULT_PARALLEL_MODE, "concurrent").configurationParameter(
					PARALLEL_STRATEGY, "fixed").configurationParameter(PARALLELISM, "2").build();
		final TestEngine engine = new VintageTestEngine();
		final TestDescriptor descriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()));

		final CountingListener listener = new CountingListener();
		final ExecutionRequest executionRequest = new ExecutionRequest(descriptor, listener,
			discoveryRequest.getConfigurationParameters());

		// when
		engine.execute(executionRequest);

		// then
		assertEquals(2, listener.getMaxConcurrentTests());
		assertEquals(2, listener.getMaxConcurrentClasses());
	}

	/**
	 * Verify that if parallel execution is disabled, tests will run sequentially.
	 */
	@Test
	void verifyTestsRunSequentially() {
		// given
		final LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request().selectors(
			selectClass(A.class), selectClass(B.class)).configurationParameter(PARALLEL_EXECUTION_ENABLED,
				"false").build();
		final TestEngine engine = new VintageTestEngine();
		final TestDescriptor descriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()));

		final CountingListener listener = new CountingListener();
		final ExecutionRequest executionRequest = new ExecutionRequest(descriptor, listener,
			discoveryRequest.getConfigurationParameters());

		// when
		engine.execute(executionRequest);

		// then
		assertEquals(1, listener.getMaxConcurrentTests());
		assertEquals(1, listener.getMaxConcurrentClasses());
	}

	/**
	 * {@link EngineExecutionListener} that exposes that maximum number of concurrent classes and tests.
	 *
	 * @since 5.8
	 */
	protected class CountingListener implements EngineExecutionListener {

		private final IntSummaryStatistics classSummary = new IntSummaryStatistics();
		private final IntSummaryStatistics testSummary = new IntSummaryStatistics();
		private final LongAdder testAdder = new LongAdder();
		private final LongAdder classAdder = new LongAdder();

		/**
		 * @return the largest number of tests that ran at the same time.
		 */
		public int getMaxConcurrentTests() {
			return testSummary.getMax();
		}

		/**
		 * @return the largest number of classes that ran at the same time.
		 */
		public int getMaxConcurrentClasses() {
			return classSummary.getMax();
		}

		public void executionStarted(final TestDescriptor testDescriptor) {
			synchronized (this) {
				final String displayName = testDescriptor.getDisplayName();
				if (testDescriptor.isTest()) {
					testAdder.increment();
					testSummary.accept(testAdder.intValue());
				}
				else if (displayName.equals("A") || displayName.equals("B")) {
					classAdder.increment();
					classSummary.accept(classAdder.intValue());
				}
			}
		}

		public void executionFinished(final TestDescriptor testDescriptor,
				final TestExecutionResult testExecutionResult) {
			synchronized (this) {
				final String displayName = testDescriptor.getDisplayName();
				assertEquals(Status.SUCCESSFUL, testExecutionResult.getStatus());
				if (testDescriptor.isTest()) {
					testAdder.decrement();
				}
				else if (displayName.equals("A") || displayName.equals("B")) {
					classAdder.decrement();
				}
			}
		}
	}
}
