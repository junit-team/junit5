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
		String actual = new EnabledIfCondition().createScript(enabled, "unknown language");
		assertEquals("one" + System.lineSeparator() + "two", actual);
	}

	@Test
	void createJavaScriptMultipleLines() {
		EnabledIf enabled = mockEnabled("m1()", "m2()");
		assertLinesMatchCreatedScript(Arrays.asList("m1()", "m2()"), enabled, "ECMAScript");
	}

	private void assertLinesMatchCreatedScript(List<String> expectedLines, EnabledIf enabled, String language) {
		EnabledIfCondition condition = new EnabledIfCondition();
		String actual = condition.createScript(enabled, language);
		assertLinesMatch(expectedLines, Arrays.asList(actual.split("\\R")));
	}

	private EnabledIf mockEnabled(String... value) {
		EnabledIf enabled = Mockito.mock(EnabledIf.class);
		Mockito.when(enabled.value()).thenReturn(value);
		return enabled;
	}
}
