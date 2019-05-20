/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.commons.JUnitException;

/**
 * Unit tests for {@link Script}.
 *
 * @since 5.1
 */
@Deprecated
class ScriptTests {

	@Test
	void constructorWithAllArguments() {
		Script script = new Script(Deprecated.class, "annotation", "engine", "source", "reason");
		assertEquals(Deprecated.class, script.getAnnotationType());
		assertEquals("annotation", script.getAnnotationAsString());
		assertEquals("engine", script.getEngine());
		assertEquals("source", script.getSource());
		assertEquals("reason", script.getReason());
		assertEquals("reason", script.toReasonString("unused result"));
	}

	@Test
	void constructorWithAnnotation(TestInfo info) {
		Annotation annotation = info.getTestMethod().orElseThrow(Error::new).getAnnotation(Test.class);
		Script script = new Script(annotation, "engine", "source", "reason");
		assertEquals(Test.class, script.getAnnotationType());
		assertEquals("@org.junit.jupiter.api.Test()", script.getAnnotationAsString());
		assertEquals("engine", script.getEngine());
		assertEquals("source", script.getSource());
		assertEquals("reason", script.getReason());
		assertEquals("reason", script.toReasonString("unused result"));
	}

	@TestFactory
	Stream<DynamicTest> preconditionsAreChecked(TestInfo info) {
		Annotation annotation = info.getTestMethod().orElseThrow(Error::new).getAnnotation(TestFactory.class);
		Class<JUnitException> expected = JUnitException.class;
		return Stream.of( //
			dynamicTest("0", () -> assertNotNull(new Script(annotation, "e", "s", "r"))), //
			dynamicTest("1", () -> assertThrows(expected, () -> new Script(null, "e", "s", "r"))), //
			// null is not allowed
			dynamicTest("2", () -> assertNotNull(new Script(Test.class, "a", "e", "s", "r"))), //
			dynamicTest("3", () -> assertThrows(expected, () -> new Script(null, "a", "e", "s", "r"))), //
			dynamicTest("4", () -> assertThrows(expected, () -> new Script(Test.class, null, "e", "s", "r"))), //
			dynamicTest("5", () -> assertThrows(expected, () -> new Script(Test.class, "a", null, "s", "r"))), //
			dynamicTest("6", () -> assertThrows(expected, () -> new Script(Test.class, "a", "e", null, "r"))), //
			dynamicTest("7", () -> assertThrows(expected, () -> new Script(Test.class, "a", "e", "s", null))), //
			// engine and source must not be blank
			dynamicTest("8", () -> assertNotNull(new Script(Test.class, "", "e", "s", ""))), //
			dynamicTest("9", () -> assertThrows(expected, () -> new Script(Test.class, "", "", "s", ""))), //
			dynamicTest("A", () -> assertThrows(expected, () -> new Script(Test.class, "", "e", "", ""))) //
		);
	}

	@Test
	void equalsAndHashCode() {
		Script s = new Script(Deprecated.class, "annotation", "engine", "source", "reason");
		// hit short-cut branches
		assertNotEquals(s, null);
		assertNotEquals(s, new Object());
		// annotationAsString and reason pattern are ignored by Script.equals and .hashCode
		Script t = new Script(Deprecated.class, "a.........", "engine", "source", "r.....");
		assertEquals(s, t);
		assertEquals(s.hashCode(), t.hashCode());
		// now assert differences
		Script u = new Script(Deprecated.class, "annotation", "u.....", "source", "reason");
		assertNotEquals(s, u);
		assertNotEquals(t, u);
		Script v = new Script(Deprecated.class, "annotation", "engine", "v.....", "reason");
		assertNotEquals(s, v);
		assertNotEquals(t, v);
		assertNotEquals(u, v);
		Script w = new Script(Override.class, "annotation", "engine", "source", "reason");
		assertNotEquals(s, w);
		assertNotEquals(t, w);
		assertNotEquals(u, w);
		assertNotEquals(v, w);
	}

	@Test
	void customReasonPattern() {
		String reasonPattern = "result={result} source={source} annotation={annotation}";
		Script script = new Script(Deprecated.class, "@Deprecated", "engine", "source", reasonPattern);
		assertEquals("result=✅ source=source annotation=@Deprecated", script.toReasonString("✅"));
	}

}
