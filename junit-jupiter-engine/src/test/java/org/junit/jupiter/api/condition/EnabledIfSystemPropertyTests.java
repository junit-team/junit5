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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnabledIfSystemProperty}.
 *
 * @since 5.1
 */
class EnabledIfSystemPropertyTests {

	private static final String KEY = "EnabledIfSystemPropertyTests.key";
	private static final String ENIGMA = "EnabledIfSystemPropertyTests.enigma";
	private static final String BOGUS = "EnabledIfSystemPropertyTests.bogus";

	@BeforeAll
	static void setUp() {
		System.setProperty(KEY, ENIGMA);
	}

	@AfterAll
	static void tearDown() {
		System.clearProperty(KEY);
	}

	@Test
	@EnabledIfSystemProperty(named = KEY, matches = ENIGMA)
	void propertyMatchesExactly() {
		assertEquals(ENIGMA, System.getProperty(KEY));
	}

	@Test
	@EnabledIfSystemProperty(named = KEY, matches = ".+enigma$")
	void propertyMatchesPattern() {
		assertEquals(ENIGMA, System.getProperty(KEY));
	}

	@Test
	@EnabledIfSystemProperty(named = KEY, matches = BOGUS)
	void propertyDoesNotMatch() {
		fail("should be disabled");
	}

	@Test
	@EnabledIfSystemProperty(named = BOGUS, matches = "doesn't matter")
	void propertyDoesNotExist() {
		assertNull(System.getProperty(BOGUS));
	}

}
