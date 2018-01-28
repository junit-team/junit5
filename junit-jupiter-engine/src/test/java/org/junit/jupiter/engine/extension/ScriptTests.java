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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Script}.
 *
 * @since 5.1
 */
class ScriptTests {

	@Test
	void arbitraryScriptProperties() {
		Script script = new Script(Deprecated.class, "@Deprecated", "engine", "source", "reason");
		assertEquals(Deprecated.class, script.getAnnotationType());
		assertEquals("@Deprecated", script.getAnnotationAsString());
		assertEquals("engine", script.getEngine());
		assertEquals("source", script.getSource());
		assertEquals("reason", script.getReason());
	}

}
