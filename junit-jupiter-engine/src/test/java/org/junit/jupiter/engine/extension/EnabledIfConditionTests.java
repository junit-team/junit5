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
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;

import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.mockito.Mockito;

class EnabledIfConditionTests {

	@Test
	void findJavaScriptEngine() {
		assertAll("Names", //
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
		EnabledIfCondition condition = new EnabledIfCondition();
		ScriptEngine engine = condition.findScriptEngine(string);
		assertNotNull(engine);
	}

	@Test
	void trivialJavaScript() {
		String script = "true";
		EnabledIf enabled = mockEnabled(script);
		String actual = new EnabledIfCondition().createScript(enabled, "ECMAScript");
		assertSame(script, actual);
	}

	@Test
	void trivialGroovyScript() {
		String script = "true";
		EnabledIf enabled = mockEnabled(script);
		String actual = new EnabledIfCondition().createScript(enabled, "Groovy");
		assertSame(script, actual);
	}

	@Test
	void trivialNonJavaScript() {
		EnabledIf enabled = mockEnabled("one", "two");
		Mockito.when(enabled.delimiter()).thenReturn("/");
		String actual = new EnabledIfCondition().createScript(enabled, "unknown language");
		assertEquals("one/two", actual);
	}

	@Test
	void createJavaScriptMultipleLines() {
		EnabledIf enabled = mockEnabled("m1()", "m2()");
		assertLinesMatchCreatedScript(Arrays.asList("m1()", "m2()"), enabled, "ECMAScript");
	}

	@Test
	void createJavaScriptMultipleLinesWithImports() {
		EnabledIf enabled = mockEnabled("m1()", "m2()");
		Mockito.when(enabled.imports()).thenReturn(new String[] { "a", "b" });
		List<String> expected = new ArrayList<>();
		expected.add("var javaImporter = new JavaImporter(a, b)");
		expected.add("with (javaImporter) {");
		expected.add("  m1()");
		expected.add("  m2()");
		expected.add("}");
		assertLinesMatchCreatedScript(expected, enabled, "ECMAScript");
	}

	@Test
	void createGroovyScriptMultipleLinesWithImports() {
		EnabledIf enabled = mockEnabled("m1()", "m2()");
		Mockito.when(enabled.imports()).thenReturn(new String[] { "a", "b" });
		List<String> expected = new ArrayList<>();
		expected.add("import a");
		expected.add("import b");
		expected.add("");
		expected.add("m1()");
		expected.add("m2()");
		assertLinesMatchCreatedScript(expected, enabled, "Groovy");
	}

	private void assertLinesMatchCreatedScript(List<String> expectedLines, EnabledIf enabled, String language) {
		EnabledIfCondition condition = new EnabledIfCondition();
		String actual = condition.createScript(enabled, language);
		assertLinesMatch(expectedLines, Arrays.asList(actual.split("\\R")));
	}

	private EnabledIf mockEnabled(String... value) {
		EnabledIf enabled = Mockito.mock(EnabledIf.class);
		Mockito.when(enabled.value()).thenReturn(value);
		try {
			Mockito.when(enabled.bindExtensionContext()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("bindExtensionContext").getDefaultValue());
			Mockito.when(enabled.bindSystemProperties()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("bindSystemProperties").getDefaultValue());
			Mockito.when(enabled.delimiter()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("delimiter").getDefaultValue());
			Mockito.when(enabled.engine()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("engine").getDefaultValue());
			Mockito.when(enabled.reason()).thenReturn(
				(String) EnabledIf.class.getDeclaredMethod("reason").getDefaultValue());
			Mockito.when(enabled.imports()).thenReturn(
				(String[]) EnabledIf.class.getDeclaredMethod("imports").getDefaultValue());
		}
		catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
		return enabled;
	}
}
