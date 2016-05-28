/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listener;

import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptorStub;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.JavaClassSource;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.listeners.SummaryGeneratingListener;

/**
 * @since 5.0
 */
class SummaryGenerationTests {

	SummaryGeneratingListener listener = new SummaryGeneratingListener();
	TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Test
	void emptyReport() throws Exception {
		listener.testPlanExecutionStarted(testPlan);
		listener.testPlanExecutionFinished(testPlan);

		assertEquals(0, listener.getSummary().getTestsFailedCount());

		String summaryString = summaryAsString();
		assertAll("summary", //
			() -> assertTrue(summaryString.contains("Test run finished after"), "test run"), //
			() -> assertTrue(summaryString.contains("0 tests found"), "tests found"), //
			() -> assertTrue(summaryString.contains("0 tests skipped"), "tests skipped"), //
			() -> assertTrue(summaryString.contains("0 tests started"), "tests started"), //
			() -> assertTrue(summaryString.contains("0 tests aborted"), "tests aborted"), //
			() -> assertTrue(summaryString.contains("0 tests successful"), "tests successful"), //
			() -> assertTrue(summaryString.contains("0 tests failed"), "tests failed") //
		);

		assertEquals("", failuresAsString());
	}

	@Test
	void reportingCorrectCounts() throws Exception {
		TestIdentifier successful = createTestIdentifier("1");
		TestIdentifier failed = createTestIdentifier("2");
		TestIdentifier aborted = createTestIdentifier("3");
		TestIdentifier skipped = createTestIdentifier("4");

		listener.testPlanExecutionStarted(testPlan);
		listener.executionStarted(successful);
		listener.executionFinished(successful, TestExecutionResult.successful());
		listener.executionStarted(failed);
		listener.executionFinished(failed, TestExecutionResult.failed(new RuntimeException("failed")));
		listener.executionStarted(aborted);
		listener.executionFinished(aborted, TestExecutionResult.aborted(new RuntimeException("aborted")));
		listener.executionSkipped(skipped, "skipped");
		listener.testPlanExecutionFinished(testPlan);

		String summaryString = summaryAsString();
		assertAll("summary", //
			() -> assertTrue(summaryString.contains("4 tests found"), "tests found"), //
			() -> assertTrue(summaryString.contains("1 tests skipped"), "tests skipped"), //
			() -> assertTrue(summaryString.contains("3 tests started"), "tests started"), //
			() -> assertTrue(summaryString.contains("1 tests aborted"), "tests aborted"), //
			() -> assertTrue(summaryString.contains("1 tests successful"), "tests successful"), //
			() -> assertTrue(summaryString.contains("1 tests failed"), "tests failed") //
		);
	}

	@Test
	void reportingCorrectFailures() throws Exception {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.root("root", "2"), "failingTest") {

			@Override
			public Optional<TestSource> getSource() {
				return Optional.of(new JavaClassSource(Object.class));
			}
		};
		TestIdentifier failed = TestIdentifier.from(testDescriptor);
		TestIdentifier aborted = TestIdentifier.from(new TestDescriptorStub(UniqueId.root("root", "3"), "abortedTest"));

		listener.testPlanExecutionStarted(testPlan);
		listener.executionStarted(failed);
		listener.executionFinished(failed, TestExecutionResult.failed(new RuntimeException("failed")));
		listener.executionStarted(aborted);
		listener.executionFinished(aborted, TestExecutionResult.aborted(new RuntimeException("aborted")));
		listener.testPlanExecutionFinished(testPlan);

		// An aborted test is not a failure
		assertEquals(1, listener.getSummary().getTestsFailedCount());

		String failuresString = failuresAsString();
		assertAll("failures", //
			() -> assertTrue(failuresString.contains("Test failures (1)"), "test failures"), //
			() -> assertTrue(failuresString.contains(Object.class.getName()), "source"), //
			() -> assertTrue(failuresString.contains("failingTest"), "display name"), //
			() -> assertTrue(failuresString.contains("=> Exception: failed"), "exception") //
		);
	}

	private TestIdentifier createTestIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier.from(
			new TestDescriptorStub(UniqueId.root("root", uniqueId), "displayName"));
		testPlan.add(identifier);
		return identifier;
	}

	private String summaryAsString() {
		StringWriter summaryWriter = new StringWriter();
		listener.getSummary().printTo(new PrintWriter(summaryWriter));
		return summaryWriter.toString();
	}

	private String failuresAsString() {
		StringWriter failuresWriter = new StringWriter();
		listener.getSummary().printFailuresTo(new PrintWriter(failuresWriter));
		return failuresWriter.toString();
	}

}
