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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import javax.script.Bindings;
import javax.script.ScriptEngine;

import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link EnabledIfCondition}.
 *
 * @since 5.1
 */
class EnabledIfConditionTests {

	private final EnabledIfCondition condition = new EnabledIfCondition();

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
		ScriptEngine engine = condition.findScriptEngine(string);
		assertNotNull(engine);
	}

	@Test
	void trivialJavaScript() {
		String script = "true";
		EnabledIf annotation = mockEnabledIfAnnotation(script);
		String actual = condition.createScript(annotation, "ECMAScript");
		assertSame(script, actual);
	}

	@Test
	void trivialGroovyScript() {
		String script = "true";
		EnabledIf annotation = mockEnabledIfAnnotation(script);
		String actual = condition.createScript(annotation, "Groovy");
		assertSame(script, actual);
	}

	@Test
	void trivialNonJavaScript() {
		EnabledIf annotation = mockEnabledIfAnnotation("one", "two");
		String script = condition.createScript(annotation, "unknown language");
		assertLinesMatch(Arrays.asList("one", "two"), Arrays.asList(script.split("\\R")));
	}

	@Test
	void createJavaScriptMultipleLines() {
		EnabledIf annotation = mockEnabledIfAnnotation("m1()", "m2()");
		String script = condition.createScript(annotation, "ECMAScript");
		assertLinesMatch(Arrays.asList("m1()", "m2()"), Arrays.asList(script.split("\\R")));
	}

	@Test
	void createReasonWithDefaultMessage() {
		EnabledIf annotation = mockEnabledIfAnnotation("?");
		String actual = condition.createReason(annotation, "?", "!");
		assertEquals("Script `?` evaluated to: !", actual);
	}

	@Test
	void createReasonWithCustomMessage() {
		EnabledIf annotation = mockEnabledIfAnnotation("?");
		when(annotation.reason()).thenReturn("result={result} script={script}");
		String actual = condition.createReason(annotation, "?", "!");
		assertEquals("result=! script=?", actual);
	}

	@Test
	void syntaxErrorInScriptFailsTest() {
		EnabledIf annotation = mockEnabledIfAnnotation("syntax error");
		Exception exception = assertThrows(JUnitException.class, () -> evaluate(annotation));
		assertThat(exception.getMessage()).contains("@EnabledIf", "script", "syntax error", "bindings");
	}

	@Test
	@EnabledIf("true")
	void defaultAnnotationValues() {
		EnabledIf annotation = reflectEnabledIfAnnotation("defaultAnnotationValues");
		assertEquals("true", String.join("-", annotation.value()));
		assertEquals("nashorn", annotation.engine());
		assertEquals("Script `{script}` evaluated to: {result}", annotation.reason());
	}

	@Test
	@EnabledIf(value = "true", reason = "{annotation}")
	void createReasonWithAnnotationPlaceholder() {
		EnabledIf annotation = reflectEnabledIfAnnotation("createReasonWithAnnotationPlaceholder");
		ConditionEvaluationResult result = evaluate(annotation);
		assertFalse(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertThat(actual)//
				.startsWith("@org.junit.jupiter.api.EnabledIf(")//
				.contains("reason=", "{annotation}", "engine=", "nashorn", "value=", "true")//
				.endsWith(")");
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScript() {
		EnabledIf annotation = mockEnabledIfAnnotation("junitConfigurationParameter.get('XXX')");
		ConditionEvaluationResult result = evaluate(annotation);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX')` evaluated to: null", actual);
	}

	@Test
	void getJUnitConfigurationParameterWithJavaScriptAndCheckForNull() {
		EnabledIf annotation = mockEnabledIfAnnotation("junitConfigurationParameter.get('XXX') != null");
		ConditionEvaluationResult result = evaluate(annotation);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX') != null` evaluated to: false", actual);
	}

	@Test
	void getJUnitConfigurationParameterWithGroovy() {
		EnabledIf annotation = mockEnabledIfAnnotation("junitConfigurationParameter.get('XXX')");
		when(annotation.engine()).thenReturn("Groovy");
		ConditionEvaluationResult result = evaluate(annotation);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX')` evaluated to: null", actual);
	}

	@Test
	void getJUnitConfigurationParameterWithGroovyAndCheckForNull() {
		EnabledIf annotation = mockEnabledIfAnnotation("junitConfigurationParameter.get('XXX') != null");
		when(annotation.engine()).thenReturn("Groovy");
		ConditionEvaluationResult result = evaluate(annotation);
		assertTrue(result.isDisabled());
		String actual = result.getReason().orElseThrow(() -> new AssertionError("causeless"));
		assertEquals("Script `junitConfigurationParameter.get('XXX') != null` evaluated to: false", actual);
	}

	private ConditionEvaluationResult evaluate(EnabledIf annotation) {
		return condition.evaluate(annotation, this::mockBinder);
	}

	private void mockBinder(Bindings bindings) {
		bindings.put(EnabledIf.Bind.JUNIT_TAGS, Collections.emptySet());
		bindings.put(EnabledIf.Bind.JUNIT_UNIQUE_ID, "Mock for UniqueId");
		bindings.put(EnabledIf.Bind.JUNIT_DISPLAY_NAME, "Mock for DisplayName");
		bindings.put(EnabledIf.Bind.JUNIT_CONFIGURATION_PARAMETER, Collections.emptyMap());
	}

	private EnabledIf mockEnabledIfAnnotation(String... value) {
		EnabledIf annotation = mock(EnabledIf.class);
		when(annotation.value()).thenReturn(value);
		try {
			when(annotation.engine()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("engine").getDefaultValue());
			when(annotation.reason()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("reason").getDefaultValue());
		}
		catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
		return annotation;
	}

	private EnabledIf reflectEnabledIfAnnotation(String methodName, Class<?>... parameterTypes) {
		try {
			return getClass().getDeclaredMethod(methodName, parameterTypes).getDeclaredAnnotation(EnabledIf.class);
		}
		catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

}
