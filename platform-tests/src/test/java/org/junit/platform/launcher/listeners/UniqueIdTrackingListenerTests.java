/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.Event.byTestDescriptor;
import static org.junit.platform.testkit.engine.EventConditions.abortedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

/**
 * Integration tests for the {@link UniqueIdTrackingListener}.
 *
 * @since 1.8
 */
class UniqueIdTrackingListenerTests {

	private final String passingTestUid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase1]/[method:passingTest()]";
	private final String abortedTestUid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase1]/[method:abortedTest()]";
	private final String failingTestUid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase1]/[method:failingTest()]";
	private final String dynamicTest1Uid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase1]/[test-factory:dynamicTests()]/[dynamic-test:#1]";
	private final String dynamicTest2Uid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase1]/[test-factory:dynamicTests()]/[dynamic-test:#2]";
	private final String test1Uid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase2]/[method:test1()]";
	private final String test2Uid = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerTests$TestCase2]/[method:test2()]";

	private final List<String> expectedUniqueIds = List.of(passingTestUid, abortedTestUid, failingTestUid,
		dynamicTest1Uid, dynamicTest2Uid, test1Uid, test2Uid).stream().sorted().collect(toList());

	@Test
	void confirmExpectedUniqueIdsViaEngineTestKit() {
		// @formatter:off
		EngineTestKit.engine("junit-jupiter")
			.selectors(selectClass(TestCase1.class), selectClass(TestCase2.class))
			.execute()
			.testEvents()
			.assertStatistics(stats -> stats.started(7).skipped(1).aborted(1).succeeded(5).failed(1))
			.assertEventsMatchLoosely(
				event(test(uniqueId(passingTestUid)), finishedSuccessfully()),
				event(test(uniqueId(abortedTestUid)), abortedWithReason(instanceOf(TestAbortedException.class))),
				event(test(uniqueId(failingTestUid)), finishedWithFailure(instanceOf(AssertionFailedError.class))),
				event(test(uniqueId(dynamicTest1Uid)), finishedSuccessfully()),
				event(test(uniqueId(dynamicTest2Uid)), finishedSuccessfully()),
				event(test(uniqueId(test1Uid)), finishedSuccessfully()),
				event(test(uniqueId(test2Uid)), finishedSuccessfully())
			);
		// @formatter:on
	}

	@Test
	void verifyUniqueIdsAreTrackedViaLauncherApi() throws Exception {
		Path path = Paths.get("build", UniqueIdTrackingListener.DEFAULT_FILE_NAME);

		try {
			LauncherDiscoveryRequest request = request() //
					.selectors(selectClass(TestCase1.class), selectClass(TestCase2.class)) //
					.filters(includeEngines("junit-jupiter")).build();

			List<String> actualUniqueIds = execute(request);

			// Sanity check using the results of our local TestExecutionListener
			assertThat(actualUniqueIds).isEqualTo(expectedUniqueIds);

			// Check contents of the file generated by the UniqueIdTrackingListener
			assertLinesMatch(expectedUniqueIds, Files.readAllLines(path));
		}
		finally {
			Files.deleteIfExists(path);
		}
	}

	private static Condition<Event> uniqueId(String uniqueId) {
		return new Condition<>(
			byTestDescriptor(where(TestDescriptor::getUniqueId, uid -> uid.toString().equals(uniqueId))),
			"descriptor with uniqueId '%s'", uniqueId);
	}

	private static List<String> execute(LauncherDiscoveryRequest request) {
		List<String> uniqueIds = new ArrayList<>();
		LauncherFactory.create().execute(request, new TestExecutionListener() {
			@Override
			public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
				if (testIdentifier.isTest()) {
					uniqueIds.add(testIdentifier.getUniqueId());
				}
			}
		});
		return uniqueIds.stream().sorted().collect(toList());
	}

	static class TestCase1 {

		@Test
		void passingTest() {
		}

		@Test
		@Disabled("testing")
		void disabledTest() {
		}

		@Test
		void abortedTest() {
			assumeTrue(false);
		}

		@Test
		void failingTest() {
			fail();
		}

		@TestFactory
		Stream<DynamicTest> dynamicTests() {
			return Stream.of("cat", "dog").map(text -> dynamicTest(text, () -> assertEquals(3, text.length())));
		}
	}

	static class TestCase2 {

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}
	}

}
