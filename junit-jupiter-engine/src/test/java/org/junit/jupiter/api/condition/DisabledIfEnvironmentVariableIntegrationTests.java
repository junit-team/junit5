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

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DisabledIfEnvironmentVariable}.
 *
 * @since 5.1
 */
@Disabled("Disabled since the required environment variable is not set")
// Tests will pass if you set the following environment variable:
// DisabledIfEnvironmentVariableTests.key = DisabledIfEnvironmentVariableTests.enigma
class DisabledIfEnvironmentVariableIntegrationTests {

	static final String KEY = "DisabledIfEnvironmentVariableTests.key";
	static final String ENIGMA = "DisabledIfEnvironmentVariableTests.enigma";
	static final String BOGUS = "DisabledIfEnvironmentVariableTests.bogus";

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledIfEnvironmentVariable(named = "  ", matches = ENIGMA)
	void blankNamedAttribute() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledIfEnvironmentVariable(named = KEY, matches = "  ")
	void blankMatchesAttribute() {
	}

	@Test
	@DisabledIfEnvironmentVariable(named = KEY, matches = ENIGMA)
	void disabledBecauseEnvironmentVariableMatchesExactly() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfEnvironmentVariable(named = KEY, matches = ".+enigma$")
	void disabledBecauseEnvironmentVariableMatchesPattern() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfEnvironmentVariable(named = KEY, matches = BOGUS)
	void enabledBecauseEnvironmentVariableDoesNotMatch() {
		assertNotEquals(BOGUS, System.getenv(KEY));
	}

	@Test
	@DisabledIfEnvironmentVariable(named = BOGUS, matches = "doesn't matter")
	void enabledBecauseEnvironmentVariableDoesNotExist() {
		assertNull(System.getenv(BOGUS));
	}

}
