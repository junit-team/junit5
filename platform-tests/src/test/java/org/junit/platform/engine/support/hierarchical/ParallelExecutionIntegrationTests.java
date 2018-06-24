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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.started;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.type;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 1.3
 */
class ParallelExecutionIntegrationTests {

	@Test
	void successfulParallelTest(TestReporter reporter) {
		List<ExecutionEvent> executionEvents = execute(3, SuccessfulParallelTestCase.class);

		List<Instant> startedTimestamps = getTimestampsFor(executionEvents, event(test(), started()));
		List<Instant> finishedTimestamps = getTimestampsFor(executionEvents, event(test(), finishedSuccessfully()));
		reporter.publishEntry("startedTimestamps", startedTimestamps.toString());
		reporter.publishEntry("finishedTimestamps", finishedTimestamps.toString());

		assertThat(startedTimestamps).hasSize(3);
		assertThat(finishedTimestamps).hasSize(3);
		assertThat(startedTimestamps).allMatch(startTimestamp -> finishedTimestamps.stream().allMatch(
			finishedTimestamp -> !finishedTimestamp.isBefore(startTimestamp)));
		assertThat(ThreadReporter.getThreadNames(executionEvents)).hasSize(3);
	}

	@Test
	void failingTestWithoutLock() {
		List<ExecutionEvent> executionEvents = execute(3, FailingWithoutLockTestCase.class);
		assertThat(executionEvents.stream().filter(event(test(), finishedWithFailure())::matches)).hasSize(2);
	}

	@Test
	void successfulTestWithMethodLock() {
		List<ExecutionEvent> executionEvents = execute(3, SuccessfulWithMethodLockTestCase.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(executionEvents)).hasSize(3);
	}

	@Test
	void successfulTestWithClassLock() {
		List<ExecutionEvent> executionEvents = execute(3, SuccessfulWithClassLockTestCase.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(executionEvents)).hasSize(1);
	}

	@Test
	void testCaseWithFactory() {
		List<ExecutionEvent> executionEvents = execute(3, TestCaseWithTestFactory.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(executionEvents)).hasSize(1);
	}

	@RepeatedTest(10)
	void mixingClassAndMethodLevelLocks() {
		List<ExecutionEvent> executionEvents = execute(4, TestCaseWithSortedLocks.class,
			TestCaseWithUnsortedLocks.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(6);
		assertThat(ThreadReporter.getThreadNames(executionEvents).count()).isLessThanOrEqualTo(2);
	}

	@RepeatedTest(10)
	void locksOnNestedTests() {
		List<ExecutionEvent> executionEvents = execute(3, TestCaseWithNestedLocks.class);

		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(6);
		assertThat(ThreadReporter.getThreadNames(executionEvents)).hasSize(1);
	}

	private List<Instant> getTimestampsFor(List<ExecutionEvent> executionEvents, Condition<ExecutionEvent> condition) {
		// @formatter:off
		return executionEvents.stream()
				.filter(condition::matches)
				.map(ExecutionEvent::getTimestamp)
				.collect(toList());
		// @formatter:on
	}

	@ExtendWith(ThreadReporter.class)
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
			incrementAndBlock(sharedResource, countDownLatch);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}
	}

	@ExtendWith(ThreadReporter.class)
	static class FailingWithoutLockTestCase {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}
	}

	@ExtendWith(ThreadReporter.class)
	static class SuccessfulWithMethodLockTestCase {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		@ResourceLock("sharedResource")
		void firstTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		@ResourceLock("sharedResource")
		void secondTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		@ResourceLock("sharedResource")
		void thirdTest(TestReporter reporter) throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}
	}

	@ExtendWith(ThreadReporter.class)
	@ResourceLock("sharedResource")
	static class SuccessfulWithClassLockTestCase {

		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		void firstTest() throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		void secondTest() throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		void thirdTest() throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}
	}

	static class TestCaseWithTestFactory {
		@TestFactory
		@Execution(SAME_THREAD)
		Stream<DynamicTest> testFactory(TestReporter testReporter) {
			AtomicInteger sharedResource = new AtomicInteger(0);
			CountDownLatch countDownLatch = new CountDownLatch(3);
			return IntStream.range(0, 3).mapToObj(i -> dynamicTest("test " + i, () -> {
				incrementBlockAndCheck(sharedResource, countDownLatch);
				testReporter.publishEntry(ThreadReporter.KEY, Thread.currentThread().getName());
			}));
		}
	}

	private static final ReentrantLock A = new ReentrantLock();
	private static final ReentrantLock B = new ReentrantLock();

	@ExtendWith(ThreadReporter.class)
	@ResourceLock("A")
	static class TestCaseWithSortedLocks {
		@ResourceLock("B")
		@Test
		void firstTest() {
			assertTrue(A.tryLock());
			assertTrue(B.tryLock());
		}

		@Execution(CONCURRENT)
		@ResourceLock("B")
		@Test
		void secondTest() {
			assertTrue(A.tryLock());
			assertTrue(B.tryLock());
		}

		@ResourceLock("B")
		@Test
		void thirdTest() {
			assertTrue(A.tryLock());
			assertTrue(B.tryLock());
		}

		@AfterEach
		void unlock() {
			B.unlock();
			A.unlock();
		}
	}

	@ExtendWith(ThreadReporter.class)
	@ResourceLock("B")
	static class TestCaseWithUnsortedLocks {
		@ResourceLock("A")
		@Test
		void firstTest() {
			assertTrue(B.tryLock());
			assertTrue(A.tryLock());
		}

		@Execution(CONCURRENT)
		@ResourceLock("A")
		@Test
		void secondTest() {
			assertTrue(B.tryLock());
			assertTrue(A.tryLock());
		}

		@ResourceLock("A")
		@Test
		void thirdTest() {
			assertTrue(B.tryLock());
			assertTrue(A.tryLock());
		}

		@AfterEach
		void unlock() {
			A.unlock();
			B.unlock();
		}
	}

	@ExtendWith(ThreadReporter.class)
	@ResourceLock("A")
	static class TestCaseWithNestedLocks {

		@ResourceLock("B")
		@Test
		void firstTest() {
			assertTrue(A.tryLock());
			assertTrue(B.tryLock());
		}

		@Execution(CONCURRENT)
		@ResourceLock("B")
		@Test
		void secondTest() {
			assertTrue(A.tryLock());
			assertTrue(B.tryLock());
		}

		@Test
		void thirdTest() {
			assertTrue(A.tryLock());
			assertTrue(B.tryLock());
		}

		@AfterEach
		void unlock() {
			A.unlock();
			B.unlock();
		}

		@Nested
		@ResourceLock("B")
		class B {

			@ResourceLock("A")
			@Test
			void firstTest() {
				assertTrue(B.tryLock());
				assertTrue(A.tryLock());
			}

			@ResourceLock("A")
			@Test
			void secondTest() {
				assertTrue(B.tryLock());
				assertTrue(A.tryLock());
			}

			@Test
			void thirdTest() {
				assertTrue(B.tryLock());
				assertTrue(A.tryLock());
			}
		}
	}

	private static void incrementBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch)
			throws InterruptedException {
		int value = incrementAndBlock(sharedResource, countDownLatch);
		assertEquals(value, sharedResource.get());
	}

	private static int incrementAndBlock(AtomicInteger sharedResource, CountDownLatch countDownLatch)
			throws InterruptedException {
		int value = sharedResource.incrementAndGet();
		countDownLatch.countDown();
		countDownLatch.await(1, SECONDS);
		return value;
	}

	private List<ExecutionEvent> execute(int parallelism, Class<?>... testClasses) {
		// @formatter:off
		LauncherDiscoveryRequest discoveryRequest = request()
				.selectors(Arrays.stream(testClasses).map(DiscoverySelectors::selectClass).collect(toList()))
				.configurationParameter(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, String.valueOf(true))
				.configurationParameter(PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, "fixed")
				.configurationParameter(PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, String.valueOf(parallelism))
				.build();
		// @formatter:on
		return ExecutionEventRecorder.execute(new JupiterTestEngine(), discoveryRequest);
	}

	static class ThreadReporter implements AfterTestExecutionCallback {

		public static final String KEY = "thread";

		private static Stream<String> getThreadNames(List<ExecutionEvent> executionEvents) {
			// @formatter:off
			return executionEvents.stream()
					.filter(type(REPORTING_ENTRY_PUBLISHED)::matches)
					.map(event -> event.getPayload(ReportEntry.class).orElse(null))
					.map(ReportEntry::getKeyValuePairs)
					.filter(keyValuePairs -> keyValuePairs.containsKey(KEY))
					.map(keyValuePairs -> keyValuePairs.get("thread"))
					.distinct();
			// @formatter:on
		}

		@Override
		public void afterTestExecution(ExtensionContext context) throws Exception {
			context.publishReportEntry("thread", Thread.currentThread().getName());
		}
	}

}
