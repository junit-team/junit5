/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.JRE.JAVA_10;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_12;
import static org.junit.jupiter.api.condition.JRE.JAVA_13;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.JRE.OTHER;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledOnJre}.
 *
 * @since 5.1
 */
class EnabledOnJreIntegrationTests {

	private static final String JAVA_VERSION = System.getProperty("java.version");

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledOnJre({})
	void missingJreDeclaration() {
	}

	@Test
	@EnabledOnJre({ JAVA_8, JAVA_9, JAVA_10, JAVA_11, JAVA_12, JAVA_13, OTHER })
	void enabledOnAllJavaVersions() {
	}

	@Test
	@EnabledOnJre(JAVA_8)
	void java8() {
		assertTrue(onJava8());
	}

	@Test
	@EnabledOnJre(JAVA_9)
	void java9() {
		assertTrue(onJava9());
	}

	@Test
	@EnabledOnJre(JAVA_10)
	void java10() {
		assertTrue(onJava10());
	}

	@Test
	@EnabledOnJre(JAVA_11)
	void java11() {
		assertTrue(onJava11());
	}

	@Test
	@EnabledOnJre(JAVA_12)
	void java12() {
		assertTrue(onJava12());
	}

	@Test
	@EnabledOnJre(JAVA_13)
	void java13() {
		assertTrue(onJava13());
	}

	@Test
	@EnabledOnJre(OTHER)
	void other() {
		assertFalse(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13());
	}

	static boolean onJava8() {
		return JAVA_VERSION.startsWith("1.8");
	}

	static boolean onJava9() {
		return JAVA_VERSION.startsWith("9");
	}

	static boolean onJava10() {
		return JAVA_VERSION.startsWith("10");
	}

	static boolean onJava11() {
		return JAVA_VERSION.startsWith("11");
	}

	static boolean onJava12() {
		return JAVA_VERSION.startsWith("12");
	}

	static boolean onJava13() {
		return JAVA_VERSION.startsWith("13");
	}

}
