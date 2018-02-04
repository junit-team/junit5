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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.assertRecordedExecutionEventsContainsExactly;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.event;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.finishedWithFailure;
import static org.junit.platform.engine.test.event.ExecutionEventConditions.test;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.isA;
import static org.junit.platform.engine.test.event.TestExecutionResultConditions.message;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ScriptEvaluationException;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.script.Script;
import org.junit.jupiter.engine.script.ScriptExecutionManager;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Unit tests for {@link ScriptExecutionCondition}.
 *
 * @since 5.1
 */
class ScriptExecutionConditionTests extends AbstractJupiterTestEngineTests {

	private final Bindings bindings = createDefaultContextBindings();
	private final ScriptExecutionCondition condition = new ScriptExecutionCondition();
	private final ScriptExecutionManager manager = new ScriptExecutionManager();

	@Test
	void computeConditionEvaluationResultWithDefaultReasonMessage() {
		Script script = script(EnabledIf.class, "?");
		String actual = condition.computeConditionEvaluationResult(script, "!").getReason().orElseThrow(Error::new);
		assertEquals("Script `?` evaluated to: !", actual);
	}

	@TestFactory
	Stream<DynamicTest> computeConditionEvaluationResultFailsForUnsupportedAnnotationType() {
		return Stream.of(Override.class, Deprecated.class, Object.class) //
				.map(type -> dynamicTest("computationFailsFor(" + type + ")", //
					() -> computeConditionEvaluationResultFailsForUnsupportedAnnotationType(type)));
	}

	private void computeConditionEvaluationResultFailsForUnsupportedAnnotationType(Type type) {
		Script script = new Script(type, "annotation", "engine", "source", "reason");
		Exception e = assertThrows(ScriptEvaluationException.class,
			() -> condition.computeConditionEvaluationResult(script, "!"));
		String expected = "Unsupported annotation type: " + type;
		String actual = e.getMessage();
		assertEquals(expected, actual);
	}

	@Test
	void defaultConditionEvaluationResultProperties() {
		Script script = script(EnabledIf.class, "true");
		ConditionEvaluationResult result = condition.evaluate(manager, script, bindings);
		assertFalse(result.isDisabled());
		assertThat(result.toString()).contains("ConditionEvaluationResult", "enabled", "true", "reason");
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScript() {
		Script script = script(EnabledIf.class, "junitConfigurationParameter.get('XXX')");
		Exception exception = assertThrows(ScriptEvaluationException.class,
			() -> condition.evaluate(manager, script, bindings));
		assertThat(exception.getMessage()).contains("Script returned `null`");
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScriptAndCheckForNull() {
		Script script = script(EnabledIf.class, "junitConfigurationParameter.get('XXX') != null");
		ConditionEvaluationResult result = condition.evaluate(manager, script, bindings);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX') != null` evaluated to: false", actual);
	}

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

	private Script script(Type type, String... lines) {
		return new Script( //
			type, //
			"Mock for " + type, //
			Script.DEFAULT_SCRIPT_ENGINE_NAME, //
			String.join("\n", lines), //
			Script.DEFAULT_SCRIPT_REASON_PATTERN //
		);
	}

	private Bindings createDefaultContextBindings() {
		Bindings bindings = new SimpleBindings();
		bindings.put(Script.BIND_JUNIT_TAGS, Collections.emptySet());
		bindings.put(Script.BIND_JUNIT_UNIQUE_ID, "Mock for UniqueId");
		bindings.put(Script.BIND_JUNIT_DISPLAY_NAME, "Mock for DisplayName");
		bindings.put(Script.BIND_JUNIT_CONFIGURATION_PARAMETER, Collections.emptyMap());
		return bindings;
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
