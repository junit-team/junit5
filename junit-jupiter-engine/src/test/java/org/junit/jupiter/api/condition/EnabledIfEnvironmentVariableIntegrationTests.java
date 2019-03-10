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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledIfEnvironmentVariable}.
 *
 * @since 5.1
 */
@Disabled("Disabled since the required environment variable is not set")
// Tests will pass if you set the following environment variable:
// EnabledIfEnvironmentVariableTests.key = EnabledIfEnvironmentVariableTests.enigma
class EnabledIfEnvironmentVariableIntegrationTests {

	static final String KEY = "EnabledIfEnvironmentVariableTests.key";
	static final String ENIGMA = "EnabledIfEnvironmentVariableTests.enigma";
	static final String BOGUS = "EnabledIfEnvironmentVariableTests.bogus";

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledIfEnvironmentVariable(named = "  ", matches = ENIGMA)
	void blankNamedAttribute() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledIfEnvironmentVariable(named = KEY, matches = "  ")
	void blankMatchesAttribute() {
	}

	@Test
	@EnabledIfEnvironmentVariable(named = KEY, matches = ENIGMA)
	void enabledBecauseEnvironmentVariableMatchesExactly() {
		assertEquals(ENIGMA, System.getenv(KEY));
	}

	@Test
	@EnabledIfEnvironmentVariable(named = KEY, matches = ".+enigma$")
	void enabledBecauseEnvironmentVariableMatchesPattern() {
		assertEquals(ENIGMA, System.getenv(KEY));
	}

	@Test
	@EnabledIfEnvironmentVariable(named = KEY, matches = BOGUS)
	void disabledBecauseEnvironmentVariableDoesNotMatch() {
		fail("should be disabled");
	}

	@Test
	@EnabledIfEnvironmentVariable(named = BOGUS, matches = "doesn't matter")
	void disabledBecauseEnvironmentVariableDoesNotExist() {
		fail("should be disabled");
	}

}
