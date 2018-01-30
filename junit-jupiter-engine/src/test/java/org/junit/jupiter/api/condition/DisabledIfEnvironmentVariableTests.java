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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DisabledIfEnvironmentVariable}.
 *
 * @since 5.1
 */
@Disabled("Disabled since the required environment variable is not set")
// Tests will pass if you set the following environment variable:
// DisabledIfEnvironmentVariableTests.key = DisabledIfEnvironmentVariableTests.enigma
class DisabledIfEnvironmentVariableTests {

	private static final String KEY = "DisabledIfEnvironmentVariableTests.key";
	private static final String ENIGMA = "DisabledIfEnvironmentVariableTests.enigma";
	private static final String BOGUS = "DisabledIfEnvironmentVariableTests.bogus";

	@Test
	@DisabledIfEnvironmentVariable(named = KEY, matches = ENIGMA)
	void environmentVariableMatchesExactly() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfEnvironmentVariable(named = KEY, matches = ".+enigma$")
	void environmentVariableMatchesPattern() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfEnvironmentVariable(named = KEY, matches = BOGUS)
	void environmentVariableDoesNotMatch() {
		assertNotEquals(BOGUS, System.getenv(KEY));
	}

	@Test
	@DisabledIfEnvironmentVariable(named = BOGUS, matches = "doesn't matter")
	void environmentVariableDoesNotExist() {
		assertNull(System.getenv(BOGUS));
	}

}
