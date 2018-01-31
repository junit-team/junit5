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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.jupiter.engine.Constants.Script.Bind.JUNIT_CONFIGURATION_PARAMETER;
import static org.junit.jupiter.engine.Constants.Script.Bind.JUNIT_DISPLAY_NAME;
import static org.junit.jupiter.engine.Constants.Script.Bind.JUNIT_TAGS;
import static org.junit.jupiter.engine.Constants.Script.Bind.JUNIT_UNIQUE_ID;
import static org.junit.jupiter.engine.Constants.Script.Bind.SYSTEM_ENVIRONMENT;
import static org.junit.jupiter.engine.Constants.Script.Bind.SYSTEM_PROPERTY;
import static org.junit.jupiter.engine.Constants.Script.DEFAULT_ENGINE_NAME;
import static org.junit.jupiter.engine.Constants.Script.Reason.DEFAULT_PATTERN;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.junit.jupiter.api.DisabledIf;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.engine.Constants;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link ScriptExecutionManager}.
 *
 * @since 5.1
 */
class ScriptExecutionManagerTests {

	private final Bindings bindings = createDefaultContextBindings();
	private final ScriptExecutionManager manager = new ScriptExecutionManager();

	@Test
	void findJavaScriptEngine() {
		assertAll("Names", //
			() -> findJavaScriptEngine("Nashorn"), //
			() -> findJavaScriptEngine("nashorn"), //
			() -> findJavaScriptEngine("javascript"), //
			() -> findJavaScriptEngine("ecmascript") //
		);

		assertAll("File extension", //
			() -> findJavaScriptEngine("js") //
		);

		assertAll("MIME types", //
			() -> findJavaScriptEngine("application/javascript"), //
			() -> findJavaScriptEngine("application/ecmascript"), //
			() -> findJavaScriptEngine("text/javascript"), //
			() -> findJavaScriptEngine("text/ecmascript") //
		);

		assertThrows(PreconditionViolationException.class, () -> findJavaScriptEngine("?!"));
	}

	private void findJavaScriptEngine(String string) {
		ScriptEngine engine = manager.createScriptEngine(string);
		assertNotNull(engine);
		assertEquals("ECMAScript", engine.getFactory().getLanguageName());
	}

	@Test
	void systemAccessorsAreBoundByDefault() {
		ScriptEngine engine = manager.createScriptEngine(Constants.Script.DEFAULT_ENGINE_NAME);
		assertTrue(ScriptAccessor.class.isAssignableFrom(engine.get(SYSTEM_ENVIRONMENT).getClass()));
		assertTrue(ScriptAccessor.class.isAssignableFrom(engine.get(SYSTEM_PROPERTY).getClass()));

		assertFalse(((ScriptAccessor) engine.get(SYSTEM_PROPERTY)).get("java.version").isEmpty());
		assertFalse(((ScriptAccessor) engine.get(SYSTEM_ENVIRONMENT)).get("PATH").isEmpty());
	}

	@Test
	void forceScriptEvaluation() throws ScriptException {
		manager.forceScriptEvaluation = true;
		assertTrue(manager.evaluate(script(EnabledIf.class, "true"), bindings).isEnabled());
		assertTrue(manager.evaluate(script(DisabledIf.class, "true"), bindings).isDisabled());
	}

	@TestFactory
	Stream<DynamicTest> evaluateScriptsEvaluatingToTrue() {
		return Stream.of("true", "java.lang.Boolean.TRUE", "'TrUe'", "0 == 0", "/pi/.test('jupiter')") //
				.map(line -> dynamicTest("`" + line + "` -> true", //
					() -> assertScriptEvaluatingToTrue(line)));
	}

	private void assertScriptEvaluatingToTrue(String... lines) throws ScriptException {
		assertTrue(manager.evaluate(script(EnabledIf.class, lines), bindings).isEnabled());
		assertTrue(manager.evaluate(script(DisabledIf.class, lines), bindings).isDisabled());
	}

	@TestFactory
	Stream<DynamicTest> evaluateScriptsEvaluatingToFalse() {
		return Stream.of("false", "java.lang.Boolean.FALSE", "'FaLse'", "2 == 3", "/sun/.test('jupiter')") //
				.map(line -> dynamicTest("`" + line + "` -> false", //
					() -> assertScriptEvaluatingToFalse(line)));
	}

	private void assertScriptEvaluatingToFalse(String... lines) throws ScriptException {
		assertTrue(manager.evaluate(script(EnabledIf.class, lines), bindings).isDisabled());
		assertTrue(manager.evaluate(script(DisabledIf.class, lines), bindings).isEnabled());
	}

	@Test
	void computeConditionEvaluationResultWithDefaultReasonMessage() {
		Script script = script(EnabledIf.class, "?");
		String actual = manager.computeConditionEvaluationResult(script, "!").getReason().orElseThrow(Error::new);
		assertEquals("Script `?` evaluated to: !", actual);
	}

	@TestFactory
	Stream<DynamicTest> computeConditionEvaluationResultFailsForUnsupportedAnnotationType() {
		return Stream.of(Override.class, Deprecated.class, Object.class) //
				.map(type -> dynamicTest("computationFailsFor(" + type + ")", //
					() -> computeConditionEvaluationResultFailsForUnsupportedAnnotationType(type)));
	}

	private void computeConditionEvaluationResultFailsForUnsupportedAnnotationType(Type type) {
		Script script = script(type, "?");
		Exception e = assertThrows(JUnitException.class, () -> manager.computeConditionEvaluationResult(script, "!"));
		String expected = "Unsupported annotation type: " + type;
		String actual = e.getMessage();
		assertEquals(expected, actual);
	}

	@Test
	void syntaxErrorInScriptFailsTest() {
		Script enabledIfScript = script(EnabledIf.class, "syntax error");
		Exception exception = assertThrows(ScriptException.class, () -> manager.evaluate(enabledIfScript, bindings));
		assertTrue(exception.getMessage().contains("syntax error"));
		Script disabledIfScript = script(DisabledIf.class, "syntax error");
		exception = assertThrows(ScriptException.class, () -> manager.evaluate(disabledIfScript, bindings));
		assertTrue(exception.getMessage().contains("syntax error"));
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScript() throws ScriptException {
		Script script = script(EnabledIf.class, "junitConfigurationParameter.get('XXX')");
		ConditionEvaluationResult result = manager.evaluate(script, bindings);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX')` evaluated to: null", actual);
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScriptAndCheckForNull() throws ScriptException {
		Script script = script(EnabledIf.class, "junitConfigurationParameter.get('XXX') != null");
		ConditionEvaluationResult result = manager.evaluate(script, bindings);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX') != null` evaluated to: false", actual);
	}

	private Script script(Type type, String... lines) {
		return new Script(type, "Mock for " + type, DEFAULT_ENGINE_NAME, String.join("\n", lines), DEFAULT_PATTERN);
	}

	private Bindings createDefaultContextBindings() {
		Bindings bindings = new SimpleBindings();
		bindings.put(JUNIT_TAGS, Collections.emptySet());
		bindings.put(JUNIT_UNIQUE_ID, "Mock for UniqueId");
		bindings.put(JUNIT_DISPLAY_NAME, "Mock for DisplayName");
		bindings.put(JUNIT_CONFIGURATION_PARAMETER, Collections.emptyMap());
		return bindings;
	}

}
