/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junit.jupiter.engine.Constants.DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.DEFAULT_PARALLEL_EXECUTION_MODE;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME;
import static org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.container;
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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer.MethodName;
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
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

/**
 * @since 1.3
 */
class ParallelExecutionIntegrationTests {

	@Test
	void successfulParallelTest(TestReporter reporter) {
		var events = executeConcurrently(3, SuccessfulParallelTestCase.class);

		var startedTimestamps = getTimestampsFor(events, event(test(), started()));
		var finishedTimestamps = getTimestampsFor(events, event(test(), finishedSuccessfully()));
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
		var events = executeConcurrently(3, FailingWithoutLockTestCase.class);
		assertThat(events.stream().filter(event(test(), finishedWithFailure())::matches)).hasSize(2);
	}

	@Test
	void successfulTestWithMethodLock() {
		var events = executeConcurrently(3, SuccessfulWithMethodLockTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(3);
	}

	@Test
	void successfulTestWithClassLock() {
		var events = executeConcurrently(3, SuccessfulWithClassLockTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	@Test
	void testCaseWithFactory() {
		var events = executeConcurrently(3, TestCaseWithTestFactory.class);

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
			var events = executeConcurrently(3, SuccessfulWithMethodLockTestCase.class);

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
		var events = executeConcurrently(4, TestCaseWithSortedLocks.class, TestCaseWithUnsortedLocks.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(6);
		assertThat(ThreadReporter.getThreadNames(events).count()).isLessThanOrEqualTo(2);
	}

	@RepeatedTest(10)
	void locksOnNestedTests() {
		var events = executeConcurrently(3, TestCaseWithNestedLocks.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(6);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	@Test
	void afterHooksAreCalledAfterConcurrentDynamicTestsAreFinished() {
		var events = executeConcurrently(3, ConcurrentDynamicTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(1);
		var timestampedEvents = ConcurrentDynamicTestCase.events;
		assertThat(timestampedEvents.get("afterEach")).isAfterOrEqualTo(timestampedEvents.get("dynamicTestFinished"));
	}

	/**
	 * @since 1.4
	 * @see <a href="https://github.com/junit-team/junit5/issues/1688">gh-1688</a>
	 */
	@Test
	void threadInterruptedByUserCode() {
		var events = executeConcurrently(3, InterruptedThreadTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(4);
	}

	@Test
	void executesTestTemplatesWithResourceLocksInSameThread() {
		var events = executeConcurrently(2, ConcurrentTemplateTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(10);
		assertThat(ThreadReporter.getThreadNames(events)).hasSize(1);
	}

	@Test
	void executesClassesInParallelIfEnabledViaConfigurationParameter() {
		ParallelClassesTestCase.GLOBAL_BARRIER.reset();

		var configParams = Map.of(DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME, "concurrent");
		var results = executeWithFixedParallelism(3, configParams, ParallelClassesTestCaseA.class,
			ParallelClassesTestCaseB.class, ParallelClassesTestCaseC.class);

		results.testEvents().assertStatistics(stats -> stats.succeeded(9));
		assertThat(ThreadReporter.getThreadNames(results.allEvents().list())).hasSize(3);
		var testClassA = findFirstTestDescriptor(results, container(ParallelClassesTestCaseA.class));
		assertThat(ThreadReporter.getThreadNames(getEventsOfChildren(results, testClassA))).hasSize(1);
		var testClassB = findFirstTestDescriptor(results, container(ParallelClassesTestCaseB.class));
		assertThat(ThreadReporter.getThreadNames(getEventsOfChildren(results, testClassB))).hasSize(1);
		var testClassC = findFirstTestDescriptor(results, container(ParallelClassesTestCaseC.class));
		assertThat(ThreadReporter.getThreadNames(getEventsOfChildren(results, testClassC))).hasSize(1);
	}

	@Test
	void executesMethodsInParallelIfEnabledViaConfigurationParameter() {
		ParallelMethodsTestCase.barriersPerClass.clear();

		var configParams = Map.of( //
			DEFAULT_PARALLEL_EXECUTION_MODE, "concurrent", //
			DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME, "same_thread");
		var results = executeWithFixedParallelism(3, configParams, ParallelMethodsTestCaseA.class,
			ParallelMethodsTestCaseB.class, ParallelMethodsTestCaseC.class);

		results.testEvents().assertStatistics(stats -> stats.succeeded(9));
		assertThat(ThreadReporter.getThreadNames(results.allEvents().list())).hasSizeGreaterThanOrEqualTo(3);
		var testClassA = findFirstTestDescriptor(results, container(ParallelMethodsTestCaseA.class));
		assertThat(ThreadReporter.getThreadNames(getEventsOfChildren(results, testClassA))).hasSize(3);
		var testClassB = findFirstTestDescriptor(results, container(ParallelMethodsTestCaseB.class));
		assertThat(ThreadReporter.getThreadNames(getEventsOfChildren(results, testClassB))).hasSize(3);
		var testClassC = findFirstTestDescriptor(results, container(ParallelMethodsTestCaseC.class));
		assertThat(ThreadReporter.getThreadNames(getEventsOfChildren(results, testClassC))).hasSize(3);
	}

	@Test
	void canRunTestsIsolatedFromEachOther() {
		var events = executeConcurrently(2, IsolatedTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedWithFailure())::matches)).isEmpty();
	}

	@Test
	void canRunTestsIsolatedFromEachOtherWithNestedCases() {
		var events = executeConcurrently(4, NestedIsolatedTestCase.class);

		assertThat(events.stream().filter(event(test(), finishedWithFailure())::matches)).isEmpty();
	}

	@Test
	void canRunTestsIsolatedFromEachOtherAcrossClasses() {
		var events = executeConcurrently(4, IndependentClasses.A.class, IndependentClasses.B.class);

		assertThat(events.stream().filter(event(test(), finishedWithFailure())::matches)).isEmpty();
	}

	@RepeatedTest(10)
	void canRunTestsIsolatedFromEachOtherAcrossClassesWithOtherResourceLocks() {
		var events = executeConcurrently(4, IndependentClasses.B.class, IndependentClasses.C.class);

		assertThat(events.stream().filter(event(test(), finishedWithFailure())::matches)).isEmpty();
	}

	@Isolated("testing")
	static class IsolatedTestCase {
		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(2);
		}

		@Test
		void a() throws Exception {
			incrementBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		void b() throws Exception {
			storeAndBlockAndCheck(sharedResource, countDownLatch);
		}
	}

	static class NestedIsolatedTestCase {
		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeAll
		static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(6);
		}

		@Test
		void a() throws Exception {
			storeAndBlockAndCheck(sharedResource, countDownLatch);
		}

		@Test
		void b() throws Exception {
			storeAndBlockAndCheck(sharedResource, countDownLatch);
		}

		@Nested
		class Inner {

			@Test
			void a() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}

			@Test
			void b() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}

			@Nested
			@Isolated
			class InnerInner {

				@Test
				void a() throws Exception {
					incrementBlockAndCheck(sharedResource, countDownLatch);
				}

				@Test
				void b() throws Exception {
					storeAndBlockAndCheck(sharedResource, countDownLatch);
				}
			}
		}
	}

	static class IndependentClasses {
		static AtomicInteger sharedResource = new AtomicInteger();
		static CountDownLatch countDownLatch = new CountDownLatch(4);

		static class A {
			@Test
			void a() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}

			@Test
			void b() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}
		}

		@Isolated
		static class B {
			@Test
			void a() throws Exception {
				incrementBlockAndCheck(sharedResource, countDownLatch);
			}

			@Test
			void b() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}
		}

		@ResourceLock("other")
		static class C {
			@Test
			void a() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}

			@Test
			void b() throws Exception {
				storeAndBlockAndCheck(sharedResource, countDownLatch);
			}
		}
	}

	private List<Event> getEventsOfChildren(EngineExecutionResults results, TestDescriptor container) {
		return results.testEvents().filter(
			event -> event.getTestDescriptor().getParent().orElseThrow().equals(container)).collect(toList());
	}

	private TestDescriptor findFirstTestDescriptor(EngineExecutionResults results, Condition<Event> condition) {
		return results.allEvents().filter(condition::matches).map(Event::getTestDescriptor).findFirst().orElseThrow();
	}

	private List<Instant> getTimestampsFor(List<Event> events, Condition<Event> condition) {
		// @formatter:off
		return events.stream()
				.filter(condition::matches)
				.map(Event::getTimestamp)
				.collect(toList());
		// @formatter:on
	}

	private List<Event> executeConcurrently(int parallelism, Class<?>... testClasses) {
		return executeWithFixedParallelism(parallelism, Map.of(DEFAULT_PARALLEL_EXECUTION_MODE, "concurrent"),
			testClasses).allEvents().list();
	}

	private EngineExecutionResults executeWithFixedParallelism(int parallelism, Map<String, String> configParams,
			Class<?>... testClasses) {
		// @formatter:off
		var discoveryRequest = request()
				.selectors(Arrays.stream(testClasses).map(DiscoverySelectors::selectClass).collect(toList()))
				.configurationParameter(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, String.valueOf(true))
				.configurationParameter(PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, "fixed")
				.configurationParameter(PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, String.valueOf(parallelism))
				.configurationParameters(configParams)
				.build();
		// @formatter:on
		return EngineTestKit.execute("junit-jupiter", discoveryRequest);
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
			var sharedResource = new AtomicInteger(0);
			var countDownLatch = new CountDownLatch(3);
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

	@TestMethodOrder(MethodName.class)
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

	@ExtendWith(ThreadReporter.class)
	static abstract class BarrierTestCase {

		@Test
		void test1() throws Exception {
			getBarrier().await();
		}

		@Test
		void test2() throws Exception {
			getBarrier().await();
		}

		@Test
		void test3() throws Exception {
			getBarrier().await();
		}

		abstract CyclicBarrier getBarrier();

	}

	static class ParallelMethodsTestCase extends BarrierTestCase {

		static final Map<Class<?>, CyclicBarrier> barriersPerClass = new ConcurrentHashMap<>();

		@Override
		CyclicBarrier getBarrier() {
			return barriersPerClass.computeIfAbsent(this.getClass(), key -> new CyclicBarrier(3));
		}
	}

	static class ParallelClassesTestCase extends BarrierTestCase {

		static final CyclicBarrier GLOBAL_BARRIER = new CyclicBarrier(3);

		@Override
		CyclicBarrier getBarrier() {
			return GLOBAL_BARRIER;
		}

	}

	static class ParallelClassesTestCaseA extends ParallelClassesTestCase {
	}

	static class ParallelClassesTestCaseB extends ParallelClassesTestCase {
	}

	static class ParallelClassesTestCaseC extends ParallelClassesTestCase {
	}

	static class ParallelMethodsTestCaseA extends ParallelMethodsTestCase {
	}

	static class ParallelMethodsTestCaseB extends ParallelMethodsTestCase {
	}

	static class ParallelMethodsTestCaseC extends ParallelMethodsTestCase {
	}

	private static void incrementBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch)
			throws InterruptedException {
		var value = incrementAndBlock(sharedResource, countDownLatch);
		assertEquals(value, sharedResource.get());
	}

	private static int incrementAndBlock(AtomicInteger sharedResource, CountDownLatch countDownLatch)
			throws InterruptedException {
		var value = sharedResource.incrementAndGet();
		countDownLatch.countDown();
		countDownLatch.await(estimateSimulatedTestDurationInMiliseconds(), MILLISECONDS);
		return value;
	}

	private static void storeAndBlockAndCheck(AtomicInteger sharedResource, CountDownLatch countDownLatch)
			throws InterruptedException {
		var value = sharedResource.get();
		countDownLatch.countDown();
		countDownLatch.await(estimateSimulatedTestDurationInMiliseconds(), MILLISECONDS);
		assertEquals(value, sharedResource.get());
	}

	/**
	 * To simulate tests running in parallel tests will modify a shared
	 * resource, simulate work by waiting, then check if the shared resource was
	 * not modified by any other thread.
	 *
	 * Depending on system performance the simulation of work needs to be longer
	 * on slower systems to ensure tests can run in parallel.
	 *
	 * Currently CI is known to be slow.
	 */
	private static long estimateSimulatedTestDurationInMiliseconds() {
		var runningInCi = Boolean.valueOf(System.getenv("CI"));
		return runningInCi ? 1000 : 100;
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
