/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
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
import static org.junit.jupiter.engine.Constants.DEFAULT_PARALLEL_EXECUTION_MODE;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.EventConditions.type;
import static org.junit.platform.testkit.engine.EventType.REPORTING_ENTRY_PUBLISHED;

import java.net.URL;
import java.net.URLClassLoader;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

/**
 * @since 1.3
 */
class ParallelExecutionIntegrationTests {

	@Test
	void successfulParallelTest(TestReporter reporter) {
		List<Event> events = execute(3, SuccessfulParallelTestCase.class);

		List<Instant> startedTimestamps = getTimestampsFor(events, event(test(), started()));
		List<Instant> finishedTimestamps = getTimestampsFor(events, event(test(), finishedSuccessfully()));
		reporter.publishEntry("startedTimestamps", startedTimestamps.toString());
		reporter.publishEntry("finishedTimestamps", finishedTimestamps.toString());

		assertThat(startedTimestamps).hasSize(3);
		assertThat(finishedTimestamps).hasSize(3);
		assertThat(startedTimestamps).allMatch(startTimestamp -> finishedTimestamps.stream().noneMatch(
			finishedTimestamp -> finishedTimestamp.isBefore(startTimestamp)));
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(3);
	}

	@Test
	void failingTestWithoutLock() {
		List<Event> events = execute(3, FailingWithoutLockTestCase.class);
		assertThat(events.stream().filter(event(test(), finishedWithFailure())::matches)).hasSize(2);
	}

	@Test
	void successfulTestWithMethodLock() {
		List<Event> events = execute(3, SuccessfulWithMethodLockTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(3);
	}

	@Test
	void successfulTestWithClassLock() {
		List<Event> events = execute(3, SuccessfulWithClassLockTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	@Test
	void testCaseWithFactory() {
		List<Event> events = execute(3, TestCaseWithTestFactory.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	@Test
	void customContextClassLoader() {
		var currentThread = Thread.currentThread();
		var currentLoader = currentThread.getContextClassLoader();
		var smilingLoader = new URLClassLoader("(-:", new URL[0], ClassLoader.getSystemClassLoader());
		currentThread.setContextClassLoader(smilingLoader);
		try {
			var events = execute(3, SuccessfulWithMethodLockTestCase.class);

			assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
			assertThat(ThreadReporter.getThreadNames(events)).hasSize(3);
			assertThat(ThreadReporter.getLoaderNames(events)).containsExactly("(-:");
		}
		finally {
			currentThread.setContextClassLoader(currentLoader);
		}
	}

	@RepeatedTest(10)
	void mixingClassAndMethodLevelLocks() {
		List<Event> events = execute(4, TestCaseWithSortedLocks.class, TestCaseWithUnsortedLocks.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(6);
		assertThat(ThreadReporter.getThreadNames(events).count()).isLessThanOrEqualTo(2);
	}

	@RepeatedTest(10)
	void locksOnNestedTests() {
		List<Event> events = execute(3, TestCaseWithNestedLocks.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(6);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	@Test
	void afterHooksAreCalledAfterConcurrentDynamicTestsAreFinished() {
		List<Event> events = execute(3, ConcurrentDynamicTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(1);
		Map<String, Instant> timestampedEvents = ConcurrentDynamicTestCase.events;
		assertThat(timestampedEvents.get("afterEach")).isAfterOrEqualTo(timestampedEvents.get("dynamicTestFinished"));
	}

	/**
	 * @since 1.4
	 * @see <a href="https://github.com/junit-team/junit5/issues/1688">gh-1688</a>
	 */
	@Test
	void threadInterruptedByUserCode() {
		List<Event> events = execute(3, InterruptedThreadTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).size().isEqualTo(4);
	}

	@Test
	void executesTestTemplatesWithResourceLocksInSameThread() {
		List<Event> events = execute(2, ConcurrentTemplateTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).size().isEqualTo(10);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	private List<Instant> getTimestampsFor(List<Event> events, Condition<Event> condition) {
		// @formatter:off
		return events.stream()
				.filter(condition::matches)
				.map(Event::getTimestamp)
				.collect(toList());
		// @formatter:on
	}

	private List<Event> execute(int parallelism, Class<?>... testClasses) {
		// @formatter:off
		LauncherDiscoveryRequest discoveryRequest = request()
				.selectors(Arrays.stream(testClasses).map(DiscoverySelectors::selectClass).collect(toList()))
				.configurationParameter(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, String.valueOf(true))
				.configurationParameter(DEFAULT_PARALLEL_EXECUTION_MODE, "concurrent")
				.configurationParameter(PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, "fixed")
				.configurationParameter(PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, String.valueOf(parallelism))
				.build();
		// @formatter:on
		return EngineTestKit.execute("junit-jupiter", discoveryRequest).all().list();
	}

	// -------------------------------------------------------------------------

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
		void firstTest() throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}

		@Test
		void secondTest() throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}

		@Test
		void thirdTest() throws Exception {
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
		void firstTest() throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		@ResourceLock("sharedResource")
		void secondTest() throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		@ResourceLock("sharedResource")
		void thirdTest() throws Exception {
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
				testReporter.publishEntry("thread", Thread.currentThread().getName());
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

	@Execution(CONCURRENT)
	static class ConcurrentDynamicTestCase {
		static Map<String, Instant> events;

		@BeforeAll
		static void beforeAll() {
			events = new ConcurrentHashMap<>();
		}

		@AfterEach
		void afterEach() {
			events.put("afterEach", Instant.now());
		}

		@TestFactory
		DynamicTest testFactory() {
			return dynamicTest("slow", () -> {
				Thread.sleep(100);
				events.put("dynamicTestFinished", Instant.now());
			});
		}
	}

	@TestMethodOrder(Alphanumeric.class)
	static class InterruptedThreadTestCase {

		@Test
		void test1() {
			Thread.currentThread().interrupt();
		}

		@Test
		void test2() throws InterruptedException {
			Thread.sleep(10);
		}

		@Test
		void test3() {
			Thread.currentThread().interrupt();
		}

		@Test
		void test4() throws InterruptedException {
			Thread.sleep(10);
		}

	}

	@Execution(CONCURRENT)
	@ExtendWith(ThreadReporter.class)
	static class ConcurrentTemplateTestCase {
		@RepeatedTest(10)
		@ResourceLock("a")
		void repeatedTest() throws Exception {
			Thread.sleep(100);
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

	static class ThreadReporter implements AfterTestExecutionCallback {

		private static Stream<String> getLoaderNames(List<Event> events) {
			return getValues(events, "loader");
		}

		private static Stream<String> getThreadNames(List<Event> events) {
			return getValues(events, "thread");
		}

		private static Stream<String> getValues(List<Event> events, String key) {
			// @formatter:off
			return events.stream()
					.filter(type(REPORTING_ENTRY_PUBLISHED)::matches)
					.map(event -> event.getPayload(ReportEntry.class).orElseThrow())
					.map(ReportEntry::getKeyValuePairs)
					.filter(keyValuePairs -> keyValuePairs.containsKey(key))
					.map(keyValuePairs -> keyValuePairs.get(key))
					.distinct();
			// @formatter:on
		}

		@Override
		public void afterTestExecution(ExtensionContext context) {
			context.publishReportEntry("thread", Thread.currentThread().getName());
			context.publishReportEntry("loader", Thread.currentThread().getContextClassLoader().getName());
		}
	}

}
