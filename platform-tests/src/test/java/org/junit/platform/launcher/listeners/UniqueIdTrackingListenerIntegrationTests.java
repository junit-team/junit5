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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.listeners.UniqueIdTrackingListener.DEFAULT_FILE_NAME;
import static org.junit.platform.launcher.listeners.UniqueIdTrackingListener.LISTENER_ENABLED_PROPERTY_NAME;
import static org.junit.platform.launcher.listeners.UniqueIdTrackingListener.OUTPUT_DIR_PROPERTY_NAME;
import static org.junit.platform.launcher.listeners.UniqueIdTrackingListener.OUTPUT_FILE_PROPERTY_NAME;
import static org.junit.platform.testkit.engine.Event.byTestDescriptor;
import static org.junit.platform.testkit.engine.EventConditions.abortedWithReason;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.ClassSelector;
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
class UniqueIdTrackingListenerIntegrationTests {

	private static final String passingTest = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase1]/[method:passingTest()]";
	private static final String skippedTest = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase1]/[method:skippedTest()]";
	private static final String abortedTest = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase1]/[method:abortedTest()]";
	private static final String failingTest = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase1]/[method:failingTest()]";
	private static final String dynamicTest1 = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase1]/[test-factory:dynamicTests()]/[dynamic-test:#1]";
	private static final String dynamicTest2 = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase1]/[test-factory:dynamicTests()]/[dynamic-test:#2]";
	private static final String test1 = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase2]/[method:test1()]";
	private static final String test2 = "[engine:junit-jupiter]/[class:org.junit.platform.launcher.listeners.UniqueIdTrackingListenerIntegrationTests$TestCase2]/[method:test2()]";

	private static final String[] expectedUniqueIds = { passingTest, skippedTest, abortedTest, failingTest,
			dynamicTest1, dynamicTest2, test1, test2 };

	@Test
	void confirmExpectedUniqueIdsViaEngineTestKit() {
		// @formatter:off
		EngineTestKit.engine("junit-jupiter")
			.selectors(selectClasses())
			.execute()
			.testEvents()
			.assertStatistics(stats -> stats.started(7).skipped(1).aborted(1).succeeded(5).failed(1))
			.assertEventsMatchLoosely(
				event(test(uniqueId(passingTest)), finishedSuccessfully()),
				event(test(uniqueId(abortedTest)), abortedWithReason(instanceOf(TestAbortedException.class))),
				event(test(uniqueId(failingTest)), finishedWithFailure(instanceOf(AssertionFailedError.class))),
				event(test(uniqueId(dynamicTest1)), finishedSuccessfully()),
				event(test(uniqueId(dynamicTest2)), finishedSuccessfully()),
				event(test(uniqueId(test1)), finishedSuccessfully()),
				event(test(uniqueId(test2)), finishedSuccessfully())
			);
		// @formatter:on
	}

	@Test
	void listenerIsRegisteredButDisabledByDefault() throws Exception {
		long numListenersRegistered = ServiceLoader.load(TestExecutionListener.class).stream()//
				.filter(provider -> UniqueIdTrackingListener.class.equals(provider.type()))//
				.count();
		assertThat(numListenersRegistered).isEqualTo(1);

		Path path = Paths.get("build", DEFAULT_FILE_NAME);
		Files.deleteIfExists(path);

		try {
			List<String> actualUniqueIds = executeTests(Map.of());

			// Sanity check using the results of our local TestExecutionListener
			assertThat(actualUniqueIds).containsExactlyInAnyOrder(expectedUniqueIds);

			// Check that file was not generated by the UniqueIdTrackingListener
			assertThat(path).doesNotExist();
		}
		finally {
			Files.deleteIfExists(path);
		}
	}

	@Test
	void verifyUniqueIdsAreTrackedWithDefaults() throws Exception {
		Path path = Paths.get("build", DEFAULT_FILE_NAME);
		verifyUniqueIdsAreTracked(path, Map.of());
	}

	@Test
	void verifyUniqueIdsAreTrackedWithCustomOutputFile() throws Exception {
		String customFilename = "test_ids.txt";

		Path path = Paths.get("build", customFilename);
		verifyUniqueIdsAreTracked(path, Map.of(OUTPUT_FILE_PROPERTY_NAME, customFilename));
	}

	@Test
	void verifyUniqueIdsAreTrackedWithCustomOutputDir() throws Exception {
		String customDir = "build/UniqueIdTrackingListenerIntegrationTests";

		Path path = Paths.get(customDir, DEFAULT_FILE_NAME);
		verifyUniqueIdsAreTracked(path, Map.of(OUTPUT_DIR_PROPERTY_NAME, customDir));
	}

	@Test
	void verifyUniqueIdsAreTrackedWithCustomOutputFileAndCustomOutputDir() throws Exception {
		String customFilename = "test_ids.txt";
		String customDir = "build/UniqueIdTrackingListenerIntegrationTests";

		Path path = Paths.get(customDir, customFilename);
		verifyUniqueIdsAreTracked(path,
			Map.of(OUTPUT_DIR_PROPERTY_NAME, customDir, OUTPUT_FILE_PROPERTY_NAME, customFilename));
	}

	private void verifyUniqueIdsAreTracked(Path path, Map<String, String> configurationParameters) throws IOException {
		configurationParameters = new HashMap<>(configurationParameters);
		configurationParameters.put(LISTENER_ENABLED_PROPERTY_NAME, "true");

		Files.deleteIfExists(path);
		try {
			List<String> actualUniqueIds = executeTests(configurationParameters);

			// Sanity check using the results of our local TestExecutionListener
			assertThat(actualUniqueIds).containsExactlyInAnyOrder(expectedUniqueIds);

			// Check contents of the file generated by the UniqueIdTrackingListener
			assertThat(Files.readAllLines(path)).containsExactlyInAnyOrder(expectedUniqueIds);
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

	private static List<String> executeTests(Map<String, String> configurationParameters) {
		List<String> uniqueIds = new ArrayList<>();
		LauncherDiscoveryRequest request = request()//
				.selectors(selectClasses())//
				.filters(includeEngines("junit-jupiter"))//
				.configurationParameters(configurationParameters)//
				.build();
		LauncherFactory.create().execute(request, new TestExecutionListener() {

			@Override
			public void executionSkipped(TestIdentifier testIdentifier, String reason) {
				if (testIdentifier.isTest()) {
					uniqueIds.add(testIdentifier.getUniqueId());
				}
			}

			@Override
			public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
				if (testIdentifier.isTest()) {
					uniqueIds.add(testIdentifier.getUniqueId());
				}
			}
		});
		return uniqueIds;
	}

	private static ClassSelector[] selectClasses() {
		return new ClassSelector[] { selectClass(TestCase1.class), selectClass(TestCase2.class) };
	}

	// -------------------------------------------------------------------------

	static class TestCase1 {

		@Test
		void passingTest() {
		}

		@Test
		@Disabled("testing")
		void skippedTest() {
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
