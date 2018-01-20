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

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedSuccessfully;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.rules.ExpectedException;

/**
 * Integration tests for {@link ExpectedExceptionSupport}.
 *
 * @since 5.0
 */
class ExpectedExceptionSupportTests {

	@Test
	void expectedExceptionIsProcessedCorrectly() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(ExpectedExceptionTestCase.class);

		assertEquals(4, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(3, eventRecorder.getTestFailedCount(), "# tests failed");

		assertThat(eventRecorder.getSuccessfulTestFinishedEvents()).have(
			event(test("correctExceptionExpectedThrown"), finishedSuccessfully()));

		assertThat(eventRecorder.getFailedTestFinishedEvents())//
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
		ExecutionEventRecorder eventRecorder = executeTestsForClass(
			ExpectedExceptionSupportWithoutExpectedExceptionRuleTestCase.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertThat(eventRecorder.getSuccessfulTestFinishedEvents()).have(
			event(test("success"), finishedSuccessfully()));

		assertThat(eventRecorder.getFailedTestFinishedEvents())//
				.haveExactly(1, event(test("failure"), //
					finishedWithFailure(message("must fail"))));
	}

	private ExecutionEventRecorder executeTestsForClass(Class<?> testClass) {
		LauncherDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
		JupiterTestEngine engine = new JupiterTestEngine();
		TestDescriptor testDescriptor = engine.discover(request, UniqueId.forEngine(engine.getId()));
		ExecutionEventRecorder eventRecorder = new ExecutionEventRecorder();
		engine.execute(new ExecutionRequest(testDescriptor, eventRecorder, request.getConfigurationParameters()));
		return eventRecorder;
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
