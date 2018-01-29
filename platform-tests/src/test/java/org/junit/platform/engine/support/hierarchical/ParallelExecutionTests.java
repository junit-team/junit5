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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.type;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.annotation.UseResource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

class ParallelExecutionTests {

	@Test
	void failingTestWithoutLock() {
		List<ExecutionEvent> executionEvents = execute(FailingTestWithoutLock.class);
		assertThat(executionEvents.stream().filter(event(test(), finishedWithFailure())::matches)).hasSize(2);
	}

	@Test
	void successfulTestWithMethodLock() {
		List<ExecutionEvent> executionEvents = execute(SuccessfulTestWithMethodLock.class);
		// TODO check concurrent execution
		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(getThreadNames(executionEvents)).hasSize(3);
	}

	@Test
	void successfulTestWithClassLock() {
		List<ExecutionEvent> executionEvents = execute(SuccessfulTestWithClassLock.class);
		// TODO check sequential execution
		assertThat(executionEvents.stream().filter(event(test(), finishedSuccessfully())::matches)).hasSize(3);
		assertThat(getThreadNames(executionEvents)).hasSize(1);
	}

	private Stream<String> getThreadNames(List<ExecutionEvent> executionEvents) {
		return executionEvents.stream().filter(type(REPORTING_ENTRY_PUBLISHED)::matches).map(
			event -> event.getPayload(ReportEntry.class).orElse(null)).map(ReportEntry::getKeyValuePairs).map(
				keyValuePairs -> keyValuePairs.get("thread")).distinct();
	}

	static class FailingTestWithoutLock {

		static AtomicInteger sharedResource = new AtomicInteger();

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}
	}

	static class SuccessfulTestWithMethodLock {

		static AtomicInteger sharedResource = new AtomicInteger();

		@Test
		@UseResource("sharedResource")
		void firstTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		@UseResource("sharedResource")
		void secondTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		@UseResource("sharedResource")
		void thirdTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}
	}

	@UseResource("sharedResource")
	static class SuccessfulTestWithClassLock {

		static AtomicInteger sharedResource = new AtomicInteger();

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}
	}

	private static void incrementWaitAndCheck(AtomicInteger sharedResource, TestReporter reporter)
			throws InterruptedException {
		int value = sharedResource.incrementAndGet();
		Thread.sleep(1000);
		assertEquals(value, sharedResource.get());
		reporter.publishEntry("thread", Thread.currentThread().getName());
	}

	private List<ExecutionEvent> execute(Class<?> testClass) {
		LauncherDiscoveryRequest discoveryRequest = request().selectors(selectClass(testClass)).build();
		return ExecutionEventRecorder.execute(new JupiterTestEngine(), discoveryRequest);
	}

}
