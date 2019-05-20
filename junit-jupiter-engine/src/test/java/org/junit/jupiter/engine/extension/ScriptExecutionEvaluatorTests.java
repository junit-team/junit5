/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ScriptEvaluationException;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.jupiter.engine.script.Script;
import org.junit.jupiter.engine.script.ScriptExecutionManager;

/**
 * Unit tests for {@link ScriptExecutionEvaluator}.
 *
 * @since 5.1
 */
@Deprecated
class ScriptExecutionEvaluatorTests extends AbstractJupiterTestEngineTests {

	private final Bindings bindings = createDefaultContextBindings();
	private final ScriptExecutionManager manager = new ScriptExecutionManager();
	private final ScriptExecutionEvaluator evaluator = new ScriptExecutionEvaluator();

	@Test
	void nullAsScriptReturnsNullAsResult() {
		assertSame(null, evaluate(null));
	}

	@Test
	void computeConditionEvaluationResultWithDefaultReasonMessage() {
		Script script = script(EnabledIf.class, "?");
		ConditionEvaluationResult result = evaluator.computeConditionEvaluationResult(script, "!");
		assertTrue(result.isDisabled());
		assertThat(result.getReason()).contains("Script `?` evaluated to: !");
	}

	@TestFactory
	Stream<DynamicTest> computeConditionEvaluationResultFailsForUnsupportedAnnotationType() {
		return Stream.of(Override.class, Deprecated.class) //
				.map(type -> dynamicTest("computationFailsFor(" + type + ")", //
					() -> computeConditionEvaluationResultFailsForUnsupportedAnnotationType(type)));
	}

	private void computeConditionEvaluationResultFailsForUnsupportedAnnotationType(Class<? extends Annotation> type) {
		Script script = new Script(type, "annotation", "engine", "source", "reason");
		Exception e = assertThrows(ScriptEvaluationException.class,
			() -> evaluator.computeConditionEvaluationResult(script, "!"));
		String expected = "Unsupported annotation type: " + type;
		String actual = e.getMessage();
		assertEquals(expected, actual);
	}

	@Test
	void defaultConditionEvaluationResultProperties() {
		Script script = script(EnabledIf.class, "true");
		ConditionEvaluationResult result = evaluate(script);
		assertFalse(result.isDisabled());
		assertThat(result.toString()).contains("ConditionEvaluationResult", "enabled", "true", "reason");
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScript() {
		Script script = script(DisabledIf.class, "junitConfigurationParameter.get('XXX')");
		Exception exception = assertThrows(ScriptEvaluationException.class, () -> evaluate(script));
		assertThat(exception.getMessage()).contains("Script returned `null`");
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScriptAndCheckForNull() {
		Script script = script(DisabledIf.class, "junitConfigurationParameter.get('XXX') == null");
		ConditionEvaluationResult result = evaluate(script);
		assertTrue(result.isDisabled());
		assertThat(result.getReason()) //
				.contains("Script `junitConfigurationParameter.get('XXX') == null` evaluated to: true");
	}

	private ConditionEvaluationResult evaluate(Script script) {
		return evaluator.evaluate(manager, script, bindings);
	}

	private Script script(Class<? extends Annotation> type, String... lines) {
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

}
