/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class SummaryGenerationTests {

	private final SummaryGeneratingListener listener = new SummaryGeneratingListener();
	private final TestPlan testPlan = TestPlan.from(Collections.emptyList());

	@Test
	void emptyReport() {
		listener.testPlanExecutionStarted(testPlan);
		listener.testPlanExecutionFinished(testPlan);

		assertEquals(0, listener.getSummary().getTestsFailedCount());

		String summaryString = summaryAsString();
		assertAll("summary", //
			() -> assertTrue(summaryString.contains("Test run finished after"), "test run"), //

			() -> assertTrue(summaryString.contains("0 containers found"), "containers found"), //
			() -> assertTrue(summaryString.contains("0 containers skipped"), "containers skipped"), //
			() -> assertTrue(summaryString.contains("0 containers started"), "containers started"), //
			() -> assertTrue(summaryString.contains("0 containers aborted"), "containers aborted"), //
			() -> assertTrue(summaryString.contains("0 containers successful"), "containers successful"), //
			() -> assertTrue(summaryString.contains("0 containers failed"), "containers failed"), //

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
	void reportingCorrectCounts() {
		TestIdentifier successfulContainer = createContainerIdentifier("c1");
		TestIdentifier failedContainer = createContainerIdentifier("c2");
		TestIdentifier abortedContainer = createContainerIdentifier("c3");
		TestIdentifier skippedContainer = createContainerIdentifier("c4");

		TestIdentifier successfulTest = createTestIdentifier("t1");
		TestIdentifier failedTest = createTestIdentifier("t2");
		TestIdentifier abortedTest = createTestIdentifier("t3");
		TestIdentifier skippedTest = createTestIdentifier("t4");

		listener.testPlanExecutionStarted(testPlan);

		listener.executionSkipped(skippedContainer, "skipped");
		listener.executionSkipped(skippedTest, "skipped");

		listener.executionStarted(successfulContainer);
		listener.executionFinished(successfulContainer, TestExecutionResult.successful());

		listener.executionStarted(successfulTest);
		listener.executionFinished(successfulTest, TestExecutionResult.successful());

		listener.executionStarted(failedContainer);
		listener.executionFinished(failedContainer, TestExecutionResult.failed(new RuntimeException("failed")));

		listener.executionStarted(failedTest);
		listener.executionFinished(failedTest, TestExecutionResult.failed(new RuntimeException("failed")));

		listener.executionStarted(abortedContainer);
		listener.executionFinished(abortedContainer, TestExecutionResult.aborted(new RuntimeException("aborted")));

		listener.executionStarted(abortedTest);
		listener.executionFinished(abortedTest, TestExecutionResult.aborted(new RuntimeException("aborted")));

		listener.testPlanExecutionFinished(testPlan);

		String summaryString = summaryAsString();
		try {
			assertAll("summary", //
				() -> assertTrue(summaryString.contains("4 containers found"), "containers found"), //
				() -> assertTrue(summaryString.contains("1 containers skipped"), "containers skipped"), //
				() -> assertTrue(summaryString.contains("3 containers started"), "containers started"), //
				() -> assertTrue(summaryString.contains("1 containers aborted"), "containers aborted"), //
				() -> assertTrue(summaryString.contains("1 containers successful"), "containers successful"), //
				() -> assertTrue(summaryString.contains("1 containers failed"), "containers failed"), //

				() -> assertTrue(summaryString.contains("4 tests found"), "tests found"), //
				() -> assertTrue(summaryString.contains("1 tests skipped"), "tests skipped"), //
				() -> assertTrue(summaryString.contains("3 tests started"), "tests started"), //
				() -> assertTrue(summaryString.contains("1 tests aborted"), "tests aborted"), //
				() -> assertTrue(summaryString.contains("1 tests successful"), "tests successful"), //
				() -> assertTrue(summaryString.contains("1 tests failed"), "tests failed") //
			);
		}
		catch (AssertionError error) {
			System.err.println(summaryString);
			throw error;
		}
	}

	@Test
	void canGetListOfFailures() {
		RuntimeException failedException = new RuntimeException("Pow!");
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.root("root", "1"), "failingTest") {

			@Override
			public Optional<TestSource> getSource() {
				return Optional.of(ClassSource.from(Object.class));
			}
		};
		TestIdentifier failingTest = TestIdentifier.from(testDescriptor);
		listener.testPlanExecutionStarted(testPlan);
		listener.executionStarted(failingTest);
		listener.executionFinished(failingTest, TestExecutionResult.failed(failedException));
		listener.testPlanExecutionFinished(testPlan);
		final List<TestExecutionSummary.Failure> failures = listener.getSummary().getFailures();
		assertThat(failures).hasSize(1);
		assertThat(failures.get(0).getException()).isEqualTo(failedException);
		assertThat(failures.get(0).getTestIdentifier()).isEqualTo(failingTest);
	}

	@Test
	void reportingCorrectFailures() {
		IllegalArgumentException iaeCausedBy = new IllegalArgumentException("Illegal Argument Exception");
		RuntimeException failedException = new RuntimeException("Runtime Exception", iaeCausedBy);
		NullPointerException npeSuppressed = new NullPointerException("Null Pointer Exception");
		failedException.addSuppressed(npeSuppressed);

		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.root("root", "2"), "failingTest") {

			@Override
			public Optional<TestSource> getSource() {
				return Optional.of(ClassSource.from(Object.class));
			}
		};
		TestIdentifier failed = TestIdentifier.from(testDescriptor);
		TestIdentifier aborted = TestIdentifier.from(new TestDescriptorStub(UniqueId.root("root", "3"), "abortedTest"));

		listener.testPlanExecutionStarted(testPlan);
		listener.executionStarted(failed);
		listener.executionFinished(failed, TestExecutionResult.failed(failedException));
		listener.executionStarted(aborted);
		listener.executionFinished(aborted, TestExecutionResult.aborted(new RuntimeException("aborted")));
		listener.testPlanExecutionFinished(testPlan);

		// An aborted test is not a failure
		assertEquals(1, listener.getSummary().getTestsFailedCount());

		String failuresString = failuresAsString();
		assertAll("failures", //
			() -> assertTrue(failuresString.contains("Failures (1)"), "test failures"), //
			() -> assertTrue(failuresString.contains(Object.class.getName()), "source"), //
			() -> assertTrue(failuresString.contains("failingTest"), "display name"), //
			() -> assertTrue(failuresString.contains("=> " + failedException), "main exception"), //
			() -> assertTrue(failuresString.contains("Caused by: " + iaeCausedBy), "Caused by exception"), //
			() -> assertTrue(failuresString.contains("Suppressed: " + npeSuppressed), "Suppressed exception") //
		);
	}

	@Test
	public void reportingCircularFailure() {
		IllegalArgumentException iaeCausedBy = new IllegalArgumentException("Illegal Argument Exception");
		RuntimeException failedException = new RuntimeException("Runtime Exception", iaeCausedBy);
		NullPointerException npeSuppressed = new NullPointerException("Null Pointer Exception");
		failedException.addSuppressed(npeSuppressed);
		npeSuppressed.addSuppressed(iaeCausedBy);

		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.root("root", "2"), "failingTest") {

			@Override
			public Optional<TestSource> getSource() {
				return Optional.of(ClassSource.from(Object.class));
			}
		};
		TestIdentifier failed = TestIdentifier.from(testDescriptor);

		listener.testPlanExecutionStarted(testPlan);
		listener.executionStarted(failed);
		listener.executionFinished(failed, TestExecutionResult.failed(failedException));
		listener.testPlanExecutionFinished(testPlan);

		assertEquals(1, listener.getSummary().getTestsFailedCount());

		String failuresString = failuresAsString();
		assertAll("failures", //
			() -> assertTrue(failuresString.contains("Suppressed: " + npeSuppressed), "Suppressed exception"), //
			() -> assertTrue(failuresString.contains("Circular reference: " + iaeCausedBy), "Circular reference"), //
			() -> assertFalse(failuresString.contains("Caused by: "),
				"'Caused by: ' omitted because of Circular reference") //
		);
	}

	@SuppressWarnings("deprecation")
	private TestIdentifier createTestIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier.from(
			new TestDescriptorStub(UniqueId.root("test", uniqueId), uniqueId));
		testPlan.add(identifier);
		return identifier;
	}

	@SuppressWarnings("deprecation")
	private TestIdentifier createContainerIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier.from(
			new TestDescriptorStub(UniqueId.root("container", uniqueId), uniqueId) {

				@Override
				public Type getType() {
					return Type.CONTAINER;
				}
			});
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
