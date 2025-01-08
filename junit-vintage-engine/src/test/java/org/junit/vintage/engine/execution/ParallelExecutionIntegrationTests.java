/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.started;
import static org.junit.vintage.engine.Constants.PARALLEL_EXECUTION_ENABLED;
import static org.junit.vintage.engine.Constants.PARALLEL_POOL_SIZE;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_RUNNER;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelTestCase.AbstractBlockingTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelTestCase.FirstTestCase;
import static org.junit.vintage.engine.samples.junit4.JUnit4ParallelTestCase.ThirdTestCase;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.junit.platform.testkit.engine.Events;
import org.junit.vintage.engine.VintageTestEngine;
import org.junit.vintage.engine.samples.junit4.JUnit4ParallelTestCase.SecondTestCase;

class ParallelExecutionIntegrationTests {

	@Test
	void executesTestClassesInParallel(TestReporter reporter) {
		AbstractBlockingTestCase.threadNames.clear();
		AbstractBlockingTestCase.countDownLatch = new CountDownLatch(3);

		var events = executeInParallelSuccessfully(3, FirstTestCase.class, SecondTestCase.class,
			ThirdTestCase.class).list();

		var startedTimestamps = getTimestampsFor(events, event(container(SEGMENT_TYPE_RUNNER), started()));
		var finishedTimestamps = getTimestampsFor(events,
			event(container(SEGMENT_TYPE_RUNNER), finishedSuccessfully()));
		var threadNames = new HashSet<>(AbstractBlockingTestCase.threadNames);

		reporter.publishEntry("startedTimestamps", startedTimestamps.toString());
		reporter.publishEntry("finishedTimestamps", finishedTimestamps.toString());

		assertThat(startedTimestamps).hasSize(3);
		assertThat(finishedTimestamps).hasSize(3);
		assertThat(startedTimestamps).allMatch(startTimestamp -> finishedTimestamps.stream().noneMatch(
			finishedTimestamp -> finishedTimestamp.isBefore(startTimestamp)));
		assertThat(threadNames).hasSize(3);
	}

	private List<Instant> getTimestampsFor(List<Event> events, Condition<Event> condition) {
		// @formatter:off
		return events.stream()
				.filter(condition::matches)
				.map(Event::getTimestamp)
				.toList();
		// @formatter:on
	}

	private Events executeInParallelSuccessfully(int poolSize, Class<?>... testClasses) {
		var events = execute(poolSize, testClasses).allEvents();
		try {
			return events.assertStatistics(it -> it.failed(0));
		}
		catch (AssertionError error) {
			events.debug();
			throw error;
		}
	}

	private static EngineExecutionResults execute(int poolSize, Class<?>... testClass) {
		return EngineTestKit.execute(new VintageTestEngine(), request(poolSize, testClass));
	}

	private static LauncherDiscoveryRequest request(int poolSize, Class<?>... testClasses) {
		var classSelectors = Arrays.stream(testClasses) //
				.map(DiscoverySelectors::selectClass) //
				.toArray(ClassSelector[]::new);

		return LauncherDiscoveryRequestBuilder.request() //
				.selectors(classSelectors) //
				.configurationParameter(PARALLEL_EXECUTION_ENABLED, String.valueOf(true)) //
				.configurationParameter(PARALLEL_POOL_SIZE, String.valueOf(poolSize)) //
				.build();
	}

}
