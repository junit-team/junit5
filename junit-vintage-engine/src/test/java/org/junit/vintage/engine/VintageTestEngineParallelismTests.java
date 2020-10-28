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
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

	private static final String PARALLELISM = "junit.vintage.execution.parallel.config.fixed.parallelism";
	private static final String PARALLEL_STRATEGY = "junit.vintage.execution.parallel.config.strategy";
	private static final String DEFAULT_PARALLEL_MODE = "junit.vintage.execution.parallel.mode.default";
	private static final String PARALLEL_EXECUTION_ENABLED = "junit.vintage.execution.parallel.enabled";

	/**
	 * Verify that if parallel execution is enabled, tests from separate classes will run concurrently.
	 */
	@Test
	void verifyTestsRunConcurrently() {
		// given
		LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request().selectors(
			selectClass(A.class), selectClass(B.class)).configurationParameter(PARALLEL_EXECUTION_ENABLED,
				"true").configurationParameter(DEFAULT_PARALLEL_MODE, "concurrent").configurationParameter(
					PARALLEL_STRATEGY, "fixed").configurationParameter(PARALLELISM, "2").build();
		TestEngine engine = new VintageTestEngine();
		TestDescriptor descriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()));

		CountingListener listener = new CountingListener(2);
		ExecutionRequest executionRequest = new ExecutionRequest(descriptor, listener,
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
		LauncherDiscoveryRequest discoveryRequest = LauncherDiscoveryRequestBuilder.request().selectors(
			selectClass(A.class), selectClass(B.class)).configurationParameter(PARALLEL_EXECUTION_ENABLED,
				"false").build();
		TestEngine engine = new VintageTestEngine();
		TestDescriptor descriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()));

		CountingListener listener = new CountingListener(1);
		ExecutionRequest executionRequest = new ExecutionRequest(descriptor, listener,
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
		private final CyclicBarrier barrier;

		/**
		 * @param methodConcurrency the number of test methods that may execute at the same time
		 */
		public CountingListener(int methodConcurrency) {
			this.barrier = new CyclicBarrier(methodConcurrency);
		}

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
			if (testDescriptor.isTest()) {
				incrementTests();
			}
			else if (!testDescriptor.isRoot()) {
				incrementClasses();
			}
		}

		public void executionFinished(final TestDescriptor testDescriptor,
				final TestExecutionResult testExecutionResult) {
			assertEquals(Status.SUCCESSFUL, testExecutionResult.getStatus());
			if (testDescriptor.isTest()) {
				decrementTests();
			}
			else if (!testDescriptor.isRoot()) {
				decrementClasses();
			}
		}

		protected void incrementTests() {
			synchronized (testSummary) {
				testAdder.increment();
				testSummary.accept(testAdder.intValue());
			}
		}

		protected void decrementTests() {
			try {
				barrier.await(100, TimeUnit.MILLISECONDS);
				synchronized (testSummary) {
					testAdder.decrement();
				}
			}
			catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}

		protected void incrementClasses() {
			synchronized (classSummary) {
				classAdder.increment();
				classSummary.accept(classAdder.intValue());
			}
		}

		protected void decrementClasses() {
			synchronized (classSummary) {
				classAdder.decrement();
			}
		}
	}

}
