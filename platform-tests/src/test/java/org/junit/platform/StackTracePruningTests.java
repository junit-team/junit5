/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * Test cases for stacktrace pruning.
 *
 * <p>Note: the package {@code org.junit.platform} this class resides in is
 * chosen on purpose. If it was in {@code org.junit.platform.launcher}
 * stack traces would be fully pruned.
 *
 * @since 5.10
 */
class StackTracePruningTests {

	@Test
	void shouldPruneStackTraceByDefault() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.selectors(selectClass(FailingAssertionTestCase.class)) //
				.execute();

		List<StackTraceElement> stackTrace = extractStackTrace(results);

		assertStackTraceMatch(stackTrace, """
				\\Qorg.junit.jupiter.api.Assertions.fail(Assertions.java:\\E.+
				>>>>
				""");

		assertStackTraceDoesNotContain(stackTrace, "java.util.ArrayList.forEach(ArrayList.java:");
	}

	@Test
	void shouldPruneStackTraceWhenEnabled() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.configurationParameter("junit.platform.stacktrace.pruning.enabled", "true") //
				.selectors(selectClass(FailingAssertionTestCase.class)) //
				.execute();

		List<StackTraceElement> stackTrace = extractStackTrace(results);

		assertStackTraceMatch(stackTrace, """
				\\Qorg.junit.jupiter.api.Assertions.fail(Assertions.java:\\E.+
				>>>>
				""");

		assertStackTraceDoesNotContain(stackTrace, "java.base/java.util.ArrayList.forEach(ArrayList.java:");
	}

	@Test
	void shouldNotPruneStackTraceWhenDisabled() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.configurationParameter("junit.platform.stacktrace.pruning.enabled", "false") //
				.selectors(selectClass(FailingAssertionTestCase.class)) //
				.execute();

		List<StackTraceElement> stackTrace = extractStackTrace(results);

		assertStackTraceMatch(stackTrace, """
				\\Qorg.junit.jupiter.api.AssertionUtils.fail(AssertionUtils.java:\\E.+
				\\Qorg.junit.jupiter.api.Assertions.fail(Assertions.java:\\E.+
				>>>>
				\\Qjava.base/java.util.ArrayList.forEach(ArrayList.java:\\E.+
				>>>>
				""");
	}

	@Test
	void shouldPruneStackTraceAccordingToPattern() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.configurationParameter("junit.platform.stacktrace.pruning.enabled", "true") //
				.configurationParameter("junit.platform.stacktrace.pruning.pattern", "jdk.*") //
				.selectors(selectClass(FailingAssertionTestCase.class)) //
				.execute();

		List<StackTraceElement> stackTrace = extractStackTrace(results);

		assertStackTraceDoesNotContain(stackTrace, "jdk.");
	}

	@Test
	void shouldAlwaysKeepJupiterAssertionStackTraceElement() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.configurationParameter("junit.platform.stacktrace.pruning.enabled", "true") //
				.configurationParameter("junit.platform.stacktrace.pruning.pattern", "*") //
				.selectors(selectClass(FailingAssertionTestCase.class)) //
				.execute();

		List<StackTraceElement> stackTrace = extractStackTrace(results);

		assertStackTraceMatch(stackTrace, """
				\\Qorg.junit.jupiter.api.Assertions.fail(Assertions.java:\\E.+
				""");
	}

	@Test
	void shouldAlwaysKeepJupiterAssumptionStackTraceElement() {
		EngineExecutionResults results = EngineTestKit.engine("junit-jupiter") //
				.configurationParameter("junit.platform.stacktrace.pruning.enabled", "true") //
				.configurationParameter("junit.platform.stacktrace.pruning.pattern", "*") //
				.selectors(selectClass(FailingAssumptionTestCase.class)) //
				.execute();

		List<StackTraceElement> stackTrace = extractStackTrace(results);

		assertStackTraceMatch(stackTrace, """
				\\Qorg.junit.jupiter.api.Assumptions.assumeTrue(Assumptions.java:\\E.+
				""");
	}

	private static List<StackTraceElement> extractStackTrace(EngineExecutionResults results) {
		var failedTestEvent = results.testEvents().failed().list().get(0);
		var testResult = failedTestEvent.getRequiredPayload(TestExecutionResult.class);
		Throwable throwable = testResult.getThrowable().orElseThrow();
		return Arrays.asList(throwable.getStackTrace());
	}

	private static void assertStackTraceMatch(List<StackTraceElement> stackTrace, String expectedLines) {
		List<String> stackStraceAsLines = stackTrace.stream() //
				.map(StackTraceElement::toString) //
				.collect(Collectors.toList());
		assertLinesMatch(expectedLines.lines().toList(), stackStraceAsLines);
	}

	private static void assertStackTraceDoesNotContain(List<StackTraceElement> stackTrace, String element) {
		String stackStraceAsString = stackTrace.stream() //
				.map(StackTraceElement::toString) //
				.collect(Collectors.joining());
		assertThat(stackStraceAsString).doesNotContain(element);
	}

	// -------------------------------------------------------------------

	static class FailingAssertionTestCase {
		@Test
		void test() {
			Assertions.fail();
		}

	}

	static class FailingAssumptionTestCase {

		@Test
		void test() {
			Assumptions.assumeTrue(() -> {
				throw new RuntimeException();
			});
		}

	}

}
