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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * Unit tests for {@link Script}.
 *
 * @since 5.1
 */
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
		Test annotation = info.getTestMethod().orElseThrow(Error::new).getAnnotation(Test.class);
		Script script = new Script(annotation, "engine", "source", "reason");
		assertEquals(Test.class, script.getAnnotationType());
		assertEquals("@org.junit.jupiter.api.Test()", script.getAnnotationAsString());
		assertEquals("engine", script.getEngine());
		assertEquals("source", script.getSource());
		assertEquals("reason", script.getReason());
		assertEquals("reason", script.toReasonString("unused result"));
	}

	@Test
	void equalsAndHashCode() {
		Script s = new Script(Deprecated.class, "annotation", "engine", "source", "reason");
		Script t = new Script(Deprecated.class, "a.........", "engine", "source", "r.....");
		assertEquals(s, t);
		assertEquals(s.hashCode(), t.hashCode());
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
