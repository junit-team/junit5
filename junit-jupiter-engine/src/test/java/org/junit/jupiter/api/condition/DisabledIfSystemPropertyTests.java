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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DisabledIfSystemProperty}.
 *
 * @since 5.1
 */
class DisabledIfSystemPropertyTests {

	private static final String KEY = "DisabledIfSystemPropertyTests.key";
	private static final String ENIGMA = "DisabledIfSystemPropertyTests.enigma";
	private static final String BOGUS = "DisabledIfSystemPropertyTests.bogus";

	@BeforeAll
	static void setUp() {
		System.setProperty(KEY, ENIGMA);
	}

	@AfterAll
	static void tearDown() {
		System.clearProperty(KEY);
	}

	@Test
	@DisabledIfSystemProperty(named = KEY, matches = ENIGMA)
	void propertyMatchesExactly() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfSystemProperty(named = KEY, matches = ".+enigma$")
	void propertyMatchesPattern() {
		fail("should be disabled");
	}

	@Test
	@DisabledIfSystemProperty(named = KEY, matches = BOGUS)
	void propertyDoesNotMatch() {
		assertNotEquals(BOGUS, System.getProperty(KEY));
	}

	@Test
	@DisabledIfSystemProperty(named = BOGUS, matches = "doesn't matter")
	void propertyDoesNotExist() {
		assertNull(System.getProperty(BOGUS));
	}

}
