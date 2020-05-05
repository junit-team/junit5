/*
 * Copyright 2015-2020 the original author or authors.
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
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.OTHER;
import static org.junit.jupiter.api.condition.OS.SOLARIS;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledOnOs}.
 *
 * @since 5.1
 */
class EnabledOnOsIntegrationTests {

	private static final String OS_NAME = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledOnOs({})
	void missingOsDeclaration() {
	}

	@Test
	@EnabledOnOs({ LINUX, MAC, WINDOWS, SOLARIS, OTHER })
	void enabledOnEveryOs() {
	}

	@Test
	@EnabledOnOs(LINUX)
	void linux() {
		assertTrue(onLinux());
	}

	@Test
	@EnabledOnOs(MAC)
	void macOs() {
		assertTrue(onMac());
	}

	@Test
	@EnabledOnMac
	void macOsWithComposedAnnotation() {
		assertTrue(onMac());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void windows() {
		assertTrue(onWindows());
	}

	@Test
	@EnabledOnOs(SOLARIS)
	void solaris() {
		assertTrue(onSolaris());
	}

	@Test
	@EnabledOnOs(value = OTHER, disabledReason = "Disabled on almost every OS")
	void other() {
		assertFalse(onLinux() || onMac() || onSolaris() || onWindows());
	}

	static boolean onLinux() {
		return OS_NAME.contains("linux");
	}

	static boolean onMac() {
		return OS_NAME.contains("mac");
	}

	static boolean onSolaris() {
		return OS_NAME.contains("solaris");
	}

	static boolean onWindows() {
		return OS_NAME.contains("windows");
	}

	// -------------------------------------------------------------------------

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@EnabledOnOs(MAC)
	@interface EnabledOnMac {
	}

}
