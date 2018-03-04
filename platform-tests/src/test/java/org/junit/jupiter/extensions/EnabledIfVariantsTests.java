/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.OS.MAC;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * Negative case tests for {@link org.junit.jupiter.api.condition.EnabledIf} variants.
 */
public class EnabledIfVariantsTests {

	private static final String KEY = "EnabledIfVariantsTests.key";

	private static final String ENIGMA = "EnabledIfVariantsTests.enigma";

	private static final String BOGUS = "EnabledIfVariantsTests.bogus";

	@BeforeAll
	static void setSystemProperty() {
		System.setProperty(KEY, ENIGMA);
	}

	@AfterAll
	static void clearSystemProperty() {
		System.clearProperty(KEY);
	}

	@EnabledIfEnvironmentVariable(named = KEY, matches = BOGUS)
	void enabledIfEnvironmentVariable() {
		fail("should NOT be enabled since this class doesn't set environment variables");
	}

	@EnabledIfSystemProperty(named = KEY, matches = BOGUS)
	void enabledIfSystemPropertyWithDisabledCondition() {
		fail("should NOT be enabled");
	}

	@EnabledOnOs(MAC)
	void enabledOnOs() {
		if (!MAC.isCurrentOs()) {
			fail("should be disabled on MAC");
		}
	}

	@EnabledOnJre(JAVA_9)
	void enabledOnJre() {
		if (!JAVA_9.isCurrentVersion()) {
			fail("should be disabled on JAVA_9");
		}
	}

}
