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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link DisabledIfSystemProperty}.
 *
 * @since 5.1
 */
class DisabledIfSystemPropertyIntegrationTests {

	private static final String KEY = "DisabledIfSystemPropertyTests.key";
	private static final String ENIGMA = "DisabledIfSystemPropertyTests.enigma";
	private static final String BOGUS = "DisabledIfSystemPropertyTests.bogus";

	@BeforeAll
	static void setSystemProperty() {
		System.setProperty(KEY, ENIGMA);
	}

	@AfterAll
	static void clearSystemProperty() {
		System.clearProperty(KEY);
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledIfSystemProperty(named = "  ", matches = ENIGMA)
	void blankNamedAttribute() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@DisabledIfSystemProperty(named = KEY, matches = "  ")
	void blankMatchesAttribute() {
	}

	@Test
	@DisabledIfSystemProperty(named = KEY, matches = ENIGMA)
	void disabledBecauseSystemPropertyMatchesExactly() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfSystemProperty(named = KEY, matches = ".+enigma$")
	void disabledBecauseSystemPropertyMatchesPattern() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfSystemProperty(named = KEY, matches = BOGUS)
	void enabledBecauseSystemPropertyDoesNotMatch() {
		assertNotEquals(BOGUS, System.getProperty(KEY));
	}

	@Test
	@DisabledIfSystemProperty(named = BOGUS, matches = "doesn't matter")
	void enabledBecauseSystemPropertyDoesNotExist() {
		assertNull(System.getProperty(BOGUS));
	}

}
