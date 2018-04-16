/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_FIXED_PARALLELISM;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_STRATEGY;
import static org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.type;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.annotation.SameThreadExecution;
import org.junit.platform.commons.annotation.UseResource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

class ParallelExecutionTests {

	@Test
	void successfulParallelTest(TestReporter reporter) {
		List<ExecutionEvent> executionEvents = execute(SuccessfulParallelTestCase.class);

		List<Instant> startedTimestamps = getTimestampsFor(executionEvents, event(test(), started()));
		List<Instant> finishedTimestamps = getTimestampsFor(executionEvents, event(test(), finishedSuccessfully()));
		reporter.publishEntry("startedTimestamps", startedTimestamps.toString());
		reporter.publishEntry("finishedTimestamps", finishedTimestamps.toString());

		assertThat(startedTimestamps).hasSize(3);
		assertThat(finishedTimestamps).hasSize(3);
		assertThat(startedTimestamps).allMatch(startTimestamp -> finishedTimestamps.stream().allMatch(
			finishedTimestamp -> !finishedTimestamp.isBefore(startTimestamp)));
		assertThat(getThreadNames(executionEvents)).hasSize(3);
	}

	@Test
	void failingTestWithoutLock() {
		List<ExecutionEvent> executionEvents = execute(FailingTestWithoutLock.class);
		assertThat(executionEvents.stream().filter(event(test(), finishedWithFailure())::matches)).hasSize(2);
	}

	@Test
	void successfulTestWithMethodLock() {
		List<ExecutionEvent> executionEvents = execute(SuccessfulTestWithMethodLock.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(getThreadNames(executionEvents)).hasSize(3);
	}

	@Test
	void successfulTestWithClassLock() {
		List<ExecutionEvent> executionEvents = execute(SuccessfulTestWithClassLock.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(getThreadNames(executionEvents)).hasSize(1);
	}

	@Test
	void testCaseWithFactory() {
		List<ExecutionEvent> executionEvents = execute(TestCaseWithTestFactory.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(getThreadNames(executionEvents)).hasSize(1);
	}

	private List<Instant> getTimestampsFor(List<ExecutionEvent> executionEvents, Condition<ExecutionEvent> condition) {
		// @formatter:off
		return executionEvents.stream()
				.filter(condition::matches)
				.map(ExecutionEvent::getTimestamp)
				.collect(toList());
		// @formatter:on
	}

	private Stream<String> getThreadNames(List<ExecutionEvent> executionEvents) {
		// @formatter:off
		return executionEvents.stream()
				.filter(type(REPORTING_ENTRY_PUBLISHED)::matches)
				.map(event -> event.getPayload(ReportEntry.class).orElse(null))
				.map(ReportEntry::getKeyValuePairs)
				.filter(keyValuePairs -> keyValuePairs.containsKey("thread"))
				.map(keyValuePairs -> keyValuePairs.get("thread"))
				.distinct();
		// @formatter:on
	}

	static class SuccessfulParallelTestCase {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementAndBlock(sharedResource, countDownLatch, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementAndBlock(sharedResource, countDownLatch, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementAndBlock(sharedResource, countDownLatch, reporter);
		}
	}

	static class FailingTestWithoutLock {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}
	}

	static class SuccessfulTestWithMethodLock {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		@UseResource("sharedResource")
		void firstTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}

		@Test
		@UseResource("sharedResource")
		void secondTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}

		@Test
		@UseResource("sharedResource")
		void thirdTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}
	}

	@UseResource("sharedResource")
	static class SuccessfulTestWithClassLock {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch, reporter);
		}
	}

	static class TestCaseWithTestFactory {
		@TestFactory
		@SameThreadExecution
		Stream<DynamicTest> testFactory(TestReporter testReporter) {
			AtomicInteger sharedResource = new AtomicInteger(0);
			CountDownLatch countDownLatch = new CountDownLatch(3);
			return IntStream.range(0, 3).mapToObj(i -> dynamicTest("test " + i, () -> {
				incrementBlockAndCheck(sharedResource, countDownLatch, testReporter);
			}));
		}
	}

	private static void incrementBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch,
			TestReporter reporter) throws InterruptedException {
		int value = incrementAndBlock(sharedResource, countDownLatch, reporter);
		assertEquals(value, sharedResource.get());
	}

	private static int incrementAndBlock(AtomicInteger sharedResource, CountDownLatch countDownLatch,
			TestReporter reporter) throws InterruptedException {
		reporter.publishEntry("thread", Thread.currentThread().getName());
		int value = sharedResource.incrementAndGet();
		countDownLatch.countDown();
		countDownLatch.await(1, SECONDS);
		return value;
	}

	private List<ExecutionEvent> execute(Class<?> testClass) {
		// @formatter:off
		LauncherDiscoveryRequest discoveryRequest = request()
				.selectors(selectClass(testClass))
				.configurationParameter(PARALLEL_EXECUTION_ENABLED, String.valueOf(true))
				.configurationParameter(PARALLEL_CONFIG_STRATEGY, "fixed")
				.configurationParameter(PARALLEL_CONFIG_FIXED_PARALLELISM, String.valueOf(3))
				.build();
		// @formatter:on
		return ExecutionEventRecorder.execute(new JupiterTestEngine(), discoveryRequest);
	}

}
