/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.migrationsupport.rules;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.tck.ExecutionGraph;
import org.junit.platform.tck.ExecutionRecorder;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.tck.ExecutionEventConditions.event;
import static org.junit.platform.tck.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.tck.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.tck.ExecutionEventConditions.test;
import static org.junit.platform.tck.TestExecutionResultConditions.isA;
import static org.junit.platform.tck.TestExecutionResultConditions.message;

/**
 * Integration tests for {@link ExpectedExceptionSupport}.
 *
 * @since 5.0
 */
class ExpectedExceptionSupportTests {

	@Test
	void expectedExceptionIsProcessedCorrectly() {
		ExecutionGraph executionGraph = executeTestsForClass(ExpectedExceptionTestCase.class).getExecutionGraph();

		assertEquals(4, executionGraph.getTestExecutionsFinished().size(), "# tests started");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
			"# tests succeeded");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(),
			"# tests aborted");
		assertEquals(3, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
			"# tests failed");

		assertThat(executionGraph.getTestFinishedEvents(TestExecutionResult.Status.SUCCESSFUL)).have(
			event(test("correctExceptionExpectedThrown"), finishedSuccessfully()));

		assertThat(executionGraph.getTestFinishedEvents(TestExecutionResult.Status.FAILED))//
				.haveExactly(1, //
					event(test("noExceptionExpectedButThrown"), //
						finishedWithFailure(message("no exception expected")))) //
				.haveExactly(1, //
					event(test("exceptionExpectedButNotThrown"), //
						finishedWithFailure(allOf(isA(AssertionError.class), //
							message("Expected test to throw an instance of java.lang.RuntimeException"))))) //
				.haveExactly(1, //
					event(test("wrongExceptionExpected"), //
						finishedWithFailure(allOf(isA(AssertionError.class), //
							message(value -> value.contains("Expected: an instance of java.io.IOException"))))));
	}

	@Test
	void expectedExceptionSupportWithoutExpectedExceptionRule() {
		ExecutionGraph executionGraph = executeTestsForClass(
			ExpectedExceptionSupportWithoutExpectedExceptionRuleTestCase.class).getExecutionGraph();

		assertEquals(2, executionGraph.getTestExecutionsFinished().size(), "# tests started");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
			"# tests succeeded");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(),
			"# tests aborted");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
			"# tests failed");

		assertThat(executionGraph.getTestFinishedEvents(TestExecutionResult.Status.SUCCESSFUL)).have(
			event(test("success"), finishedSuccessfully()));

		assertThat(executionGraph.getTestFinishedEvents(TestExecutionResult.Status.FAILED))//
				.haveExactly(1, event(test("failure"), //
					finishedWithFailure(message("must fail"))));
	}

	private ExecutionRecorder executeTestsForClass(Class<?> testClass) {
		LauncherDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
		JupiterTestEngine engine = new JupiterTestEngine();
		TestDescriptor testDescriptor = engine.discover(request, UniqueId.forEngine(engine.getId()));
		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		engine.execute(new ExecutionRequest(testDescriptor, executionRecorder, request.getConfigurationParameters()));
		return executionRecorder;
	}

	@ExtendWith(ExpectedExceptionSupport.class)
	static class ExpectedExceptionTestCase {

		@Rule
		public ExpectedException thrown = ExpectedException.none();

		@Test
		void noExceptionExpectedButThrown() {
			throw new RuntimeException("no exception expected");
		}

		@Test
		void exceptionExpectedButNotThrown() {
			thrown.expect(RuntimeException.class);
		}

		@Test
		void wrongExceptionExpected() {
			thrown.expect(IOException.class);
			throw new RuntimeException("wrong exception");
		}

		@Test
		void correctExceptionExpectedThrown() {
			thrown.expect(RuntimeException.class);
			throw new RuntimeException("right exception");
		}

	}

	@ExtendWith(ExpectedExceptionSupport.class)
	static class ExpectedExceptionSupportWithoutExpectedExceptionRuleTestCase {

		@Test
		void success() {
			/* no-op */
		}

		@Test
		void failure() {
			fail("must fail");
		}

	}

}
