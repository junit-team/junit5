/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.allOf;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.DisabledIf;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Unit tests for {@link ScriptExecutionCondition}.
 *
 * @since 5.1
 */
class ScriptExecutionConditionTests extends AbstractJupiterTestEngineTests {

	@Test
	void executeSimpleTestCases() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(SimpleTestCases.class)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertAll("Summary of simple test cases run", //
			() -> assertEquals(3, eventRecorder.getTestStartedCount(), "# tests started"), //
			() -> assertEquals(1, eventRecorder.getTestSkippedCount(), "# tests skipped"), //
			() -> assertEquals(1, eventRecorder.getTestFailedCount(), "# tests started") //

		);

		assertRecordedExecutionEventsContainsExactly(eventRecorder.getFailedTestFinishedEvents(), //
			event(test("syntaxError"), //
				finishedWithFailure( //
					allOf( //
						isA(JUnitException.class), //
						message(value -> value.contains("syntax error")) //
					))));

	}

	@Test
	@EnabledIf("true")
	@DisabledIf("false")
	void annotationDefaultValues(TestInfo info) {
		EnabledIf e = info.getTestMethod().orElseThrow(Error::new).getDeclaredAnnotation(EnabledIf.class);
		assertEquals("Nashorn", e.engine());
		assertEquals("Script `{source}` evaluated to: {result}", e.reason());
		DisabledIf d = info.getTestMethod().orElseThrow(Error::new).getDeclaredAnnotation(DisabledIf.class);
		assertEquals("Nashorn", d.engine());
		assertEquals("Script `{source}` evaluated to: {result}", d.reason());
	}

	static class SimpleTestCases {

		@Test
		@EnabledIf("true")
		void testIsEnabled() {
		}

		@Test
		@DisabledIf("false")
		void testIsNotDisabled() {
		}

		@Test
		@EnabledIf("syntax error")
		void syntaxErrorFails() {
			fail("test must not be executed");
		}

		@Test
		@DisabledIf("junitConfigurationParameter.get('does-not-exist') == null")
		void accessingNonExistentJUnitConfigurationParameter() {
			fail("test must not be executed");
		}
	}

}
