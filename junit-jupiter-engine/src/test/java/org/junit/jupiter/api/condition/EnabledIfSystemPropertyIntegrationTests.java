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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledIfSystemProperty}.
 *
 * @since 5.1
 */
class EnabledIfSystemPropertyIntegrationTests {

	private static final String KEY = "EnabledIfSystemPropertyTests.key";
	private static final String ENIGMA = "EnabledIfSystemPropertyTests.enigma";
	private static final String BOGUS = "EnabledIfSystemPropertyTests.bogus";

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
	@EnabledIfSystemProperty(named = "  ", matches = ENIGMA)
	void blankNamedAttribute() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledIfSystemProperty(named = KEY, matches = "  ")
	void blankMatchesAttribute() {
	}

	@Test
	@EnabledIfSystemProperty(named = KEY, matches = ENIGMA)
	void enabledBecauseSystemPropertyMatchesExactly() {
		assertEquals(ENIGMA, System.getProperty(KEY));
	}

	@Test
	@EnabledIfSystemProperty(named = KEY, matches = ".+enigma$")
	void enabledBecauseSystemPropertyMatchesPattern() {
		assertEquals(ENIGMA, System.getProperty(KEY));
	}

	@Test
	@EnabledIfSystemProperty(named = KEY, matches = BOGUS)
	void disabledBecauseSystemPropertyDoesNotMatch() {
		fail("should be disabled");
	}

	@Test
	@EnabledIfSystemProperty(named = BOGUS, matches = "doesn't matter")
	void disabledBecauseSystemPropertyDoesNotExist() {
		fail("should be disabled");
	}

}
