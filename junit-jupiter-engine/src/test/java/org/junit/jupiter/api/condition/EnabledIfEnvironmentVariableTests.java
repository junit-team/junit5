/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnabledIfEnvironmentVariable}.
 *
 * @since 5.1
 */
@Disabled("Disabled since the required environment variable is not set")
// Tests will pass if you set the following environment variable:
// EnabledIfEnvironmentVariableTests.key = EnabledIfEnvironmentVariableTests.enigma
class EnabledIfEnvironmentVariableTests {

	private static final String KEY = "EnabledIfEnvironmentVariableTests.key";
	private static final String ENIGMA = "EnabledIfEnvironmentVariableTests.enigma";
	private static final String BOGUS = "EnabledIfEnvironmentVariableTests.bogus";

	@Test
	@EnabledIfEnvironmentVariable(named = KEY, matches = ENIGMA)
	void environmentVariableMatchesExactly() {
		assertEquals(ENIGMA, System.getenv(KEY));
	}

	@Test
	@EnabledIfEnvironmentVariable(named = KEY, matches = ".+enigma$")
	void environmentVariableMatchesPattern() {
		assertEquals(ENIGMA, System.getenv(KEY));
	}

	@Test
	@EnabledIfEnvironmentVariable(named = KEY, matches = BOGUS)
	void environmentVariableDoesNotMatch() {
		fail("should be disabled");
	}

	@Test
	@EnabledIfEnvironmentVariable(named = BOGUS, matches = "doesn't matter")
	void environmentVariableDoesNotExist() {
		assertNull(System.getenv(BOGUS));
	}

}
