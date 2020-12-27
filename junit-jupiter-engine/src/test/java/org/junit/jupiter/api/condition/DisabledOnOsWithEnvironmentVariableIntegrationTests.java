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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onLinux;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onMac;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onSolaris;
import static org.junit.jupiter.api.condition.EnabledOnOsIntegrationTests.onWindows;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.OTHER;
import static org.junit.jupiter.api.condition.OS.SOLARIS;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Only used in a unit test via reflection")
final class DisabledOnOsWithEnvironmentVariableIntegrationTests {

	static final String KEY1 = "DisabledIfEnvironmentVariableTests.key1";
	static final String KEY2 = "DisabledIfEnvironmentVariableTests.key2";
	static final String ENIGMA = "enigma";
	static final String BOGUS = "bogus";

	@Test
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledOnOsWithEnvironmentVariable(value = {}, named = "", matches = "")
	void missingOsDeclaration() {
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = { LINUX, MAC, WINDOWS, SOLARIS,
			OTHER }, named = KEY1, matches = ENIGMA, disabledReason = "Disabled on every OS")
	void disabledOnEveryOs() {
		fail("should be disabled");
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY1, matches = BOGUS)
	void enabledOnLinuxWithoutEnvironmentVariablesMatches() {
		assertFalse(onLinux());
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY1, matches = ENIGMA)
	void disableOnOsWithEnvironmentVariableMatches() {
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = MAC, named = KEY1, matches = ENIGMA)
	void macOs() {
		assertFalse(onMac());
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = WINDOWS, named = KEY1, matches = ENIGMA)
	void windows() {
		assertFalse(onWindows());
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = SOLARIS, named = KEY1, matches = ENIGMA)
	void solaris() {
		assertFalse(onSolaris());
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = OTHER, named = KEY1, matches = ENIGMA)
	void other() {
		assertTrue(onLinux() || onMac() || onSolaris() || onWindows());
	}

	// -------------------------------------------------------------------------

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = "  ", matches = ENIGMA)
	void blankNamedAttribute() {
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = SOLARIS, named = KEY1, matches = "  ")
	void blankMatchesAttribute() {
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY1, matches = ENIGMA, disabledReason = "That's an enigma")
	void disabledBecauseEnvironmentVariableMatchesExactly() {
		fail("should be disabled");
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY1, matches = BOGUS)
	@CustomDisabled
	void disabledBecauseEnvironmentVariableForComposedAnnotationMatchesExactly() {
		fail("should be disabled");
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY1, matches = ".*e.+gma$")
	void disabledBecauseEnvironmentVariableMatchesPattern() {
		fail("should be disabled");
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY1, matches = BOGUS)
	void enabledBecauseEnvironmentVariableDoesNotMatch() {
		assertNotEquals(BOGUS, System.getenv(KEY1));
	}

	@Test
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = BOGUS, matches = "doesn't matter")
	void enabledBecauseEnvironmentVariableDoesNotExist() {
		assertNull(System.getenv(BOGUS));
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@DisabledOnOsWithEnvironmentVariable(value = LINUX, named = KEY2, matches = ENIGMA)
	@interface CustomDisabled {
	}
}
